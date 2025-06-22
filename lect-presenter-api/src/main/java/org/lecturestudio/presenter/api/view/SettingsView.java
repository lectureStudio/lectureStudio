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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;

/**
 * Interface representing the settings view in the presenter application.
 * Provides methods for configuring and managing application settings.
 *
 * @author Alex Andres
 */
public interface SettingsView extends View {

	/**
	 * Sets whether advanced settings should be shown or hidden.
	 *
	 * @param selected true to show advanced settings, false to hide them.
	 */
	void setAdvancedSettings(boolean selected);

	/**
	 * Sets the file system path where settings are stored.
	 *
	 * @param path the path to the settings storage location.
	 */
	void setSettingsPath(String path);

	/**
	 * Sets the action to be executed when the settings view is closed.
	 *
	 * @param action the action to execute on close.
	 */
	void setOnClose(Action action);

}
