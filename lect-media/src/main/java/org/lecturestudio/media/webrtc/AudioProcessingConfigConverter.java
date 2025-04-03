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

package org.lecturestudio.media.webrtc;

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.audio.AudioProcessingConfig;
import dev.onvoid.webrtc.media.audio.AudioProcessingConfig.NoiseSuppression;

import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.beans.Converter;

/**
 * WebRTC {@code AudioProcessingConfig} to {@code AudioProcessingSettings}
 * converter.
 *
 * @author Alex Andres
 */
public class AudioProcessingConfigConverter implements
		Converter<AudioProcessingConfig, AudioProcessingSettings> {

	public static final AudioProcessingConfigConverter INSTANCE = new AudioProcessingConfigConverter();


	@Override
	public AudioProcessingSettings to(AudioProcessingConfig config) {
		AudioProcessingSettings settings = new AudioProcessingSettings();
		settings.setEchoCancellerEnabled(config.echoCanceller.enabled);
		settings.setGainControlEnabled(config.gainControl.enabled);
		settings.setHighpassFilterEnabled(config.highPassFilter.enabled);
		settings.setNoiseSuppressionEnabled(config.noiseSuppression.enabled);

		NoiseSuppression.Level nsLevel = config.noiseSuppression.level;

		if (nonNull(nsLevel)) {
			settings.setNoiseSuppressionLevel(getNsLevel(nsLevel));
		}

		return settings;
	}

	@Override
	public AudioProcessingConfig from(AudioProcessingSettings settings) {
		AudioProcessingConfig config = new AudioProcessingConfig();
		config.echoCanceller.enabled = settings.isEchoCancellerEnabled();
		config.echoCanceller.enforceHighPassFiltering = false;
		config.gainControl.enabled = settings.isGainControlEnabled();
		config.highPassFilter.enabled = settings.isHighpassFilterEnabled();
		config.noiseSuppression.enabled = settings.isNoiseSuppressionEnabled();

		NoiseSuppressionLevel nsLevel = settings.getNoiseSuppressionLevel();

		if (nonNull(nsLevel)) {
			config.noiseSuppression.level = getNativeNsLevel(nsLevel);
		}

		return config;
	}

	private NoiseSuppressionLevel getNsLevel(NoiseSuppression.Level nsLevel) {
		switch (nsLevel) {
			case LOW:
				return NoiseSuppressionLevel.LOW;
			case HIGH:
				return NoiseSuppressionLevel.HIGH;
			case VERY_HIGH:
				return NoiseSuppressionLevel.VERY_HIGH;
			default:
				return NoiseSuppressionLevel.MODERATE;
		}
	}

	private NoiseSuppression.Level getNativeNsLevel(NoiseSuppressionLevel nsLevel) {
		switch (nsLevel) {
			case LOW:
				return NoiseSuppression.Level.LOW;
			case HIGH:
				return NoiseSuppression.Level.HIGH;
			case VERY_HIGH:
				return NoiseSuppression.Level.VERY_HIGH;
			default:
				return NoiseSuppression.Level.MODERATE;
		}
	}
}
