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

package org.lecturestudio.javafx.control;

import static java.util.Objects.nonNull;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

public class AdjustAudioVolumeSelectionSkin extends MediaTrackSelectionSkin {

	private static final double OUT_MIN = 0;

	private static final double OUT_MAX = 2;

	private final AdjustAudioVolumeSelection trackSelection;

	private VolumeSlider volumeSlider;


	protected AdjustAudioVolumeSelectionSkin(AdjustAudioVolumeSelection control) {
		super(control);

		trackSelection = control;

		initLayout();
	}

	@Override
	public void dispose() {
		super.dispose();

		unregisterChangeListeners(trackSelection.volumeScalarProperty());
	}

	private void initLayout() {
		volumeSlider = new VolumeSlider(selectRect);
		volumeSlider.setOnUpdateValue(this::updateVolumeSliderValue);

		getChildren().add(volumeSlider);

		Platform.runLater(() -> {
			setSliderPos(volumeSlider, trackSelection.getVolumeScalar());
		});
	}

	private void setSliderPos(VolumeSlider slider, double value) {
		double y = (value - OUT_MAX) / (OUT_MIN - OUT_MAX);

		slider.setLayoutY(parent.getHeight() * y - slider.getHeight() / 2);
	}

	private double getSliderValue(VolumeSlider slider) {
		Affine transform = parent.getTransform();

		double sliderY = slider.getLayoutY() + slider.getHeight() / 2;
		double height = parent.getHeight();
		double ty = transform.getTy();

		return (sliderY / height - ty);
	}

	private void updateVolumeSliderValue() {
		double value = getSliderValue(volumeSlider);
		double output = OUT_MAX + (OUT_MIN - OUT_MAX) * value;

		trackSelection.setVolumeScalar(output);
		trackSelection.getTrackControl().setVolumeScalar(output);
	}



	private class VolumeSlider extends Rectangle implements Slider {

		private Runnable updateValueCallback;


		VolumeSlider(Rectangle ref) {
			getStyleClass().add("slider-rect");
			setManaged(false);
			setHeight(4);
			layoutXProperty().bind(ref.layoutXProperty());
			widthProperty().bind(ref.widthProperty());

			ThumbMouseHandler mouseHandler = new ThumbMouseHandler(this,
					Orientation.VERTICAL);

			setOnMouseDragged(mouseHandler);
			setOnMousePressed(mouseHandler);
			setOnMouseReleased(mouseHandler);
			setOnMouseClicked(mouseHandler);
		}

		@Override
		public void moveByDelta(double dy) {
			double pos = getLayoutY() + dy;
			double height = getHeight();

			if (pos < getParent().getLayoutY()) {
				return;
			}
			if (pos + height > getParent().getLayoutY() + parent.getHeight()) {
				return;
			}

			setLayoutY(pos);
		}

		@Override
		public void mouseDragged() {
			if (nonNull(updateValueCallback)) {
				updateValueCallback.run();
			}
		}

		void setOnUpdateValue(Runnable callback) {
			updateValueCallback = callback;
		}
	}
}
