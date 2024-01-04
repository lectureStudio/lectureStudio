package org.lecturestudio.editor.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.file.IncompatibleFileFormatException;
import org.lecturestudio.core.recording.file.RecordingFileReader;

public class RecordingFileServiceTest extends ServiceTest {
	RecordingFileService recordingFileService;

	@Override
	@BeforeEach
	protected void setupInjector() throws Exception {
		injector = new GuiceInjector(new AbstractModule() {

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

		});

		recordingFileService = injector.getInstance(RecordingFileService.class);
		String recordingPath = getResourcePath("empty_pages_recording.presenter").toString();
		recordingFileService.openRecording(new File(recordingPath)).get();
	}

	@Test
	public void testSplitAndSaveRecording() throws RecordingEditException, InterruptedException,
			IOException, IncompatibleFileFormatException {

		Recording recording = recordingFileService.getSelectedRecording();
		File newFile = testPath.resolve(recording.getSourceFile().getName()).toFile();
		Interval<Long> interval = new Interval<>(0L, recording.getRecordingHeader().getDuration() / 2);
		long durationBeforeCut = recording.getRecordingHeader().getDuration();

		AtomicReference<Float> progressRef = new AtomicReference<>((float) 0);
		recordingFileService.savePartialRecording(newFile, interval, progressRef::set);
		awaitTrue(() -> progressRef.get() >= 1f, 10);

		Recording newRecording = RecordingFileReader.read(new File(testPath.toString(), newFile.getName()));
		assertEquals(interval.lengthLong(), newRecording.getRecordingHeader().getDuration());

		awaitTrue(() -> recording.getRecordingHeader().getDuration() == durationBeforeCut - interval.lengthLong(), 10);
		awaitTrue(() -> recording.getEditManager().hasUndoActions(), 10);

		recordingFileService.undoChanges();

		awaitTrue(() -> recording.getRecordingHeader().getDuration() == durationBeforeCut, 10);
		awaitTrue(() -> recording.getEditManager().hasRedoActions(), 10);

	}
}
