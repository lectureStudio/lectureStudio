package org.lecturestudio.editor.api.presenter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.inject.Singleton;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.view.ConfirmationNotificationView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.ConfirmationNotificationMockView;
import org.lecturestudio.editor.api.view.MediaTracksMockView;
import org.lecturestudio.editor.api.view.MediaTracksView;

public class MediaTracksPresenterTest extends PresenterTest {

	RecordingFileService recordingService;

	MediaTracksMockView view = new MediaTracksMockView();

	MediaTracksPresenter presenter;

	ConfirmationNotificationMockView confirmationNotificationView = new ConfirmationNotificationMockView();
	NotificationMockView notificationView = new NotificationMockView();


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
			MediaTracksView provideReplacePageView() {
				return view;
			}

			@Provides
			@Singleton
			ConfirmationNotificationView provideConfirmationNotificationView() {
				return confirmationNotificationView;
			}

			@Provides
			@Singleton
			NotificationView provideNotificationView() {
				return notificationView;
			}
		});

		recordingService = injector.getInstance(RecordingFileService.class);
		String recordingPath = Objects.requireNonNull(getClass().getClassLoader().getResource("empty_pages_recording.presenter")).getFile();
		recordingService.openRecording(new File(recordingPath)).get();

		presenter = injector.getInstance(MediaTracksPresenter.class);
		presenter.initialize();
	}

	@Test
	void testMovePage() {
		Recording recording = recordingService.getSelectedRecording();

		List<RecordedPage> pages = recording.getRecordedEvents().getRecordedPages();

		RecordedPage movePage = pages.get(1);

		int pageTimeBeforeMove = movePage.getTimestamp();
		long durationBeforeMove = recording.getRecordingHeader().getDuration();
		int audioLengthBeforeMove = recording.getRecordingHeader().getAudioLength();
		int pageTimeAfterMove = (pageTimeBeforeMove + pages.get(2).getTimestamp()) / 2;
		Integer[] pageTimesBeforeMove = pages.stream().filter(page -> page.getNumber() != 1).map(RecordedPage::getTimestamp).toArray(Integer[]::new);


		movePage.setTimestamp(pageTimeAfterMove);
		view.setOnMovePageAction.execute(movePage);

		movePage = recording.getRecordedEvents().getRecordedPage(1);
		Integer[] pageTimesAfterMove = recording.getRecordedEvents().getRecordedPages().stream().filter(page -> page.getNumber() != 1).map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		assertNotEquals(pageTimeBeforeMove, pageTimeAfterMove);
		assertEquals(pageTimeAfterMove, movePage.getTimestamp());
		assertEquals(durationBeforeMove, recording.getRecordingHeader().getDuration());
		assertEquals(audioLengthBeforeMove, recording.getRecordingHeader().getAudioLength());
		assertArrayEquals(pageTimesBeforeMove, pageTimesAfterMove);

	}

	/**
	 * Tests whether the HidePageAction gets executed successfully and that nothing else gets modified further.
	 * Also tests the ConfirmationNotification view and presenter, including the confirmation action and all texts.
	 *
	 * @throws InterruptedException
	 */
	@Test
	void testConfirmHidePage() throws InterruptedException {
		CountDownLatch confirmLatch = new CountDownLatch(1);
		Recording recording = recordingService.getSelectedRecording();

		List<RecordedPage> pages = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesBeforeHide = pages.stream().filter(page -> page.getNumber() != 1).map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		RecordedPage hidePage = pages.get(1);

		int numPagesBeforeHide = pages.size();
		long durationBeforeHide = recording.getRecordingHeader().getDuration();
		int audioLengthBeforeHide = recording.getRecordingHeader().getAudioLength();

		view.setOnHidePageAction.execute(hidePage);
		CompletableFuture.runAsync(() -> {
			while (confirmationNotificationView.setOnConfirmAction == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			confirmLatch.countDown();
		});

		assertTrue(confirmLatch.await(10, TimeUnit.SECONDS));

		assertEquals(dict.get("hide.page.notification.title"), confirmationNotificationView.setTitleString);
		assertEquals(dict.get("hide.page.notification.text"), confirmationNotificationView.setMessageString);
		assertEquals(dict.get("hide.page.notification.confirm"), confirmationNotificationView.setConfirmButtonTextString);
		assertEquals(dict.get("hide.page.notification.close"), confirmationNotificationView.setDiscardButtonTextString);
		assertEquals(NotificationType.QUESTION, confirmationNotificationView.setTypeType);

		confirmationNotificationView.setOnConfirmAction.execute();

		CountDownLatch pagesReducedLatch = new CountDownLatch(1);

		CompletableFuture.runAsync(() -> {
			while (recording.getRecordedEvents().getRecordedPages().size() == numPagesBeforeHide) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				pagesReducedLatch.countDown();
			}
		});

		assertTrue(pagesReducedLatch.await(10, TimeUnit.SECONDS));

		List<RecordedPage> pagesAfterHide = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesAfterHide = pagesAfterHide.stream().map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		assertEquals(numPagesBeforeHide - 1, pagesAfterHide.size());

		for (RecordedPage page : pagesAfterHide) {
			assertTrue(page.getNumber() >= 0);
			assertTrue(page.getNumber() < pagesAfterHide.size());
		}

		assertEquals(durationBeforeHide, recording.getRecordingHeader().getDuration());
		assertEquals(audioLengthBeforeHide, recording.getRecordingHeader().getAudioLength());
		assertArrayEquals(pageTimesBeforeHide, pageTimesAfterHide);
	}

	/**
	 * Tests whether the HidePageAction gets executed successfully and that nothing else gets modified further.
	 * Also tests the ConfirmationNotification view and presenter, including the confirmation action and all texts.
	 *
	 * @throws InterruptedException
	 */
	@Test
	void testAbortHidePage() throws InterruptedException {
		CountDownLatch confirmLatch = new CountDownLatch(1);
		Recording recording = recordingService.getSelectedRecording();

		List<RecordedPage> pages = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesBeforeHide = pages.stream().map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		RecordedPage hidePage = pages.get(1);

		int numPagesBeforeHide = pages.size();
		long durationBeforeHide = recording.getRecordingHeader().getDuration();
		int audioLengthBeforeHide = recording.getRecordingHeader().getAudioLength();

		view.setOnHidePageAction.execute(hidePage);
		CompletableFuture.runAsync(() -> {
			while (confirmationNotificationView.setOnConfirmAction == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			confirmLatch.countDown();
		});

		assertTrue(confirmLatch.await(10, TimeUnit.SECONDS));

		assertEquals(dict.get("hide.page.notification.title"), confirmationNotificationView.setTitleString);
		assertEquals(dict.get("hide.page.notification.text"), confirmationNotificationView.setMessageString);
		assertEquals(dict.get("hide.page.notification.confirm"), confirmationNotificationView.setConfirmButtonTextString);
		assertEquals(dict.get("hide.page.notification.close"), confirmationNotificationView.setDiscardButtonTextString);
		assertEquals(NotificationType.QUESTION, confirmationNotificationView.setTypeType);

		confirmationNotificationView.setOnDiscardAction.execute();

		List<RecordedPage> pagesAfterHide = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesAfterHide = pagesAfterHide.stream().map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		assertEquals(numPagesBeforeHide, pagesAfterHide.size());

		for (int i = 0; i < pages.size(); i++) {
			RecordedPage before = pages.get(i);
			RecordedPage after = pagesAfterHide.get(i);

			assertEquals(before.getNumber(), after.getNumber());
			assertEquals(before.getTimestamp(), after.getTimestamp());
			assertEquals(before.getPlaybackActions(), after.getPlaybackActions());
			assertEquals(before.getStaticActions(), after.getStaticActions());
		}

		assertEquals(durationBeforeHide, recording.getRecordingHeader().getDuration());
		assertEquals(audioLengthBeforeHide, recording.getRecordingHeader().getAudioLength());
		assertArrayEquals(pageTimesBeforeHide, pageTimesAfterHide);
	}

	/**
	 * Tests whether a notification gets shown, when the new duration of a page is less than a specified amount.
	 *
	 * @throws InterruptedException
	 */
	@Test
	void testShowMovePageNotification() throws InterruptedException {
		CountDownLatch confirmLatch = new CountDownLatch(1);
		Recording recording = recordingService.getSelectedRecording();

		List<RecordedPage> pages = recording.getRecordedEvents().getRecordedPages();

		RecordedPage movePage = pages.get(1);

		int pageTimeBeforeMove = movePage.getTimestamp();
		long durationBeforeMove = recording.getRecordingHeader().getDuration();
		int audioLengthBeforeMove = recording.getRecordingHeader().getAudioLength();
		int pageTimeAfterMove = (pages.get(2).getTimestamp() - 1);
		Integer[] pageTimesBeforeMove = pages.stream().filter(page -> page.getNumber() != 1).map(RecordedPage::getTimestamp).toArray(Integer[]::new);
		notificationView.setTitle(null);
		notificationView.setMessage(null);
		notificationView.setType(null);

		movePage.setTimestamp(pageTimeAfterMove);

		view.setOnMovePageAction.execute(movePage);

		CompletableFuture.runAsync(() -> {
			while (notificationView.title == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			confirmLatch.countDown();
		});

		movePage = recording.getRecordedEvents().getRecordedPage(1);
		Integer[] pageTimesAfterMove = recording.getRecordedEvents().getRecordedPages().stream().filter(page -> page.getNumber() != 1).map(RecordedPage::getTimestamp).toArray(Integer[]::new);


		assertTrue(confirmLatch.await(10, TimeUnit.SECONDS));

		assertNotEquals(pageTimeBeforeMove, pageTimeAfterMove);
		assertEquals(pageTimeAfterMove, movePage.getTimestamp());
		assertEquals(durationBeforeMove, recording.getRecordingHeader().getDuration());
		assertEquals(audioLengthBeforeMove, recording.getRecordingHeader().getAudioLength());
		assertArrayEquals(pageTimesBeforeMove, pageTimesAfterMove);
		assertEquals(dict.get("move.page.duration.low.title"), notificationView.title);
		assertEquals(dict.get("move.page.duration.low.text"), notificationView.message);
		assertEquals(NotificationType.DEFAULT, notificationView.type);
	}


	/**
	 * Tests whether the HidePageAction gets executed successfully and that nothing else gets modified further.
	 * Also tests the ConfirmationNotification view and presenter, including the confirmation action and all texts.
	 *
	 * @throws InterruptedException
	 */
	@Test
	void testConfirmHideAndMoveNextPage() throws InterruptedException {
		CountDownLatch confirmLatch = new CountDownLatch(1);
		Recording recording = recordingService.getSelectedRecording();

		List<RecordedPage> pages = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesBeforeHide = pages.stream().filter(page -> page.getNumber() != 2).map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		RecordedPage hidePage = recording.getRecordedEvents().getRecordedPage(1);

		int numPagesBeforeHide = pages.size();
		int timestampHiddenPage = recording.getRecordedEvents().getRecordedPage(1).getTimestamp();
		long durationBeforeHide = recording.getRecordingHeader().getDuration();
		int audioLengthBeforeHide = recording.getRecordingHeader().getAudioLength();

		view.setOnHideAndMoveNextPageAction.execute(hidePage);
		CompletableFuture.runAsync(() -> {
			while (confirmationNotificationView.setOnConfirmAction == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			confirmLatch.countDown();
		});

		assertTrue(confirmLatch.await(10, TimeUnit.SECONDS));

		assertEquals(dict.get("hide.page.notification.title"), confirmationNotificationView.setTitleString);
		assertEquals(dict.get("hide.page.notification.text"), confirmationNotificationView.setMessageString);
		assertEquals(dict.get("hide.page.notification.confirm"), confirmationNotificationView.setConfirmButtonTextString);
		assertEquals(dict.get("hide.page.notification.close"), confirmationNotificationView.setDiscardButtonTextString);
		assertEquals(NotificationType.QUESTION, confirmationNotificationView.setTypeType);

		confirmationNotificationView.setOnConfirmAction.execute();

		CountDownLatch pagesReducedLatch = new CountDownLatch(1);

		CompletableFuture.runAsync(() -> {
			while (recording.getRecordedEvents().getRecordedPages().size() == numPagesBeforeHide) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				pagesReducedLatch.countDown();
			}
		});

		assertTrue(pagesReducedLatch.await(10, TimeUnit.SECONDS));
		List<RecordedPage> pagesAfterHide = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesAfterHide = pagesAfterHide.stream().map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		assertEquals(numPagesBeforeHide - 1, pagesAfterHide.size());

		for (RecordedPage page : pagesAfterHide) {
			assertTrue(page.getNumber() >= 0);
			assertTrue(page.getNumber() < pagesAfterHide.size());
		}

		assertEquals(timestampHiddenPage, recording.getRecordedEvents().getRecordedPage(1).getTimestamp());
		assertEquals(durationBeforeHide, recording.getRecordingHeader().getDuration());
		assertEquals(audioLengthBeforeHide, recording.getRecordingHeader().getAudioLength());
		assertArrayEquals(pageTimesBeforeHide, pageTimesAfterHide);
	}

	/**
	 * Tests whether the HidePageAction gets executed successfully and that nothing else gets modified further.
	 * Also tests the ConfirmationNotification view and presenter, including the confirmation action and all texts.
	 *
	 * @throws InterruptedException
	 */
	@Test
	void testAbortHideAndMoveNextPage() throws InterruptedException {
		CountDownLatch confirmLatch = new CountDownLatch(1);
		Recording recording = recordingService.getSelectedRecording();

		List<RecordedPage> pages = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesBeforeHide = pages.stream().map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		RecordedPage hidePage = pages.get(1);

		int numPagesBeforeHide = pages.size();
		long durationBeforeHide = recording.getRecordingHeader().getDuration();
		int audioLengthBeforeHide = recording.getRecordingHeader().getAudioLength();

		view.setOnHideAndMoveNextPageAction.execute(hidePage);
		CompletableFuture.runAsync(() -> {
			while (confirmationNotificationView.setOnConfirmAction == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			confirmLatch.countDown();
		});

		assertTrue(confirmLatch.await(10, TimeUnit.SECONDS));

		assertEquals(dict.get("hide.page.notification.title"), confirmationNotificationView.setTitleString);
		assertEquals(dict.get("hide.page.notification.text"), confirmationNotificationView.setMessageString);
		assertEquals(dict.get("hide.page.notification.confirm"), confirmationNotificationView.setConfirmButtonTextString);
		assertEquals(dict.get("hide.page.notification.close"), confirmationNotificationView.setDiscardButtonTextString);
		assertEquals(NotificationType.QUESTION, confirmationNotificationView.setTypeType);

		confirmationNotificationView.setOnDiscardAction.execute();

		List<RecordedPage> pagesAfterHide = recording.getRecordedEvents().getRecordedPages();
		Integer[] pageTimesAfterHide = pagesAfterHide.stream().map(RecordedPage::getTimestamp).toArray(Integer[]::new);

		assertEquals(numPagesBeforeHide, pagesAfterHide.size());

		for (int i = 0; i < pages.size(); i++) {
			RecordedPage before = pages.get(i);
			RecordedPage after = pagesAfterHide.get(i);

			assertEquals(before.getNumber(), after.getNumber());
			assertEquals(before.getTimestamp(), after.getTimestamp());
			assertEquals(before.getPlaybackActions(), after.getPlaybackActions());
			assertEquals(before.getStaticActions(), after.getStaticActions());
		}

		assertEquals(durationBeforeHide, recording.getRecordingHeader().getDuration());
		assertEquals(audioLengthBeforeHide, recording.getRecordingHeader().getAudioLength());
		assertArrayEquals(pageTimesBeforeHide, pageTimesAfterHide);
	}
}