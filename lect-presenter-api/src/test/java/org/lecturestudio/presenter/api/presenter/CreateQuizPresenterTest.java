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

package org.lecturestudio.presenter.api.presenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.service.QuizDataSource;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.service.WebRtcStreamEventRecorder;
import org.lecturestudio.presenter.api.service.WebRtcStreamService;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.service.WebServiceInfo;
import org.lecturestudio.presenter.api.view.CreateQuizDefaultOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizNumericOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizView;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.model.quiz.Quiz;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreateQuizPresenterTest extends PresenterTest {

	private static final Quiz.QuizType[] TYPES = {
			Quiz.QuizType.MULTIPLE,
			Quiz.QuizType.SINGLE,
			Quiz.QuizType.NUMERIC
	};

	private Path quizPath;

	private Path quizzesDir;

	private DocumentService documentService;

	private QuizService quizService;

	private StreamService streamService;

	private List<CreateQuizOptionMockView> optionViews;

	private GuiceInjector injector;

	@BeforeEach
	void setup() throws IOException, URISyntaxException, ExecutableException {
		quizzesDir = getResourcePath(".");

		quizPath = quizzesDir.resolve("empty.quiz");

		String quizzesFile = quizPath.toFile().getPath();

		Document document = new Document(quizzesDir.resolve("empty.pdf").toFile());

		documentService = new DocumentService(context);
		documentService.addDocument(document);
		documentService.selectDocument(document);

		quizService = new QuizService(new QuizDataSource(new File(quizzesFile)), documentService);

		Properties streamProps = new Properties();
		streamProps.load(getClass().getClassLoader().getResourceAsStream("resources/stream.properties"));

		WebServiceInfo webServiceInfo = new WebServiceInfo(streamProps);

		WebService webService = new WebService((PresenterContext) context, documentService, new LocalBroadcaster(context), webServiceInfo);

		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");

		recorder = new FileLectureRecorder(context, audioSystemProvider, documentService, audioConfig, getRecordingDirectory());

		injector = new GuiceInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ViewContextFactory.class).to(DIViewContextFactory.class);
			}

			@Provides
			@Singleton
			ApplicationContext provideApplicationContext() {
				return context;
			}

			@Provides
			@Singleton
			AudioSystemProvider provideAudioSystemProvider() {
				return audioSystemProvider;
			}

			@Provides
			@Singleton
			WebService provideWebService() {
				return webService;
			}

			@Provides
			@Singleton
			DocumentService provideDocumentService() {
				return documentService;
			}

			@Provides
			FileLectureRecorder provideFileLectureRecorder() {
				return recorder;
			}

			@Provides
			@Singleton
			WebServiceInfo provideWebServiceInfo() {
				return webServiceInfo;
			}
		});

		streamService = new StreamService((PresenterContext) context, injector.getInstance(MockWebRtcStreamService.class), webService);

		optionViews = new LinkedList<>();

		viewFactory = new ViewContextMockFactory() {

			final String[] options = {"choose this", "choose that", "I don't understand", "I know this", "I actually dont", "I might", "I might", "I might", "I might", "choose this", "choose that", "I don't understand", "I know this", "I actually dont", "I might", "I might", "I might", "I might"};


			@Override
			@SuppressWarnings("unchecked")
			public <T> T getInstance(Class<T> cls) {
				if (cls == CreateQuizDefaultOptionView.class) {
					CreateQuizOptionMockView view = new CreateQuizDefaultOptionMockView();
					view.setOptionText(options[optionViews.size()]);

					optionViews.add(view);

					return (T) view;
				}
				else if (cls == CreateQuizNumericOptionView.class) {
					CreateQuizOptionMockView view = new CreateQuizNumericOptionMockView();
					view.setOptionText(options[optionViews.size()]);

					optionViews.add(view);

					return (T) view;
				}

				return null;
			}
		};
	}

	@AfterEach
	void dispose() throws IOException {
		Files.deleteIfExists(quizzesDir.resolve("empty.quizzes"));
	}

	@Test
	void testDocuments() throws Exception {
		CreateQuizView view = new CreateQuizMockView() {
			@Override
			public void setDocuments(List<Document> documents) {
				assertEquals(2, documents.size());
			}

			@Override
			public void setDocument(Document doc) {
				assertEquals(doc, documentService.getDocuments().getSelectedDocument());
			}
		};

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();
	}

	@Test
	void testDocumentSelected() throws Exception {
		CreateQuizMockView view = new CreateQuizMockView();

		documentService.getDocuments().getSelectedDocument().setDocumentType(DocumentType.SCREEN);
		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();
		presenter.getQuiz().setQuestion("Smart question");
		presenter.getQuiz().setType(Quiz.QuizType.SINGLE);
		presenter.getQuiz().addOption("1");
		presenter.getQuiz().addOption("2");
		presenter.getQuiz().addOption("3");

		assertEquals(Quiz.QuizSet.GENERIC, presenter.getQuiz().getQuizSet());

		view.docSelectAction.execute(documentService.getDocuments().getSelectedDocument());

		assertEquals(Quiz.QuizSet.DOCUMENT_SPECIFIC, presenter.getQuiz().getQuizSet());
		assertEquals(Quiz.QuizType.SINGLE, presenter.getQuiz().getType());
		assertEquals("Smart question", presenter.getQuiz().getQuestion());
		assertEquals(3, presenter.getQuiz().getOptions().size());
	}

	@Test
	void testNewOption() throws Exception {
		AtomicReference<Quiz.QuizType> type = new AtomicReference<>(Quiz.QuizType.MULTIPLE);

		CreateQuizMockView view = new CreateQuizMockView() {
			@Override
			public void addQuizOptionView(CreateQuizOptionView optionView) {
				if (type.get() == Quiz.QuizType.MULTIPLE || type.get() == Quiz.QuizType.SINGLE) {
					assertEquals(CreateQuizDefaultOptionView.class, optionView.getClass().getInterfaces()[0]);
				}
				else if (type.get() == Quiz.QuizType.NUMERIC) {
					assertEquals(CreateQuizNumericOptionView.class, optionView.getClass().getInterfaces()[0]);
				}
			}
		};

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();

		for (Quiz.QuizType t : TYPES) {
			optionViews.clear();

			type.set(t);
			view.quizTypeAction.execute(t);
			view.newOptionAction.execute();
		}
	}

	@Test
	void testSaveQuiz() throws Exception {
		CreateQuizMockView view = new CreateQuizMockView();
		documentService.getDocuments().getSelectedDocument().setDocumentType(DocumentType.SCREEN);

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();

		view.setQuizText("HTML question content..");

		int count = 0;

		for (Quiz.QuizType type : TYPES) {
			testSavingOp(view, presenter, Quiz.QuizSet.GENERIC, type, ++count);
		}

		documentService.getDocuments().getSelectedDocument().setDocumentType(DocumentType.PDF);
		view.docSelectAction.execute(documentService.getDocuments().getSelectedDocument());

		for (Quiz.QuizType type : TYPES) {
			testSavingOp(view, presenter, Quiz.QuizSet.DOCUMENT_SPECIFIC, type, ++count);
		}
	}

	@Test
	void testStartQuiz() throws Exception {
		CreateQuizMockView view = new CreateQuizMockView();

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();

		view.setQuizText("HTML question content..");
		view.startQuizAction.execute();

		assertEquals(view.getQuizText(), presenter.getQuiz().getQuestion());
	}

	@Test
	void testRemoveQuizOption() throws Exception {
		CreateQuizMockView view = new CreateQuizMockView();

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();

		view.setQuizText("HTML question content..");
		view.newOptionAction.execute();
		view.newOptionAction.execute();
		view.saveQuizAction.execute();

		int count = presenter.getQuiz().getOptions().size();
		List<String> optionList = optionViews.stream()
				.map(CreateQuizOptionMockView::getOptionText)
				.collect(Collectors.toList());
		ListIterator<String> optionListIter = optionList.listIterator();

		assertEquals(optionList.size(), count);

		for (int i = 0; i < count; i++) {
			optionViews.get(i).removeAction.execute();
			view.saveQuizAction.execute();

			optionListIter.next();
			optionListIter.remove();

			assertEquals(count - i - 1, presenter.getQuiz().getOptions().size());
			assertEquals(optionList, presenter.getQuiz().getOptions());
		}
	}

	@Test
	void testMoveQuizOptionDown() throws Exception {
		CreateQuizMockView view = new CreateQuizMockView();

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();

		view.setQuizText("HTML question content..");
		view.newOptionAction.execute();
		view.newOptionAction.execute();
		view.saveQuizAction.execute();

		int count = presenter.getQuiz().getOptions().size();
		List<String> optionList = optionViews.stream()
				.map(CreateQuizOptionMockView::getOptionText)
				.collect(Collectors.toList());

		assertEquals(optionList.size(), count);

		for (int i = 0; i < count; i++) {
			optionViews.get(i).moveDownAction.execute();
			view.saveQuizAction.execute();

			if (i < count - 1) {
				Collections.swap(optionViews, i, i + 1);
				Collections.swap(optionList, i, i + 1);
			}

			assertEquals(optionList.size(), presenter.getQuiz().getOptions().size());
			assertEquals(optionList, presenter.getQuiz().getOptions());
		}
	}

	@Test
	void testMoveQuizOptionUp() throws Exception {
		CreateQuizMockView view = new CreateQuizMockView();

		CreateQuizPresenter presenter = new CreateQuizPresenter(context, view,
				viewFactory, documentService, quizService, streamService);
		presenter.initialize();

		view.setQuizText("HTML question content..");
		view.newOptionAction.execute();
		view.newOptionAction.execute();
		view.saveQuizAction.execute();

		int count = presenter.getQuiz().getOptions().size();
		List<String> optionList = optionViews.stream()
				.map(CreateQuizOptionMockView::getOptionText)
				.collect(Collectors.toList());

		assertEquals(optionList.size(), count);

		for (int i = count - 1; i >= 0; i--) {
			optionViews.get(i).moveUpAction.execute();
			view.saveQuizAction.execute();

			if (i > 0) {
				Collections.swap(optionViews, i, i - 1);
				Collections.swap(optionList, i, i - 1);
			}

			assertEquals(optionList.size(), presenter.getQuiz().getOptions().size());
			assertEquals(optionList, presenter.getQuiz().getOptions());
		}
	}

	private void testSavingOp(CreateQuizMockView view, CreateQuizPresenter presenter, Quiz.QuizSet set, Quiz.QuizType type, int count)
			throws IOException {
		optionViews.clear();
		presenter.setQuiz(null);
		view.quizTypeAction.execute(type);
		view.newOptionAction.execute();
		view.newOptionAction.execute();
		view.saveQuizAction.execute();

		List<String> optionList = optionViews.stream()
				.map(CreateQuizOptionMockView::getOptionText)
				.collect(Collectors.toList());

		assertEquals(set, presenter.getQuiz().getQuizSet());
		assertEquals(type, presenter.getQuiz().getType());
		assertEquals("HTML question content..", presenter.getQuiz().getQuestion());
		assertEquals(optionList.size(), presenter.getQuiz().getOptions().size());
		assertEquals(optionList, presenter.getQuiz().getOptions());

		assertEquals(count, quizService.getQuizzes().size());
	}


	private static class CreateQuizMockView implements CreateQuizView {

		ConsumerAction<Document> docSelectAction;

		Action newOptionAction;

		Action saveQuizAction;

		Action startQuizAction;

		Action saveAndNextQuizAction;

		ConsumerAction<Quiz.QuizType> quizTypeAction;

		String question;


		@Override
		public void clearOptions() {

		}

		@Override
		public void addQuizOptionView(CreateQuizOptionView optionView) {

		}

		@Override
		public void removeQuizOptionView(CreateQuizOptionView optionView) {

		}

		@Override
		public void moveQuizOptionViewUp(CreateQuizOptionView optionView) {

		}

		@Override
		public void moveQuizOptionViewDown(CreateQuizOptionView optionView) {

		}

		@Override
		public String getQuizText() {
			return question;
		}

		@Override
		public void setQuizText(String text) {
			this.question = text;
		}

		@Override
		public void setDocuments(List<Document> documents) {

		}

		@Override
		public void setDocument(Document doc) {

		}

		@Override
		public void setQuizType(Quiz.QuizType type) {

		}

		@Override
		public void setOnDocumentSelected(ConsumerAction<Document> action) {
			assertNotNull(action);

			docSelectAction = action;
		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnNewOption(Action action) {
			assertNotNull(action);

			newOptionAction = action;
		}

		@Override
		public void setOnSaveQuiz(Action action) {
			assertNotNull(action);

			saveQuizAction = action;
		}

		@Override
		public void setOnSaveAndNextQuiz(Action action) {
			saveAndNextQuizAction = action;
		}

		@Override
		public void setOnStartQuiz(Action action) {
			assertNotNull(action);

			startQuizAction = action;
		}

		@Override
		public void setOnQuizType(ConsumerAction<Quiz.QuizType> action) {
			assertNotNull(action);

			quizTypeAction = action;
		}
	}



	private static class CreateQuizDefaultOptionMockView extends CreateQuizOptionMockView implements CreateQuizDefaultOptionView {

	}



	private static class CreateQuizNumericOptionMockView extends CreateQuizOptionMockView implements CreateQuizNumericOptionView {

		@Override
		public int getMinValue() {
			return 0;
		}

		@Override
		public int getMaxValue() {
			return 0;
		}

		@Override
		public void setMinValue(int value) {

		}

		@Override
		public void setMaxValue(int value) {

		}
	}



	private static abstract class CreateQuizOptionMockView implements CreateQuizOptionView {

		private String option;

		Action changeAction;

		Action removeAction;

		Action moveUpAction;

		Action moveDownAction;


		@Override
		public void focus() {

		}

		@Override
		public String getOptionText() {
			return option;
		}

		@Override
		public void setOptionText(String text) {
			this.option = text;
		}

		@Override
		public void addOnChange(Action action) {
			assertNotNull(action);

			changeAction = action;
		}

		@Override
		public void setOnRemove(Action action) {
			assertNotNull(action);

			removeAction = action;
		}

		@Override
		public void setOnMoveUp(Action action) {
			assertNotNull(action);

			moveUpAction = action;
		}

		@Override
		public void setOnMoveDown(Action action) {
			assertNotNull(action);

			moveDownAction = action;
		}

		@Override
		public void setOnEnterKey(Action action) {
			assertNotNull(action);
		}

		@Override
		public void setOnTabKey(Action action) {
			assertNotNull(action);
		}
	}

	public static class MockWebRtcStreamService extends WebRtcStreamService {

		@Inject
		public MockWebRtcStreamService(ApplicationContext context, WebServiceInfo webServiceInfo, WebRtcStreamEventRecorder eventRecorder, RecordingService recordingService) throws ExecutableException {
			super(context, webServiceInfo, eventRecorder, recordingService);
		}

		@Override
		public void acceptSpeechRequest(SpeechBaseMessage message) {

		}

		@Override
		public void rejectSpeechRequest(SpeechBaseMessage message) {

		}

		@Override
		public void startCameraStream() {

		}

		@Override
		public void stopCameraStream() {

		}

		@Override
		public void setScreenShareContext(ScreenShareContext context) {

		}

		@Override
		public void startScreenShare() {

		}

		@Override
		public void stopScreenShare() {

		}

		@Override
		public void mutePeerAudio(boolean mute) {

		}

		@Override
		public void mutePeerVideo(boolean mute) {

		}

		@Override
		public void stopPeerConnection(UUID requestId) {

		}

		@Override
		public void shareDocument(Document document) {

		}

		@Override
		public ExecutableState getScreenShareState() {
			return null;
		}

		@Override
		public void initInternal() {

		}

		@Override
		public void startInternal() {

		}

		@Override
		public void stopInternal() {

		}

		@Override
		public void destroyInternal() {

		}
	}
}