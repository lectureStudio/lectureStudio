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

package org.lecturestudio.presenter.api.handler.shutdown;

import static java.util.Objects.nonNull;

import java.io.IOException;

import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.command.QuitSaveRecordingCommand;
import org.lecturestudio.presenter.api.recording.RecordingBackup;

public class SaveRecordingHandler extends ShutdownHandler {

	private final PresenterContext context;

	private boolean result;


	public SaveRecordingHandler(PresenterContext context) {
		this.context = context;
	}

	@Override
	public boolean execute() {
		result = true;

		RecordingBackup backup = null;

		try {
			backup = new RecordingBackup(context.getRecordingDirectory());
		}
		catch (IOException e) {
			result = false;
		}

		if (nonNull(backup) && backup.hasCheckpoint()) {
			executeAndWait(() -> {
				Action abortAction = () -> {
					result = false;
					resume();
				};
				Action discardAction = this::resume;
				Action saveAndQuitAction = this::resume;

				context.getEventBus().post(new QuitSaveRecordingCommand(
						abortAction, discardAction, saveAndQuitAction));
			});
		}

		return result;
	}
}
