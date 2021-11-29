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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.recording.RecordingBackup;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.RestoreRecordingView;

import org.junit.jupiter.api.*;

class RestoreRecordingPresenterTest extends PresenterTest {

	private RecordingService recordingService;


	@BeforeEach
	void setUp() throws IOException, ExecutableException {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");

		DocumentService documentService = context.getDocumentService();
		FileLectureRecorder recorder = new FileLectureRecorder(audioSystemProvider, documentService, audioConfig, context.getRecordingDirectory());

		recordingService = new RecordingService(context, recorder);
		recordingService.start();
		recordingService.stop();
	}

	@AfterEach
	void tearDown() throws ExecutableException {
		if (!recordingService.destroyed()) {
			recordingService.destroy();
		}
	}

	@Test
	void testDiscard() throws IOException {
		AtomicBoolean discarded = new AtomicBoolean(false);
		AtomicBoolean warned = new AtomicBoolean(false);

		RestoreRecordingMockView view = new RestoreRecordingMockView() {
			@Override
			public void showWarning() {
				warned.set(true);
			}
		};

		RestoreRecordingPresenter presenter = new RestoreRecordingPresenter(context, view, viewFactory);
		presenter.initialize();
		presenter.setOnDiscardRecording(() -> discarded.set(true));

		RecordingBackup backup = new RecordingBackup(context.getRecordingDirectory());

		assertTrue(backup.hasCheckpoint());

		view.discardAction.execute();

		assertTrue(backup.hasCheckpoint());
		assertTrue(warned.get());
		assertFalse(discarded.get());

		view.discardAction.execute();

		assertFalse(backup.hasCheckpoint());
		assertTrue(discarded.get());
	}

	@Test
	void testSave() throws IOException, InterruptedException {
		AtomicBoolean success = new AtomicBoolean(false);
		AtomicBoolean shownProgress = new AtomicBoolean(false);
		AtomicReference<String> errorRef = new AtomicReference<>();
		AtomicReference<String> pathRef = new AtomicReference<>();
		AtomicReference<FileChooserMockView> chooserRef = new AtomicReference<>();
		CountDownLatch saveLatch = new CountDownLatch(1);

		Path selectedPath = testPath.resolve("test.presenter");
		File selectedFile = selectedPath.toFile();

		viewFactory = new ViewContextMockFactory() {
			@Override
			public FileChooserView createFileChooserView() {
				FileChooserMockView view = new FileChooserMockView() {
					@Override
					public File showSaveFile(View parent) {
						super.showSaveFile(parent);
						return selectedFile;
					}
				};
				chooserRef.set(view);
				return view;
			}
		};

		RestoreRecordingMockView view = new RestoreRecordingMockView() {
			@Override
			public void setError(String message) {
				errorRef.set(message);
			}

			@Override
			public void setSavePath(String path) {
				pathRef.set(path);
			}

			@Override
			public void showProgress() {
				shownProgress.set(true);
			}

			@Override
			public void setSuccess() {
				success.set(true);
			}
		};

		RestoreRecordingPresenter presenter = new RestoreRecordingPresenter(context, view, viewFactory);
		presenter.initialize();
		presenter.setOnRecordingSaved(saveLatch::countDown);

		view.saveAction.execute();

		RecordingBackup backup = new RecordingBackup(context.getRecordingDirectory());
		File initFile = new File(backup.getCheckpoint() + "." + PresenterContext.RECORDING_EXTENSION);

		view.saveAction.execute();

		saveLatch.await();

		assertEquals("Presenter Recordings", chooserRef.get().description);
		assertArrayEquals(new String[] { "*.presenter" }, chooserRef.get().extensions);
		assertEquals(initFile.getName(), chooserRef.get().initialFileName);
		assertEquals(initFile.getParentFile(), chooserRef.get().directory);
		assertEquals(selectedFile.getPath(), pathRef.get());
		assertTrue(shownProgress.get());
		assertFalse(backup.hasCheckpoint());
		assertTrue(success.get());
	}



	private static class RestoreRecordingMockView implements RestoreRecordingView {

		Action discardAction;

		Action saveAction;


		@Override
		public void setSavePath(String path) {

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
		public void setOnDiscard(Action action) {
			assertNotNull(action);

			discardAction = action;
		}

		@Override
		public void setOnSave(Action action) {
			assertNotNull(action);

			saveAction = action;
		}

		@Override
		public void showProgress() {

		}

		@Override
		public void showWarning() {

		}
	}

}