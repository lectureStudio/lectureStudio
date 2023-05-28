package org.lecturestudio.editor.api.presenter;

import javax.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.util.ReplacePageType;
import org.lecturestudio.editor.api.view.ReplacePageMockView;
import org.lecturestudio.editor.api.view.ReplacePageView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReplacePagePresenterTest extends PresenterTest {

	RecordingFileService recordingService;

	ReplacePageMockView view = new ReplacePageMockView();

	ReplacePagePresenter presenter;

	Document newDocument;


	@BeforeEach
	@Override
	void setupInjector() throws ExecutionException, InterruptedException {
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
			ReplacePageView provideReplacePageView() {
				return view;
			}
		});

		recordingService = injector.getInstance(RecordingFileService.class);
		String recordingPath = Objects.requireNonNull(getClass().getClassLoader().getResource("empty_pages_recording.presenter")).getFile();
		recordingService.openRecording(new File(recordingPath)).get();

		presenter = injector.getInstance(ReplacePagePresenter.class);

		String docPath = Objects.requireNonNull(getClass().getClassLoader().getResource("written_pages.pdf")).getFile();
		newDocument = context.getDocumentService().openDocument(new File(docPath)).get();

		presenter.initialize();
		presenter.setNewDocument(newDocument);
	}

	@Test
	void testReplaceAllPages() throws InterruptedException {
		int expectedPageCount = newDocument.getPageCount();
		List<Integer> actualReplacedPages = replacePages(ReplacePageType.REPLACE_ALL_PAGES.getName(), newDocument, true);
		assertFalse(view.setDisableAllPagesTypeRadioBoolean);

		assertEquals(expectedPageCount, actualReplacedPages.size());
	}

	List<Integer> replacePages(String pageReplaceType, Document replacingDocument, boolean confirm) throws InterruptedException {
		CountDownLatch doneLatch = new CountDownLatch(1);
		replacingDocument.getPages().forEach((page -> page.setUid(UUID.randomUUID())));

		view.setOnReplaceTypeChangeAction.execute(pageReplaceType);

		CompletableFuture.runAsync(() -> {
			view.setOnReplaceAction.execute();
			do {
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			} while (!view.doneBoolean);

			view.doneBoolean = false;

			doneLatch.countDown();
		});

		assertTrue(doneLatch.await(10, TimeUnit.SECONDS));

		if (confirm) {
			CountDownLatch confirmLatch = new CountDownLatch(1);
			presenter.setOnClose(confirmLatch::countDown);
			view.setOnConfirmAction.execute();
			assertTrue(confirmLatch.await(1, TimeUnit.SECONDS));

			Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();

			List<Integer> replacedPages = new ArrayList<>();

			for (int i = 0; i < document.getPageCount(); i++) {
				if (!document.getPage(i).getPageText().isBlank()) {
					replacedPages.add(i);
				}
			}

			return replacedPages;
		}
		return List.of();
	}

	@Test
	void testReplaceSelectedPage() throws InterruptedException {
		List<Integer> actualReplacedPages = replacePages(ReplacePageType.REPLACE_SINGLE_PAGE.getName(), newDocument, true);

		assertEquals(1, actualReplacedPages.size());
	}

	@Test
	void testSetOnNextPrevPageNewDoc() throws InterruptedException {
		CountDownLatch pageSetLatch = new CountDownLatch(1);
		view.setNewPagePage = null;
		view.setOnNextPageNewDocAction.execute();

		CountDownLatch finalPageSetLatch = pageSetLatch;
		CompletableFuture.runAsync(() -> {
			while (view.setNewPagePage == null) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			finalPageSetLatch.countDown();
		});

		assertTrue(pageSetLatch.await(10, TimeUnit.SECONDS));
		assertEquals(1, view.setNewPagePage.getPageNumber());

		pageSetLatch = new CountDownLatch(1);
		view.setNewPagePage = null;

		view.setOnPreviousPageNewDocAction.execute();

		CountDownLatch finalPageSetLatch1 = pageSetLatch;
		CompletableFuture.runAsync(() -> {
			while (view.setNewPagePage == null) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			finalPageSetLatch1.countDown();
		});

		assertTrue(pageSetLatch.await(10, TimeUnit.SECONDS));
		assertEquals(0, view.setNewPagePage.getPageNumber());
	}

	@Test
	void testSetOnPageNumberNewDoc() throws InterruptedException {
		CountDownLatch pageSetLatch = new CountDownLatch(1);
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();

		view.setCurrentPagePage = null;

		view.setOnPageNumberCurrentDocAction.execute(document.getPageCount() - 1);

		CompletableFuture.runAsync(() -> {
			while (view.setCurrentPagePage == null) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			pageSetLatch.countDown();
		});

		assertTrue(pageSetLatch.await(10, TimeUnit.SECONDS));

		assertEquals(document.getPageCount() - 1, view.setCurrentPagePage.getPageNumber());
	}

	@Test
	void testSetOnNextPreviousPageCurrentDoc() throws InterruptedException {
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();
		CountDownLatch pageSetLatch = new CountDownLatch(1);

		assertTrue(document.getPageCount() > 1);
		view.setCurrentPagePage = null;
		view.setOnNextPageCurrentDocAction.execute();

		CountDownLatch finalPageSetLatch = pageSetLatch;
		CompletableFuture.runAsync(() -> {
			while (view.setCurrentPagePage == null) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			finalPageSetLatch.countDown();
		});

		assertTrue(pageSetLatch.await(10, TimeUnit.SECONDS));

		assertEquals(1, view.setCurrentPagePage.getPageNumber());

		pageSetLatch = new CountDownLatch(1);
		view.setCurrentPagePage = null;

		view.setOnPreviousPageCurrentDocAction.execute();

		CountDownLatch finalPageSetLatch1 = pageSetLatch;
		CompletableFuture.runAsync(() -> {
			while (view.setCurrentPagePage == null) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			finalPageSetLatch1.countDown();
		});

		assertTrue(pageSetLatch.await(10, TimeUnit.SECONDS));

		assertEquals(0, view.setCurrentPagePage.getPageNumber());
	}

	@Test
	void testSetOnPageNumberCurrentDoc() throws InterruptedException {
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();
		CountDownLatch pageSetLatch = new CountDownLatch(1);
		view.setCurrentPagePage = null;

		view.setOnPageNumberCurrentDocAction.execute(document.getPageCount() - 1);

		CompletableFuture.runAsync(() -> {
			while (view.setCurrentPagePage == null) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			pageSetLatch.countDown();
		});

		assertTrue(pageSetLatch.await(10, TimeUnit.SECONDS));

		assertEquals(document.getPageCount() - 1, view.setCurrentPagePage.getPageNumber());
	}

	@Test
	void testSetDisableAllPagesTypeRadio() {
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();
		if (document.getPageCount() == newDocument.getPageCount()) {
			assertFalse(view.setDisableAllPagesTypeRadioBoolean);
		}
		else {
			assertTrue(view.setDisableAllPagesTypeRadioBoolean);
		}
	}

	@Test
	void testSetTotalPagesNewDocLabel() {
		assertEquals(view.setTotalPagesNewDocLabelInt, newDocument.getPageCount());
	}

	@Test
	void testSetTotalPagesCurrentDocLabel() {
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();

		assertEquals(view.setTotalPagesNewDocLabelInt, document.getPageCount());
	}

	@Test
	void testConfirmAndCloseView() throws InterruptedException {
		AtomicBoolean close = new AtomicBoolean(false);
		CountDownLatch closeLatch = new CountDownLatch(1);
		presenter.setOnClose(() -> {
			close.set(true);
			closeLatch.countDown();
		});
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();

		int end = document.getPageCount();
		for (int i = 0; i < end - 1; i++) {
			replacePages(ReplacePageType.REPLACE_SINGLE_PAGE.getName(), newDocument, false);

			assertFalse(close.get());
		}
		replacePages(ReplacePageType.REPLACE_SINGLE_PAGE.getName(), newDocument, true);

		assertTrue(close.get());
	}

	@Test
	void testAbortView() throws InterruptedException {
		AtomicBoolean close = new AtomicBoolean(false);
		CountDownLatch closeLatch = new CountDownLatch(1);
		presenter.setOnClose(() -> {
			close.set(true);
			closeLatch.countDown();
		});
		Document document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();

		int end = document.getPageCount();
		for (int i = 0; i < end; i++) {
			replacePages(ReplacePageType.REPLACE_SINGLE_PAGE.getName(), newDocument, false);

			assertFalse(close.get());
		}
		view.setOnAbortAction.execute();

		assertTrue(closeLatch.await(2, TimeUnit.SECONDS));
		assertTrue(close.get());

		document = recordingService.getSelectedRecording().getRecordedDocument().getDocument();

		for (int i = 0; i < document.getPageCount(); i++) {
			assertTrue(document.getPage(i).getPageText().isBlank());
		}
	}

}
