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
import javax.swing.JPanel;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.WhiteboardSettingsView;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.components.ColorChooserButton;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "whiteboard-settings", presenter = org.lecturestudio.presenter.api.presenter.WhiteboardSettingsPresenter.class)
public class SwingWhiteboardSettingsView extends JPanel implements WhiteboardSettingsView {

	private ColorChooserButton colorChooserButton;

	private JButton closeButton;

	private JButton resetButton;


	SwingWhiteboardSettingsView() {
		super();
	}

	@Override
	public void setBackgroundColor(ObjectProperty<Color> color) {
		SwingUtils.bindBidirectional(colorChooserButton, new ConvertibleObjectProperty<>(color,
				ColorConverter.INSTANCE));
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}
}
