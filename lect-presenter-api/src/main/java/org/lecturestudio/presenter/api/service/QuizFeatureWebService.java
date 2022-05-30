/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.model.QuizDocument;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;

public class QuizFeatureWebService extends FeatureServiceBase {

	private final Consumer<QuizAnswerMessage> messageConsumer = this::onMessage;

	/** The web service client. */
	private final QuizFeatureService webService;

	private final DocumentService documentService;

	private final EventBus eventBus;

	/** Asynchronous quiz result render queue with only one rendering task in the queue. */
	private ExecutorService executorService;

	private Quiz quiz;

	private QuizResult quizResult;

	private Document quizDocument;

	/* The received answer count. */
	private long answerCount;


	/**
	 * Creates a new {@link QuizFeatureWebService}.
	 *
	 * @param context         The application context.
	 * @param featureService  The quiz web feature service.
	 * @param documentService The document service.
	 */
	public QuizFeatureWebService(ApplicationContext context,
			QuizFeatureService featureService,
			DocumentService documentService) {
		super(context);

		this.webService = featureService;
		this.documentService = documentService;
		this.eventBus = context.getEventBus();
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

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(quiz)) {
			throw new ExecutableException("No quiz provided");
		}

		executorService = new ThreadPoolExecutor(1, 1, 0L,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (isNull(quizResult)) {
			throw new ExecutableException("No quiz result created");
		}

		answerCount = 0;

		// Create a copy, since the markup of the question gets changed for the web view.
		Quiz webQuiz = quiz.clone();

		try {
			MultipartBody data = getQuizResources(webQuiz);
			data.addFormData("quiz", webQuiz, MediaType.APPLICATION_JSON_TYPE);

			serviceId = webService.startQuiz(courseId, data);

			webService.addMessageListener(QuizAnswerMessage.class, messageConsumer);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		if (isNull(quizDocument)) {
			updateQuizDocument();
		}
		else {
			// Replace quiz document in silent-mode.
			updateQuizDocument(false);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			webService.removeMessageListener(QuizAnswerMessage.class, messageConsumer);
			webService.stopQuiz(courseId);
			// Stop receiving quiz events.
			webService.close();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() {
		executorService.shutdown();
	}

	private void onMessage(QuizAnswerMessage message) {
		QuizAnswer answer = message.getQuizAnswer();

		if (isNull(answer.getOptions())) {
			// Handle malformed answer.
			answer.setOptions(new String[0]);
		}

		if (quizResult.addAnswer(answer)) {
			updateQuizDocumentAsync();

			answerCount++;

			eventBus.post(new QuizWebServiceState(getState(), answerCount));
		}
	}

	private Document createQuizDocument(final QuizResult result) throws ExecutableException {
		Document doc;

		try {
			Dictionary dict = context.getDictionary();

			doc = new QuizDocument(dict, result);
			doc.setDocumentType(DocumentType.QUIZ);
		}
		catch (Exception e) {
			throw new ExecutableException("Create quiz document failed.", e);
		}

		return doc;
	}

	private void updateQuizDocument() throws ExecutableException {
		// Add quiz document.
		quizDocument = createQuizDocument(quizResult);
		quizDocument.setUid(UUID.randomUUID());

		for (var page : quizDocument.getPages()) {
			page.setUid(UUID.randomUUID());
		}

		Document prevQuizDoc = null;

		// Find old quiz document.
		for (Document doc : documentService.getDocuments().asList()) {
			if (doc.isQuiz()) {
				prevQuizDoc = doc;
			}
		}

		if (nonNull(prevQuizDoc)) {
			documentService.replaceDocument(prevQuizDoc, quizDocument);
		}
		else {
			documentService.addDocument(quizDocument);
		}

		documentService.selectDocument(quizDocument);
	}

	private void updateQuizDocument(boolean copyAnnotations) {
		try {
			final Document oldDoc = quizDocument;
			quizDocument = createQuizDocument(quizResult);
			quizDocument.setUid(copyAnnotations ? oldDoc.getUid() : UUID.randomUUID());

			for (int i = 0; i < quizDocument.getPageCount(); i++) {
				Page page = quizDocument.getPage(i);
				Page oldPage = oldDoc.getPage(i);

				if (nonNull(oldPage)) {
					page.setUid(copyAnnotations ? oldPage.getUid() : null);
				}
			}

			documentService.replaceDocument(oldDoc, quizDocument, copyAnnotations);
		}
		catch (Exception e) {
			logException(e, "Create quiz document failed");
		}
	}

	private void updateQuizDocumentAsync() {
		try {
			executorService.execute(() -> {
				if (started()) {
					updateQuizDocument(true);
				}
			});
		}
		catch (Exception e) {
			// Ignore. May happen if execution was rejected, which is the
			// objective to avoid multiple renderings.
		}
	}

	private MultipartBody getQuizResources(Quiz quiz) throws IOException {
		MultipartBody body = new MultipartBody();
		Map<File, String> files = loadQuizContent(quiz);

		for (var entry : files.entrySet()) {
			File file = entry.getKey();
			String mimeType = Files.probeContentType(file.toPath());

			if (isNull(mimeType)) {
				mimeType = "*/*";
			}

			logDebugMessage("Upload quiz resource: %s as %s", file, mimeType);

			body.addFormData("files", new FileInputStream(file),
					MediaType.valueOf(mimeType), entry.getValue());
		}

		return body;
	}

	private Map<File, String> loadQuizContent(Quiz quiz) {
		Map<File, String> fileMap = new HashMap<>();

		// Parse template and pretty print
		String html = quiz.getQuestion();
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		doc.outputSettings().prettyPrint(false);

		// Post process question HTML.
		// Add a whitespace within empty div's.
		for (Element element : doc.select("div")) {
			if (!element.hasText() && element.isBlock()) {
				element.appendElement("br");
			}
		}

		// Get all elements with image tag.
		Elements img = doc.getElementsByTag("img");
		for (Element e : img) {
			String src = e.absUrl("src");
			File imgFile = new File(URI.create(src).getPath());

			String generatedName = UUID.randomUUID() + "."
					+ FilenameUtils.getExtension(imgFile.getName());

			fileMap.put(imgFile, generatedName);

			// Replace by new relative web-root path.
			e.attr("src", Paths.get(Long.toString(courseId), "quiz", "resource",
					generatedName).toString().replaceAll("\\\\", "/"));
		}

		html = doc.body().html();

		quiz.setQuestion(html);

		return fileMap;
	}
}
