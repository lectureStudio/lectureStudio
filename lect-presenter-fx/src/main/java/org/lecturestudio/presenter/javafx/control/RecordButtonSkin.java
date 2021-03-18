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

package org.lecturestudio.presenter.javafx.control;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.skin.ButtonSkin;
import javafx.util.Duration;

public class RecordButtonSkin extends ButtonSkin {

	private Timeline flasher;


	protected RecordButtonSkin(RecordButton control) {
		super(control);

		initLayout(control);
	}

	private void initLayout(RecordButton control) {
		flasher = new Timeline(
				new KeyFrame(Duration.seconds(0.5), e -> {
					control.pseudoClassStateChanged(RecordButton.BLINK_PSEUDOCLASS_STATE, true);
				}),

				new KeyFrame(Duration.seconds(1.0), e -> {
					control.pseudoClassStateChanged(RecordButton.BLINK_PSEUDOCLASS_STATE, false);
				})
		);
		flasher.setCycleCount(Timeline.INDEFINITE);

		control.blinkProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				flasher.play();
			}
			else {
				flasher.stop();
				control.pseudoClassStateChanged(RecordButton.BLINK_PSEUDOCLASS_STATE,false);
			}
		});

		if (control.isBlinking()) {
			flasher.play();
		}
	}
}
