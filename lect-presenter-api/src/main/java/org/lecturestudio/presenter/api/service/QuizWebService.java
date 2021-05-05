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
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.pdf.PdfFactory;
import org.lecturestudio.presenter.api.quiz.QuizResultCsvWriter;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;
import org.lecturestudio.web.api.service.QuizProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;

public class QuizWebService extends ExecutableBase {

	private final ApplicationContext context;

	private final EventBus eventBus;

	/** The web service client. */
	private QuizProviderService webService;

	/** Asynchronous quiz result render queue with only one rendering task in the queue. */
	private ExecutorService executorService;

	private Quiz quiz;

	private QuizResult quizResult;

	private Document quizDocument;

	private String classroomId;

	private String serviceId;

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

	public void setClassroomId(String id) {
		requireNonNull(id);

		classroomId = id;
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
			serviceId = webService.startQuiz(classroomId, webQuiz);

			webService.subscribe(serviceId, message -> {
				QuizAnswer answer = message.getQuizAnswer();

				if (isNull(answer.getOptions())) {
					// Handle malformed answer.
					answer.setOptions(new String[0]);
				}

				if (quizResult.addAnswer(answer)) {
					updateQuizDocument();

					answerCount++;

					eventBus.post(new QuizWebServiceState(getState(), answerCount));
				}
			}, error -> logException(error, "Quiz event failure"));
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
			webService.stopQuiz(classroomId, serviceId);
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
						logException(e, "Create quiz document failed");
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
			e.attr("src", Paths.get("classrooms", imgFile.getName()).toString()
					.replaceAll("\\\\", "/"));
		}
		
		html = doc.body().html();
		
		quiz.setQuestion(html);
		
		return includeFiles;
	}

	private void initSession() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		String broadcastAddress = netConfig.getBroadcastAddress();
		int broadcastPort = netConfig.getBroadcastTlsPort();

		ServiceParameters params = new ServiceParameters();
		params.setUrl(String.format("https://%s:%d", broadcastAddress, broadcastPort));

		webService = new QuizProviderService(params);
	}
}
