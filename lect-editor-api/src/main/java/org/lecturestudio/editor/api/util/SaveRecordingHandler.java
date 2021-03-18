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

package org.lecturestudio.editor.api.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.presenter.command.QuitSaveRecordingCommand;
import org.lecturestudio.editor.api.service.RecordingFileService;

public class SaveRecordingHandler extends ShutdownHandler {

	private final ApplicationContext context;

	private final RecordingFileService recordingService;


	public SaveRecordingHandler(ApplicationContext context, RecordingFileService recordingService) {
		this.context = context;
		this.recordingService = recordingService;
	}

	@Override
	public boolean execute() {
		Recording recording = recordingService.getSelectedRecording();

		if (isNull(recording)) {
			return true;
		}

		Integer savedHash = recordingService.getRecordingSaveHash(recording);

		if (nonNull(savedHash)) {
			int currentHash = recording.getStateHash();
			if (currentHash == savedHash) {
				// Already saved, nothing to do;
				return true;
			}
		}

		if (recording.hasRedoActions() || recording.hasUndoActions()) {
			executeAndWait(() -> {
				Action closeAction = this::resume;

				context.getEventBus().post(new QuitSaveRecordingCommand(closeAction));
			});
		}

		return true;
	}
}
