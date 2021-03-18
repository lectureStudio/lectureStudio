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

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.AdjustAudioCaptureLevelView;
import org.lecturestudio.swing.components.LevelMeter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "adjust-audio-capture-level")
public class SwingAdjustAudioCaptureLevelView extends JPanel implements AdjustAudioCaptureLevelView {

	private final ResourceBundle resources;

	private Action finishAction;

	private LevelMeter levelMeter;

	private JLabel captureDeviceLabel;

	private JButton beginButton;

	private JButton cancelButton;


	@Inject
	SwingAdjustAudioCaptureLevelView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void setAudioLevel(double value) {
		SwingUtils.invoke(() -> levelMeter.setLevel(value));
	}

	@Override
	public void setAudioLevelCaptureStarted(boolean started) {
		String buttonText;

		if (started) {
			buttonText = resources.getString("button.finish");
		}
		else {
			buttonText = resources.getString("button.begin");
		}

		SwingUtils.invoke(() -> beginButton.setText(buttonText));
		SwingUtils.bindAction(beginButton, finishAction);
	}

	@Override
	public void setCaptureDeviceName(String name) {
		SwingUtils.invoke(() -> captureDeviceLabel.setText(name));
	}

	@Override
	public void setOnBegin(Action action) {
		SwingUtils.bindAction(beginButton, action);
	}

	@Override
	public void setOnCancel(Action action) {
		SwingUtils.bindAction(cancelButton, action);
	}

	@Override
	public void setOnFinish(Action action) {
		this.finishAction = action;
	}
}
