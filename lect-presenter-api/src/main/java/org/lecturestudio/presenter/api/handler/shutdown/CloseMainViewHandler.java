/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.presenter.api.view.MainView;

public class CloseMainViewHandler extends ShutdownHandler {

	private final MainView view;


	public CloseMainViewHandler(MainView view) {
		this.view = view;
	}

	@Override
	public boolean execute() throws Exception {
		view.hideView();
		return true;
	}
}
