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

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.service.DisplayService;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationViewFactory;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.service.WebRtcStreamEventRecorder;
import org.lecturestudio.presenter.api.service.WebRtcStreamService;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.service.WebServiceInfo;
import org.lecturestudio.presenter.api.view.MainView;
import org.lecturestudio.presenter.api.view.QuitRecordingView;
import org.lecturestudio.presenter.api.view.RestoreRecordingView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;
import org.lecturestudio.presenter.api.view.SettingsView;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainPresenterTest extends PresenterTest {

	private static final Screen[] SCREENS = {
			new Screen(0, 0, 1280, 720),
			new Screen(1280, 720, 1920, 1200),
			new Screen(-640, -480, 640, 480)
	};


	private ViewContextFactory viewFactory;

	private BookmarkService bookmarkService;

	private DocumentService documentService;

	private RecordingService recordingService;

	private ObservableList<Screen> screens;

	private PresentationController presentationController;

	private WebRtcStreamService streamService;

	private GuiceInjector injector;

	private DocumentRecorder documentRecorder;


	@BeforeEach
	void setup() throws IOException {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");

		documentService = context.getDocumentService();

		bookmarkService = new BookmarkService(documentService);

		recorder = new FileLectureRecorder(audioSystemProvider, documentService, audioConfig, getRecordingDirectory());

		recordingService = new RecordingService(context, recorder);

		screens = new ObservableArrayList<>();
		screens.addAll(List.of(SCREENS));

		DisplayService displayService = () -> screens;

		PresentationViewFactory factory = (context, screen) -> null;

		presentationController = new PresentationController(context, displayService, factory);

		Properties streamProps = new Properties();
		streamProps.load(getClass().getClassLoader().getResourceAsStream("resources/stream.properties"));

		WebServiceInfo webServiceInfo = new WebServiceInfo(streamProps);

		WebService webService = new WebService((PresenterContext) context, documentService, new LocalBroadcaster(context), webServiceInfo);
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

			@Provides
			RecordingService provideRecordingService() {
				return recordingService;
			}
		});


		streamService = injector.getInstance(CreateQuizPresenterTest.MockWebRtcStreamService.class);

		documentRecorder = new DocumentRecorder(context);

		viewFactory = new ViewContextMockFactory() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T getInstance(Class<T> cls) {
				if (cls == SlidesPresenter.class) {
					ToolController toolController = new ToolController(context, documentService);
					return (T) new SlidesPresenter(context, createProxy(SlidesView.class), null, toolController, presentationController, null, documentService, documentRecorder, recordingService, webService, webServiceInfo, streamService);
				}
				else if (cls == SettingsPresenter.class) {
					return (T) new SettingsPresenter(context, createProxy(SettingsView.class));
				}
				else if (cls == RestoreRecordingPresenter.class) {
					return (T) new RestoreRecordingPresenter(context, createProxy(RestoreRecordingView.class), null);
				}
				else if (cls == QuitRecordingPresenter.class) {
					return (T) new QuitRecordingPresenter(context, createProxy(QuitRecordingView.class), null);
				}
				else if (cls == SaveDocumentsPresenter.class) {
					return (T) new SaveDocumentsPresenter(context, createProxy(SaveDocumentsView.class), viewFactory, documentRecorder);
				}

				return super.getInstance(cls);
			}
		};
	}

	@Test
	void testShowView() throws Exception {
		class TestView implements View { }

		View testView = new TestView();

		MainMockView view = new MainMockView() {
			@Override
			public void showView(View view, ViewLayer layer) {
				assertEquals(testView, view);
				assertEquals(ViewLayer.Content, layer);
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.showView(testView, ViewLayer.Content);
	}

	@Test
	void testDisplayContext() throws Exception {
		AtomicBoolean initialized = new AtomicBoolean(false);
		AtomicBoolean shown = new AtomicBoolean(false);
		AtomicInteger showCounter = new AtomicInteger(0);

		class TestView implements View { }
		class TestPresenter extends Presenter<TestView> {
			TestPresenter(ApplicationContext context, TestView view) {
				super(context, view);
			}

			@Override
			public void initialize() throws Exception {
				super.initialize();

				initialized.set(true);
			}
		}

		MainMockView view = new MainMockView() {
			@Override
			public void showView(View view, ViewLayer layer) {
				assertTrue(view instanceof TestView);
				assertEquals(ViewLayer.Content, layer);

				shown.set(true);
				showCounter.incrementAndGet();
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.display(new TestPresenter(context, new TestView()));

		assertTrue(initialized.get());
		assertTrue(shown.get());
		assertEquals(1, showCounter.get());

		// Don't show same view if it's currently visible.
		presenter.display(new TestPresenter(context, new TestView()));
		assertEquals(1, showCounter.get());
	}

	@Test
	void testDisplayCachedContext() throws Exception {
		AtomicBoolean initialized = new AtomicBoolean(false);
		AtomicBoolean shown = new AtomicBoolean(false);
		AtomicInteger initializedCounter = new AtomicInteger(0);

		class TestView implements View { }
		class TestPresenter extends Presenter<TestView> {
			TestPresenter(ApplicationContext context, TestView view) {
				super(context, view);
			}

			@Override
			public void initialize() throws Exception {
				super.initialize();

				initialized.set(true);
				initializedCounter.incrementAndGet();
			}

			@Override
			public boolean cache() {
				return true;
			}
		}

		MainMockView view = new MainMockView() {
			@Override
			public void showView(View view, ViewLayer layer) {
				assertTrue(view instanceof TestView);
				assertEquals(ViewLayer.Content, layer);

				shown.set(true);
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.display(new TestPresenter(context, new TestView()));

		assertTrue(initialized.get());
		assertTrue(shown.get());
		assertEquals(1, initializedCounter.get());

		// A cached context of the same type should be initialized only once.
		presenter.display(new TestPresenter(context, new TestView()));
		assertEquals(1, initializedCounter.get());
	}

	@Test
	void testDestroyContext() throws Exception {
		AtomicBoolean removed = new AtomicBoolean(false);
		AtomicBoolean destroyed = new AtomicBoolean(false);

		class TestView implements View { }
		class TestPresenter extends Presenter<TestView> {
			TestPresenter(ApplicationContext context, TestView view) {
				super(context, view);
			}

			@Override
			public void destroy() {
				destroyed.set(true);

				super.destroy();
			}
		}

		MainMockView view = new MainMockView() {
			@Override
			public void removeView(View view, ViewLayer layer) {
				assertTrue(view instanceof TestView);
				assertEquals(ViewLayer.Content, layer);

				removed.set(true);
			}
		};

		TestPresenter testPresenter = new TestPresenter(context, new TestView());

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.display(testPresenter);
		presenter.destroy(testPresenter);

		assertTrue(removed.get());
		assertTrue(destroyed.get());
	}

	@Test
	void testDestroyCachedContext() throws Exception {
		AtomicBoolean removed = new AtomicBoolean(false);
		AtomicBoolean destroyed = new AtomicBoolean(false);

		class TestView implements View { }
		class TestPresenter extends Presenter<TestView> {
			TestPresenter(ApplicationContext context, TestView view) {
				super(context, view);
			}

			@Override
			public void destroy() {
				destroyed.set(true);

				super.destroy();
			}

			@Override
			public boolean cache() {
				return true;
			}
		}

		MainMockView view = new MainMockView() {
			@Override
			public void removeView(View view, ViewLayer layer) {
				assertTrue(view instanceof TestView);
				assertEquals(ViewLayer.Content, layer);

				removed.set(true);
			}
		};

		TestPresenter testPresenter = new TestPresenter(context, new TestView());

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.display(testPresenter);
		presenter.destroy(testPresenter);

		assertTrue(removed.get());
		assertFalse(destroyed.get());
	}

	@Test
	void testSetFullscreen() throws Exception {
		AtomicBoolean fullscreen = new AtomicBoolean(false);

		MainMockView view = new MainMockView() {
			@Override
			public void setFullscreen(boolean enabled) {
				fullscreen.set(enabled);
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.setFullscreen(true);

		assertTrue(fullscreen.get());

		presenter.setFullscreen(false);

		assertFalse(fullscreen.get());
	}

	@Test
	void testRestoreRecordingOnStartup() throws Exception {
		AtomicBoolean shownRestoreView = new AtomicBoolean(false);

		recordingService.start();
		recordingService.stop();

		MainMockView view = new MainMockView() {
			@Override
			public void showView(View view, ViewLayer layer) {
				assertTrue(view instanceof RestoreRecordingView);
				assertEquals(ViewLayer.Dialog, layer);

				shownRestoreView.set(true);
			}
		};

		// Simulate crash and restore with new recording service
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");

		recorder = new FileLectureRecorder(audioSystemProvider, documentService, audioConfig, getRecordingDirectory());

		recordingService = new RecordingService(context, recorder);

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();

		view.shownAction.execute();

		assertTrue(shownRestoreView.get());
	}

	@Test
	void testCloseView() throws Exception {
		AtomicBoolean viewHidden = new AtomicBoolean(false);
		CountDownLatch shutdownLatch = new CountDownLatch(1);

		MainMockView view = new MainMockView() {
			@Override
			public void hideView() {
				viewHidden.set(true);
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.addShutdownHandler(new ShutdownHandler() {

			@Override
			public boolean execute() {
				shutdownLatch.countDown();
				return true;
			}
		});

		view.closeAction.execute();

		shutdownLatch.await();

		assertTrue(viewHidden.get());
		assertEquals(0, shutdownLatch.getCount());
	}

	@Test
	void testCloseViewShortcut() throws Exception {
		AtomicBoolean removed = new AtomicBoolean(false);

		class TestView implements View { }
		class TestPresenter extends Presenter<TestView> {
			TestPresenter(ApplicationContext context, TestView view) {
				super(context, view);
			}
		}

		MainMockView view = new MainMockView() {
			@Override
			public void removeView(View view, ViewLayer layer) {
				assertTrue(view instanceof TestView);
				assertEquals(ViewLayer.Content, layer);

				removed.set(true);
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();
		presenter.display(new TestPresenter(context, new TestView()));

		view.keyAction.test(Shortcut.CLOSE_VIEW.getKeyEvent());

		assertTrue(removed.get());
	}

	@Test
	void testSaveRecordingOnShutdown() throws Exception {
		AtomicBoolean shownView = new AtomicBoolean(false);
		CountDownLatch shownLatch = new CountDownLatch(1);

		MainMockView view = new MainMockView() {
			@Override
			public void showView(View view, ViewLayer layer) {
				assertTrue(view instanceof QuitRecordingView);
				assertEquals(ViewLayer.Dialog, layer);

				shownView.set(true);
				shownLatch.countDown();
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();

		recordingService.start();

		view.closeAction.execute();

		shownLatch.await(10, TimeUnit.SECONDS);

		assertTrue(shownView.get());
		assertEquals(0, shownLatch.getCount());

		recordingService.stop();
	}

	@Test
	void testSaveDocumentsOnShutdown() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		config.setSaveDocumentOnClose(true);

		AtomicBoolean shownView = new AtomicBoolean(false);
		CountDownLatch shownLatch = new CountDownLatch(1);

		MainMockView view = new MainMockView() {
			@Override
			public void showView(View view, ViewLayer layer) {
				assertTrue(view instanceof SaveDocumentsView);
				assertEquals(ViewLayer.Content, layer);

				shownView.set(true);
				shownLatch.countDown();
			}
		};

		MainPresenter presenter = new MainPresenter(context, view, presentationController, null, viewFactory, documentService, bookmarkService, recordingService, null, null);
		presenter.initialize();

		ToolController toolController = new ToolController(context, documentService);
		toolController.selectPenTool();
		toolController.beginToolAction(new PenPoint2D(10, 10));
		toolController.executeToolAction(new PenPoint2D(50, 50));
		toolController.endToolAction(new PenPoint2D(400, 70));

		view.closeAction.execute();

		shownLatch.await(10, TimeUnit.SECONDS);

		assertTrue(shownView.get());
		assertEquals(0, shownLatch.getCount());
	}



	private static class MainMockView implements MainView {

		Action closeAction;

		Action shownAction;

		Predicate<KeyEvent> keyAction;


		@Override
		public Rectangle2D getViewBounds() {
			return null;
		}

		@Override
		public void closeView() {
			closeAction.execute();
		}

		@Override
		public void hideView() {

		}

		@Override
		public void removeView(View view, ViewLayer layer) {

		}

		@Override
		public void showView(View view, ViewLayer layer) {

		}

		@Override
		public void setFullscreen(boolean fullscreen) {

		}

		@Override
		public void setMenuVisible(boolean visible) {

		}

		@Override
		public void setOnKeyEvent(Predicate<KeyEvent> action) {
			assertNotNull(action);

			keyAction = action;
		}

		@Override
		public void setOnBounds(ConsumerAction<Rectangle2D> action) {

		}

		@Override
		public void setOnFocus(ConsumerAction<Boolean> action) {

		}

		@Override
		public void setOnShown(Action action) {
			assertNotNull(action);

			shownAction = action;
		}

		@Override
		public void setOnClose(Action action) {
			assertNotNull(action);

			closeAction = action;
		}
	}

	public static class MockWebRtcStreamService extends WebRtcStreamService {

		@Inject
		public MockWebRtcStreamService(ApplicationContext context, WebService webService, WebServiceInfo webServiceInfo, WebRtcStreamEventRecorder eventRecorder, RecordingService recordingService) throws ExecutableException {
			super(context, webService, webServiceInfo, eventRecorder, recordingService);
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