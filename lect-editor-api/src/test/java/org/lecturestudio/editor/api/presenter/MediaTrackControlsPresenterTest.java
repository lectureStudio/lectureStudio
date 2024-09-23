package org.lecturestudio.editor.api.presenter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.file.IncompatibleFileFormatException;
import org.lecturestudio.core.recording.file.RecordingFileReader;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.MediaControlsMockView;
import org.lecturestudio.editor.api.view.MediaControlsView;
import org.lecturestudio.editor.api.view.MediaTrackControlsMockView;
import org.lecturestudio.editor.api.view.MediaTrackControlsView;
import org.lecturestudio.editor.api.view.MediaTracksMockView;
import org.lecturestudio.editor.api.view.MediaTracksView;
import org.lecturestudio.editor.api.view.ProgressDialogMockView;
import org.lecturestudio.editor.api.view.SplitRecordingMockView;
import org.lecturestudio.editor.api.view.SplitRecordingView;

public class MediaTrackControlsPresenterTest extends PresenterTest {

	RecordingFileService recordingService;
	MediaTracksPresenter mediaTracksPresenter;
	MediaControlsPresenter mediaControlsPresenter;
	MediaTrackControlsPresenter mediaTrackControlsPresenter;
	MediaTracksMockView mediaTracksMockView;
	MediaControlsMockView mediaControlsMockView;
	MediaTrackControlsMockView mediaTrackControlsMockView;
	SplitRecordingMockView splitRecordingMockView;
	FileChooserMockView fileChooserMockView;
	ProgressDialogMockView progressDialogMockView;


	@BeforeEach
	@Override
	void setupInjector() throws ExecutionException, InterruptedException, URISyntaxException {
		mediaTracksMockView = new MediaTracksMockView();
		mediaControlsMockView = new MediaControlsMockView();
		mediaTrackControlsMockView = new MediaTrackControlsMockView();
		splitRecordingMockView = new SplitRecordingMockView();
		fileChooserMockView = new FileChooserMockView();
		progressDialogMockView = new ProgressDialogMockView();
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
				return mediaTracksMockView;
			}

			@Provides
			@Singleton
			MediaTrackControlsView provideMediaTrackControlsView() {
				return mediaTrackControlsMockView;
			}

			@Provides
			@Singleton
			SplitRecordingView provideSplitRecordingView() {
				return splitRecordingMockView;
			}

			@Provides
			@Singleton
			MediaControlsView provideMediaControlsMockView() {
				return mediaControlsMockView;
			}

			@Provides
			@Singleton
			FileChooserView provideFileChooser() {
				return fileChooserMockView;
			}
		});

		recordingService = injector.getInstance(RecordingFileService.class);
		String recordingPath = getResourcePath("empty_pages_recording.presenter").toString();
		recordingService.openRecording(new File(recordingPath)).get();

		mediaControlsPresenter = injector.getInstance(MediaControlsPresenter.class);
		mediaControlsPresenter.initialize();

		mediaTracksPresenter = injector.getInstance(MediaTracksPresenter.class);
		mediaTracksPresenter.initialize();

		mediaTrackControlsPresenter = injector.getInstance(MediaTrackControlsPresenter.class);
		mediaTrackControlsPresenter.initialize();

		context.getConfiguration().getContextPaths().put(EditorContext.RECORDING_CONTEXT, testPath.toString());
	}

	@Test
	public void confirmSavePartialRecordingTest() throws InterruptedException, IOException, IncompatibleFileFormatException {
		long lengthBeforeSplit = recordingService.getSelectedRecording().getRecordingHeader().getDuration();

		// Wait until finished initializing
		assertTrue(awaitTrue(() -> mediaTracksMockView.bindPrimarySelectionProperty != null, 10));

		// Checking that the button is deactivated if it is set to 0 (the initial state)
		if (mediaTracksMockView.bindPrimarySelectionProperty.get() == 0 || mediaTracksMockView.bindPrimarySelectionProperty.get() == 1) {
			assertFalse(mediaTrackControlsMockView.canSplitAndSaveRecording.get());
		}
		else {
			assertTrue(mediaTrackControlsMockView.canSplitAndSaveRecording.get());
		}

		mediaTracksMockView.bindPrimarySelectionProperty.set(0.2);

		// Waiting until change took place
		assertTrue(awaitTrue(() -> mediaTrackControlsMockView.canSplitAndSaveRecording.get(), 10));

		assertTrue(mediaTrackControlsMockView.canSplitAndSaveRecording.get());

		mediaTrackControlsMockView.onSplitAndSaveRecordingAction.execute();

		// Waiting until view got initialized
		assertTrue(awaitTrue(() -> splitRecordingMockView.onSubmitAction != null, 10));

		int lengthOfNewRecording = splitRecordingMockView.beginning.lengthInt();

		splitRecordingMockView.onSubmitAction.execute(splitRecordingMockView.beginning);

		String title = FileUtils.stripExtension(recordingService.getSelectedRecording().getSourceFile().getName());
		String fileName = title + "-part-1.presenter";

		// Checking whether the name and directory is set correctly
		assertEquals(context.getDictionary().get("file.description.recording"), fileChooserMockView.description);
		assertArrayEquals(new String[]{"presenter"}, fileChooserMockView.extensions);
		assertEquals(fileName, fileChooserMockView.initialFileName);

		// Waiting until the save of the new recording finished
		assertTrue(awaitTrue(() -> lengthBeforeSplit != recordingService.getSelectedRecording().getRecordingHeader().getDuration(), 10));

		// Waiting until the current recording was cut successfully
		assertTrue(awaitTrue(() -> recordingService.getSelectedRecording().getEditManager().hasUndoActions(), 10));

		assertEquals(lengthBeforeSplit - lengthOfNewRecording, recordingService.getSelectedRecording().getRecordingHeader().getDuration());


		// Check whether the new recording has the correct length
		Recording newRecording = RecordingFileReader.read(new File(testPath.toString(), fileName));
		assertEquals(lengthOfNewRecording, newRecording.getRecordingHeader().getDuration());
	}


	@Test
	public void abortSavePartialRecordingTest() throws InterruptedException {
		long lengthBeforeSplit = recordingService.getSelectedRecording().getRecordingHeader().getDuration();

		// Wait until finished initializing
		assertTrue(awaitTrue(() -> mediaTracksMockView.bindPrimarySelectionProperty != null, 10));

		// Checking that the button is deactivated if it is set to 0 (the initial state)
		if (mediaTracksMockView.bindPrimarySelectionProperty.get() == 0 || mediaTracksMockView.bindPrimarySelectionProperty.get() == 1) {
			assertFalse(mediaTrackControlsMockView.canSplitAndSaveRecording.get());
		}
		else {
			assertTrue(mediaTrackControlsMockView.canSplitAndSaveRecording.get());
		}

		mediaTracksMockView.bindPrimarySelectionProperty.set(0.2);

		// Waiting until change took place
		assertTrue(awaitTrue(() -> mediaTrackControlsMockView.canSplitAndSaveRecording.get(), 10));

		assertTrue(mediaTrackControlsMockView.canSplitAndSaveRecording.get());

		mediaTrackControlsMockView.onSplitAndSaveRecordingAction.execute();

		// Waiting until view got initialized
		assertTrue(awaitTrue(() -> splitRecordingMockView.onSubmitAction != null, 10));

		splitRecordingMockView.onCloseAction.execute();

		// Waiting to see that nothing, except for closing the view, happened
		assertFalse(awaitTrue(() -> lengthBeforeSplit != recordingService.getSelectedRecording().getRecordingHeader().getDuration(), 4));
		assertFalse(awaitTrue(() -> recordingService.getSelectedRecording().getEditManager().hasUndoActions(), 4));

		assertEquals(lengthBeforeSplit, recordingService.getSelectedRecording().getRecordingHeader().getDuration());
	}
}
