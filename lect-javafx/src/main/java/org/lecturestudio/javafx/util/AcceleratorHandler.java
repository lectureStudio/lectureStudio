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

package org.lecturestudio.javafx.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Map;

import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import javafx.util.Duration;

public class AcceleratorHandler {

	public static void set(ButtonBase control) {
		if (!(control instanceof AcceleratorSupport)) {
			throw new IllegalArgumentException("Control cannot handle accelerators");

		}

		final ObjectProperty<KeyCombination> acceleratorProperty = ((AcceleratorSupport) control).acceleratorProperty();

		// Scene - Accelerator
		final Scene scene = control.getScene();

		if (isNull(scene)) {
			// Wait until the scene is set, and then install the accelerator.
			control.sceneProperty().addListener(new InvalidationListener() {

				@Override
				public void invalidated(Observable observable) {
					Scene scene = control.getScene();

					if (nonNull(scene)) {
						control.sceneProperty().removeListener(this);

						installAccelerator(acceleratorProperty, control);
					}
				}
			});
		}
		else {
			installAccelerator(acceleratorProperty, control);
		}
	}

	private static void installAccelerator(ObjectProperty<KeyCombination> acceleratorProperty, ButtonBase control) {
		Scene scene = control.getScene();

		if (isNull(scene)) {
			return;
		}

		KeyCombination accelerator = acceleratorProperty.get();

		if (nonNull(accelerator)) {
			final Map<KeyCombination, Runnable> accelerators = scene.getAccelerators();

			Runnable acceleratorRunnable = () -> {
				if (!control.isDisable()) {
					control.arm();

					if (control.isArmed()) {
						PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
						pause.setOnFinished(evt -> {
							control.fire();
							control.disarm();
						});
						pause.play();
					}
				}
			};

			accelerators.put(accelerator, acceleratorRunnable);
		}

		// We also listen to the accelerator property for changes, such
		// that we can update the scene when a button accelerator changes.
		acceleratorProperty.addListener((observable, oldValue, newValue) -> {
			final Map<KeyCombination, Runnable> accelerators = scene.getAccelerators();
			Runnable acceleratorRunnable = accelerators.remove(oldValue);

			if (nonNull(newValue)) {
				accelerators.put(newValue, acceleratorRunnable);
			}

			updateTooltip(newValue, control.getTooltip());
		});

		// Tooltip - Accelerator
		final Tooltip tooltip = control.getTooltip();

		if (isNull(tooltip)) {
			control.tooltipProperty().addListener((observable, oldValue, newValue) -> {
				updateTooltip(acceleratorProperty.get(), newValue);
			});
		}
		else {
			updateTooltip(acceleratorProperty.get(), tooltip);
		}
	}

	private static void updateTooltip(KeyCombination accelerator, Tooltip tooltip) {
		if (nonNull(accelerator)) {
			Label acceleratorText = new Label(accelerator.getDisplayText());
			acceleratorText.getStyleClass().add("accelerator");
			acceleratorText.fontProperty().bind(tooltip.fontProperty());

			tooltip.setContentDisplay(ContentDisplay.RIGHT);
			tooltip.setGraphic(acceleratorText);
		}
		else {
			tooltip.setGraphic(null);
		}
	}
}
