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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Skin;

import org.lecturestudio.media.track.control.AdjustAudioVolumeControl;

public class AdjustAudioVolumeSelection extends MediaTrackSelection<AdjustAudioVolumeControl> {

	private final static String DEFAULT_STYLE_CLASS = "audio-volume-selection";

	private final DoubleProperty volumeScalar = new SimpleDoubleProperty();


	public AdjustAudioVolumeSelection() {
		super();

		initialize();
	}

	public final double getVolumeScalar() {
		return volumeScalar.get();
	}

	public final void setVolumeScalar(double time) {
		volumeScalar.set(time);
	}

	public final DoubleProperty volumeScalarProperty() {
		return volumeScalar;
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new AdjustAudioVolumeSelectionSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
}
