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

package org.lecturestudio.presenter.swing.view;

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.StartStreamView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.web.api.stream.model.Course;

@SwingView(name = "start-stream")
public class SwingStartStreamView extends JPanel implements StartStreamView {

	private JComboBox<Course> courseCombo;

	private JLabel courseLabel;

	private JLabel errorLabel;

	private JButton closeButton;

	private JButton startButton;


	SwingStartStreamView() {
		super();
	}

	@Override
	public void setCourse(ObjectProperty<Course> course) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(courseCombo, course);
		});
	}

	@Override
	public void setCourses(List<Course> courses) {
		SwingUtils.invoke(() -> {
			courseCombo.setLightWeightPopupEnabled(false);
			courseCombo.setModel(new DefaultComboBoxModel<>(new Vector<>(courses)));
		});
	}

	@Override
	public void setError(String message) {
		SwingUtils.invoke(() -> {
			errorLabel.setText(message);
			errorLabel.setVisible(true);

			courseLabel.setVisible(false);
			courseCombo.setVisible(false);
			startButton.setVisible(false);
		});
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnStart(Action action) {
		SwingUtils.bindAction(startButton, action);
	}
}
