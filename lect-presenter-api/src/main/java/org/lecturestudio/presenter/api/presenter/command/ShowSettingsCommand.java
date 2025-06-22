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

package org.lecturestudio.presenter.api.presenter.command;

import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.SettingsPresenter;

/**
 * Command to show application settings using a SettingsPresenter.
 * This class extends ShowPresenterCommand and provides functionality
 * to open the settings view with a specific settings path.
 *
 * @author Alex Andres
 */
public class ShowSettingsCommand extends ShowPresenterCommand<SettingsPresenter> {

	/**
	 * The path to the specific settings section that should be displayed.
	 * This path is passed to the SettingsPresenter to navigate to a particular
	 * settings page when the settings view is shown.
	 */
	private final String settingsPath;


	/**
	 * Creates a new command to show settings with navigation to a specific settings path.
	 *
	 * @param settingsPath The path identifying the settings section to display when the settings view is shown.
	 *                     This is used to navigate directly to a specific settings page.
	 */
	public ShowSettingsCommand(String settingsPath) {
		super(SettingsPresenter.class);

		this.settingsPath = settingsPath;
	}

	@Override
	public void execute(SettingsPresenter presenter) {
		PresenterContext pContext = (PresenterContext) presenter.getContext();
		// Temporarily disable the notify-to-record feature while in settings.
		pContext.setNotifyToRecord(false);

		presenter.setSettingsPath(settingsPath);
		presenter.setCloseAction(() -> {
			// Re-enable the notify-to-record feature when the settings view is closed.
			pContext.setNotifyToRecord(true);
		});
	}
}
