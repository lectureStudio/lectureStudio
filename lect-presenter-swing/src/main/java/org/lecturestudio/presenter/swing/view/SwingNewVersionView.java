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

package org.lecturestudio.presenter.swing.view;

import javax.swing.JButton;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NewVersionView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "new-version")
public class SwingNewVersionView extends NotificationPane implements NewVersionView {

	private JButton closeButton;

	private JButton downloadButton;

	private JButton openUrlButton;


	SwingNewVersionView() {
		super();
	}

	@Override
	public void setOnDownload(Action action) {
		SwingUtils.bindAction(downloadButton, action);
	}

	@Override
	public void setOnOpenUrl(Action action) {
		SwingUtils.bindAction(openUrlButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}
}
