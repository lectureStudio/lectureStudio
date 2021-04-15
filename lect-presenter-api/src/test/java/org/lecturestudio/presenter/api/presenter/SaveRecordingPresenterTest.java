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

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.recording.RecordingBackup;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.SaveRecordingView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaveRecordingPresenterTest extends PresenterTest {

	private AtomicReference<FileChooserMockView> chooserRef;

	private DocumentService documentService;

	private RecordingService recordingService;

	private String defaultSavePath;

	private String defaultFile;

	private Path selectedFile;


	@BeforeEach
	void setup() throws IOException {
		chooserRef = new AtomicReference<>();

		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setInputDeviceName("dummy");
		audioConfig.setSoundSystem("dummy");

		documentService = context.getDocumentService();

		FileLectureRecorder recorder = new FileLectureRecorder(documentService, audioConfig, context.getRecordingDirectory());

		recordingService = new RecordingService(context, recorder);

		defaultSavePath = getRecordingPath();
		defaultFile = getRecordingName();
		selectedFile = testPath.resolve("test.presenter");

		viewFactory = new ViewContextMockFactory() {
			@Override
			public FileChooserView createFileChooserView() {
				FileChooserMockView view = new FileChooserMockView() {
					@Override
					public File showSaveFile(View parent) {
						super.showSaveFile(parent);
						return selectedFile.toFile();
					}
				};
				chooserRef.set(view);
				return view;
			}
		};
	}

	@AfterEach
	void dispose() throws IOException {
		Files.deleteIfExists(selectedFile);
	}

	@Test
	void testInit() {
		AtomicReference<File> pathRef = new AtomicReference<>();

		SaveRecordingMockView view = new SaveRecordingMockView() {
			@Override
			public void setDestinationFile(File file) {
				pathRef.set(file);
			}
		};

		SaveRecordingPresenter presenter = new SaveRecordingPresenter(context, view, viewFactory, documentService, recordingService);
		presenter.initialize();

		view.viewShownAction.execute();

		assertEquals("Presenter Recordings", chooserRef.get().description);
		assertArrayEquals(new String[] { "*.presenter" }, chooserRef.get().extensions);
		assertEquals(defaultFile, chooserRef.get().initialFileName);
		assertEquals(new File(defaultSavePath), chooserRef.get().directory);
		assertEquals(selectedFile.toFile(), pathRef.get());
	}

	@Test
	void testSaveError() throws InterruptedException {
		AtomicReference<String> errorRef = new AtomicReference<>();
		CountDownLatch saveLatch = new CountDownLatch(1);

		SaveRecordingMockView view = new SaveRecordingMockView() {
			@Override
			public void setError(String message) {
				errorRef.set(message);
				saveLatch.countDown();
			}
		};

		SaveRecordingPresenter presenter = new SaveRecordingPresenter(context, view, viewFactory, documentService, recordingService);
		presenter.initialize();

		view.viewShownAction.execute();

		saveLatch.await();

		assertEquals("recording.save.error", errorRef.get());
	}

	@Test
	void testDiscard() throws ExecutableException, IOException {
		AtomicBoolean aborted = new AtomicBoolean(false);

		viewFactory = new ViewContextMockFactory() {
			@Override
			public FileChooserView createFileChooserView() {
				FileChooserMockView view = new FileChooserMockView() {
					@Override
					public File showSaveFile(View parent) {
						return null;
					}
				};
				return view;
			}
		};

		SaveRecordingMockView view = new SaveRecordingMockView();

		SaveRecordingPresenter presenter = new SaveRecordingPresenter(context, view, viewFactory, documentService, recordingService);
		presenter.initialize();
		presenter.setOnAbort(() -> aborted.set(true));

		recordingService.start();
		recordingService.stop();

		view.viewShownAction.execute();

		RecordingBackup backup = new RecordingBackup(context.getRecordingDirectory());

		assertTrue(aborted.get());
		assertFalse(backup.hasCheckpoint());
	}

	@Test
	void testSave() throws ExecutableException, InterruptedException, IOException {
		AtomicBoolean success = new AtomicBoolean(false);
		AtomicBoolean gotProgress = new AtomicBoolean(false);
		CountDownLatch saveLatch = new CountDownLatch(1);

		SaveRecordingMockView view = new SaveRecordingMockView() {
			@Override
			public void setProgress(double progress) {
				gotProgress.set(progress > 0);
			}

			@Override
			public void setSuccess() {
				success.set(true);
				saveLatch.countDown();
			}
		};

		SaveRecordingPresenter presenter = new SaveRecordingPresenter(context, view, viewFactory, documentService, recordingService);
		presenter.initialize();

		recordingService.start();
		recordingService.stop();

		view.viewShownAction.execute();

		saveLatch.await();

		assertTrue(gotProgress.get());
		assertTrue(success.get());

		FileChannel recFileChannel = FileChannel.open(selectedFile);

		assertTrue(recFileChannel.size() > 0);
	}

	private String getRecordingName() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		Document doc = documentService.getDocuments().getSelectedDocument();
		String date = dateFormat.format(new Date());

		return doc.getName() + "-" + date + "." + PresenterContext.RECORDING_EXTENSION;
	}

	private String getRecordingPath() {
		String recordingPath = context.getConfiguration().getAudioConfig().getRecordingPath();

		if (isNull(recordingPath) || recordingPath.isEmpty()) {
			recordingPath = System.getProperty("user.home");
		}

		return recordingPath;
	}



	private static class SaveRecordingMockView implements SaveRecordingView {

		Action viewShownAction;


		@Override
		public void setDestinationFile(File file) {

		}

		@Override
		public void setError(String message) {

		}

		@Override
		public void setSuccess() {

		}

		@Override
		public void setProgress(double progress) {

		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnViewShown(Action action) {
			viewShownAction = action;
		}
	}
}
