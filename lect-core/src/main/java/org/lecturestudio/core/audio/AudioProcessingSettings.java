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

package org.lecturestudio.core.audio;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;

/**
 * Specifies software audio processing filters to be applied to audio samples
 * coming from an audio input device, e.g. a microphone, or being provided to an
 * audio output device, e.g. speakers. If the hardware has activated such
 * filters, then the corresponding setting should be disabled here.
 *
 * @author Alex Andres
 */
public class AudioProcessingSettings {

	public enum NoiseSuppressionLevel {
		LOW,
		MODERATE,
		HIGH,
		VERY_HIGH
	}



	private final BooleanProperty enableEchoCanceller = new BooleanProperty();

	private final BooleanProperty enableGainControl = new BooleanProperty();

	private final BooleanProperty enableHighpassFilter = new BooleanProperty();

	private final BooleanProperty enableNoiseSuppression = new BooleanProperty();

	private final BooleanProperty enableLevelEstimation = new BooleanProperty();

	private final BooleanProperty enableVoiceDetection = new BooleanProperty();

	private final ObjectProperty<NoiseSuppressionLevel> noiseSuppressionLevel = new ObjectProperty<>();


	public boolean isEchoCancellerEnabled() {
		return enableEchoCanceller.get();
	}

	public void setEchoCancellerEnabled(boolean enable) {
		enableEchoCanceller.set(enable);
	}

	public boolean isGainControlEnabled() {
		return enableGainControl.get();
	}

	public void setGainControlEnabled(boolean enable) {
		enableGainControl.set(enable);
	}

	public boolean isHighpassFilterEnabled() {
		return enableHighpassFilter.get();
	}

	public void setHighpassFilterEnabled(boolean enable) {
		enableHighpassFilter.set(enable);
	}

	public BooleanProperty enableNoiseSuppressionProperty() {
		return enableNoiseSuppression;
	}

	public boolean isNoiseSuppressionEnabled() {
		return enableNoiseSuppression.get();
	}

	public void setNoiseSuppressionEnabled(boolean enable) {
		enableNoiseSuppression.set(enable);
	}

	public boolean isLevelEstimationEnabled() {
		return enableLevelEstimation.get();
	}

	public void setLevelEstimationEnabled(boolean enable) {
		enableLevelEstimation.set(enable);
	}

	public boolean isVoiceDetectionEnabled() {
		return enableVoiceDetection.get();
	}

	public void setVoiceDetectionEnabled(boolean enable) {
		enableVoiceDetection.set(enable);
	}

	public NoiseSuppressionLevel getNoiseSuppressionLevel() {
		return noiseSuppressionLevel.get();
	}

	public void setNoiseSuppressionLevel(NoiseSuppressionLevel level) {
		noiseSuppressionLevel.set(level);
	}

	public ObjectProperty<NoiseSuppressionLevel> noiseSuppressionLevelProperty() {
		return noiseSuppressionLevel;
	}
}
