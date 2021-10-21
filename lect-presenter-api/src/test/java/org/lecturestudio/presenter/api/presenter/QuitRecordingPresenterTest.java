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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.recording.RecordingBackup;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.QuitRecordingView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuitRecordingPresenterTest extends PresenterTest {

	private RecordingService recordingService;

	private QuitRecordingMockView view;

	private QuitRecordingPresenter presenter;


	@BeforeEach
	void setup() throws IOException {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");
		audioConfig.setSoundSystem("dummy");

		DocumentService documentService = context.getDocumentService();

		FileLectureRecorder recorder = new FileLectureRecorder(documentService, audioConfig, context.getRecordingDirectory());

		recordingService = new RecordingService(context, recorder);

		view = new QuitRecordingMockView();

		presenter = new QuitRecordingPresenter(context, view, recordingService);
		presenter.initialize();
	}

	@Test
	void testAbort() {
		AtomicBoolean aborted = new AtomicBoolean(false);

		presenter.setOnAbort(() -> {
			aborted.set(true);
		});

		view.abortAction.execute();

		assertTrue(aborted.get());
	}

	@Test
	void testDiscard() throws ExecutableException, IOException {
		RecordingBackup backup = new RecordingBackup(context.getRecordingDirectory());
		AtomicBoolean discarded = new AtomicBoolean(false);

		presenter.setOnDiscardRecording(() -> {
			discarded.set(true);
		});

		recordingService.start();
		recordingService.suspend();

		assertTrue(backup.hasCheckpoint());

		view.discardAction.execute();

		assertFalse(backup.hasCheckpoint());
		assertTrue(discarded.get());
	}

	@Test
	void testSave() {
		AtomicBoolean save = new AtomicBoolean(false);

		presenter.setOnSaveRecording(() -> {
			save.set(true);
		});

		view.saveAction.execute();

		assertTrue(save.get());
	}



	private static class QuitRecordingMockView implements QuitRecordingView {

		Action abortAction;

		Action discardAction;

		Action saveAction;


		@Override
		public void setOnAbort(Action action) {
			assertNotNull(action);

			abortAction = action;
		}

		@Override
		public void setOnDiscardRecording(Action action) {
			assertNotNull(action);

			discardAction = action;
		}

		@Override
		public void setOnSaveRecording(Action action) {
			assertNotNull(action);

			saveAction = action;
		}
	}
}