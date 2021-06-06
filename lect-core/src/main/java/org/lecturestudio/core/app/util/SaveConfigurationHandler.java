/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.app.util;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.util.ShutdownHandler;

public class SaveConfigurationHandler extends ShutdownHandler {

	private final ApplicationContext context;


	public SaveConfigurationHandler(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public boolean execute() {
		try {
			context.saveConfiguration();
		}
		catch (Exception e) {
			logException(e, "Save configuration failed");
		}

		return true;
	}
}
