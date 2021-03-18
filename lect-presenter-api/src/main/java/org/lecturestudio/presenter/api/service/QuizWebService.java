/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.pdf.PdfFactory;
import org.lecturestudio.web.api.connector.ConnectorFactory;
import org.lecturestudio.web.api.connector.JsonDecoder;
import org.lecturestudio.web.api.connector.client.ClientConnector;
import org.lecturestudio.web.api.connector.client.ClientTcpConnectorHandler;
import org.lecturestudio.web.api.connector.client.ConnectorListener;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.message.WebPacket;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.QuizService;
import org.lecturestudio.web.api.model.StreamDescription;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;
import org.lecturestudio.web.api.model.quiz.io.QuizResultCsvWriter;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.QuizServiceClient;
import org.lecturestudio.web.api.ws.rs.QuizRestClient;

public class QuizWebService extends ExecutableBase implements ConnectorListener<WebPacket> {

	private static final Logger LOG = LogManager.getLogger(QuizWebService.class);

	private final ApplicationContext context;

	private final EventBus eventBus;

	/** The web service client. */
	private QuizServiceClient webService;

	private ClientConnector connector;

	private Classroom classroom;

	private org.lecturestudio.web.api.model.QuizService service;

	/** Asynchronous quiz result render queue with only one rendering task in the queue. */
	private ExecutorService executorService;

	private Quiz quiz;

	private QuizResult quizResult;

	private Document quizDocument;

	/* The received answer count. */
	private long answerCount;


	/**
	 * Creates a new {@link QuizWebService}.
	 *
	 * @param context The ApplicationContext.
	 */
	public QuizWebService(ApplicationContext context) {
		this.context = context;
		this.eventBus = context.getEventBus();
	}

	/**
	 * Returns the quiz document that contains the question and results
	 * of the current quiz session.
	 *
	 * @return the current quiz document.
	 */
	public Document getQuizDocument() {
		return quizDocument;
	}

	/**
	 * Sets a new quiz.
	 *
	 * @param quiz The quiz to start.
	 */
	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
		this.quizResult = new QuizResult(quiz);
	}

	/**
	 * Writes quiz results to the provided file paths. Each file may have its own
	 * individual file format. If the file format is not supported, the file will
	 * be skipped.
	 *
	 * @param files The files to write to with individual file formats.
	 * @param callback The callback to be invoked on write progress.
	 *
	 * @throws IOException if the quiz results could not be written to the files.
	 * @throws NullPointerException if no quiz result was created.
	 */
	public void saveQuizResult(List<String> files, ProgressCallback callback) throws IOException {
		if (isNull(quizResult)) {
			throw new NullPointerException("No quiz result created");
		}

		int total = files.size();
		int count = 0;

		for (String file : files) {
			String ext = FileUtils.getExtension(file);
			switch (ext) {
				case "csv":
					QuizResultCsvWriter csvWriter = new QuizResultCsvWriter(',');
					csvWriter.write(quizResult, new File(file));
					break;

				case "pdf":
					FileOutputStream fileStream = new FileOutputStream(file);
					quizDocument.toOutputStream(fileStream);
					fileStream.close();
					break;

				default:
					break;
			}

			callback.onProgress(1.f * ++count / total);
		}
	}

	@Override
	public void onConnectorRead(WebPacket packet) {
		Class<?> msgClass = packet.getMessage().getClass();

		if (QuizAnswerMessage.class.isAssignableFrom(msgClass)) {
			// Make sure the quiz is active and equal to the running one.
			if (nonNull(quizResult)) {
				QuizAnswerMessage quizMessage = (QuizAnswerMessage) packet.getMessage();
				QuizAnswer answer = quizMessage.getQuizAnswer();

				if (isNull(answer.getOptions())) {
					// Handle malformed answer.
					answer.setOptions(new String[0]);
				}

				if (quizResult.addAnswer(quizMessage.getQuizAnswer())) {
					updateQuizDocument();

					answerCount++;

					eventBus.post(new QuizWebServiceState(getState(), answerCount));
				}
			}
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(quiz)) {
			throw new NullPointerException("No quiz provided.");
		}

		try {
			initSession();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		answerCount = 0;
		executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (isNull(quizResult)) {
			throw new NullPointerException("No quiz result created.");
		}

		// Create a copy, since the markup of the question gets changed for the web view.
		Quiz webQuiz = quiz.clone();

		List<File> files = loadQuizContent(webQuiz);

		try {
			Classroom webClassroom = webService.startService(classroom, service);

			connector = createConnector(webClassroom);
			connector.start();
			
			// Upload files included in the HTML source.
			if (!files.isEmpty()) {
				for (File file : files) {
					webService.sendQuizFile(classroom, file.getAbsolutePath());
				}
			}
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		quizDocument = createQuizDocument(quizResult);

		eventBus.register(this);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		eventBus.unregister(this);

		try {
			webService.stopService(classroom, service);

			connector.stop();
			connector.destroy();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() {
		executorService.shutdown();
	}
	
	private Document createQuizDocument(final QuizResult result) throws ExecutableException {
		Document doc;

		try {
			Dictionary dict = context.getDictionary();
			PdfDocument pdfDoc = PdfFactory.createQuizDocument(dict, result);
			
			doc = new Document(pdfDoc);
			doc.setDocumentType(DocumentType.QUIZ);
		}
		catch (Exception e) {
			throw new ExecutableException("Create quiz document failed.", e);
		}
		
		return doc;
	}

	private void updateQuizDocument() {
		try {
			executorService.execute(() -> {
				if (getState() == ExecutableState.Started) {
					Dictionary dict = context.getDictionary();
					try {
						PdfDocument pdfDoc = PdfFactory.createQuizDocument(dict, quizResult);
						
						quizDocument.setPdfDocument(pdfDoc);
					}
					catch (Exception e) {
						LOG.error("Create quiz document failed.", e);
					}
				}
			});
		}
		catch (Exception e) {
			// Ignore. May happen if execution was rejected, which is the objective.
		}
	}

	private List<File> loadQuizContent(Quiz quiz) {
		// Parse template and pretty print
		String html = quiz.getQuestion();
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);
		
		// Post process question HTML.
		// Add a whitespace within empty div's.
		for (Element element : doc.select("div")) {
			if (!element.hasText() && element.isBlock()) {
				element.html("&nbsp;");
			}
		}
		
		List<File> includeFiles = new ArrayList<>();
		
		// Get all elements with image tag.
		Elements img = doc.getElementsByTag("img");
		for (Element e : img) {
			String src = e.absUrl("src");
			File imgFile = new File(URI.create(src).getPath());
			
			includeFiles.add(imgFile);
			
			// Replace by new relative web-root path.
			e.attr("src", Paths.get("classrooms", classroom.getShortName(),
					imgFile.getName()).toString().replaceAll("\\\\", "/"));
		}
		
		html = doc.body().html();
		
		quiz.setQuestion(html);
		
		return includeFiles;
	}
	
	private void initSession() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		String broadcastAddress = netConfig.getBroadcastAddress();
		int broadcastPort = netConfig.getBroadcastTlsPort();
		String classShortName = config.getClassroomShortName();

		ConnectionParameters parameters = new ConnectionParameters(broadcastAddress, broadcastPort, true);

		if (NetUtils.isLocalAddress(broadcastAddress, broadcastPort)) {
			// No need to identify classroom by short name on local machine.
			classShortName = "";
		}

		webService = new QuizRestClient(parameters);

		classroom = new Classroom(config.getClassroomName(), classShortName);
		classroom.setLocale(config.getLocale());
		classroom.setShortName(classShortName);
		classroom.setIpFilterRules(netConfig.getIpFilter().getRules());

		Quiz webQuiz = quiz.clone();

		loadQuizContent(webQuiz);

		service = new org.lecturestudio.web.api.model.QuizService();
		service.setQuiz(webQuiz);
		service.setRegexRules(config.getQuizConfig().getInputFilter().getRules());
	}
	
	private ClientConnector createConnector(Classroom classroom) throws Exception {
		Optional<StreamDescription> streamDesc = classroom.getServices()
				.stream()
				.filter(QuizService.class::isInstance)
				.flatMap(service -> service.getStreamDescriptions().stream())
				.filter(desc -> desc.getMediaType() == MediaType.Messenger)
				.findFirst();

		if (streamDesc.isEmpty()) {
			throw new Exception("No stream provided for the quiz session.");
		}
		
		ClientConnector connector = ConnectorFactory.createClientConnector(streamDesc.get());
		connector.addChannelHandler(new JsonDecoder());
		connector.addChannelHandler(new ClientTcpConnectorHandler<>(this));
		
		return connector;
	}
	
}
