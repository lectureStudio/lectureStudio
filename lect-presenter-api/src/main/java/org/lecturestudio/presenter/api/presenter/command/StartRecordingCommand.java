/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.presenter.command;

import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.StartRecordingPresenter;

/**
 * Command to display the start recording presenter dialog and configure its start action.
 * This command is responsible for initializing the recording start workflow.
 *
 * @author Alex Andres
 */
public class StartRecordingCommand extends ShowPresenterCommand<StartRecordingPresenter> {

	/** The action to be executed when the recording starts. */
	private final Action startAction;


	/**
	 * Creates a new start recording command with the specified start action.
	 *
	 * @param startAction the action to execute when recording starts.
	 */
	public StartRecordingCommand(Action startAction) {
		super(StartRecordingPresenter.class);

		this.startAction = startAction;
	}

	/**
	 * Executes the command by setting the start action on the presenter.
	 *
	 * @param presenter the start recording presenter to configure.
	 */
	@Override
	public void execute(StartRecordingPresenter presenter) {
		final PresenterContext pContext = (PresenterContext) presenter.getContext();
		final boolean notifyToRecord = pContext.getNotifyToRecord();

		// Temporarily disable the notify-to-record feature while in settings.
		pContext.setNotifyToRecord(false);

		presenter.setOnStart(startAction);
		presenter.setOnClose(() -> {
			// Re-enable the notify-to-record feature when the view is closed.
			pContext.setNotifyToRecord(notifyToRecord);
		});
	}
}
