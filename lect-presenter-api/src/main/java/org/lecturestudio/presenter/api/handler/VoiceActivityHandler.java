/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.audio.VoiceActivityDetector;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.presenter.api.context.PresenterContext;

public class VoiceActivityHandler extends PresenterHandler {

	private final AudioSystemProvider audioSystemProvider;

	private final AudioConfiguration audioConfig;

	private AudioRecorder recorder;

	private VoiceActivityDetector activityDetector;


	public VoiceActivityHandler(PresenterContext context, AudioSystemProvider systemProvider) {
		super(context);

		audioConfig = context.getConfiguration().getAudioConfig();
		audioSystemProvider = systemProvider;
	}

	@Override
	public void initialize() {
		AudioProcessingSettings settings = new AudioProcessingSettings();
		settings.setEchoCancellerEnabled(true);
		settings.setHighpassFilterEnabled(true);
		settings.setNoiseSuppressionEnabled(true);
		settings.setNoiseSuppressionLevel(AudioProcessingSettings.NoiseSuppressionLevel.LOW);

		activityDetector = new VoiceActivityDetector();

		recorder = createAudioRecorder();
		recorder.setAudioProcessingSettings(settings);
		recorder.setAudioSink(new AudioSink() {

			final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 16000, 1);


			@Override
			public void open() {}

			@Override
			public void reset() {}

			@Override
			public void close() {}

			@Override
			public int write(byte[] data, int offset, int length) {
				activityDetector.process(data, data.length / 2, audioFormat.getSampleRate());

				float probability = activityDetector.getLastVoiceProbability();

				if (probability > 0.75) {
					System.out.println("Voice detected");
				}

//				context.getAudioBus().post(new AudioSignalEvent(level));

				return 0;
			}

			@Override
			public AudioFormat getAudioFormat() {
				return audioFormat;
			}

			@Override
			public void setAudioFormat(AudioFormat format) {}
		});

		start();
	}

	private void start() {
		if (!recorder.started()) {
			try {
				recorder.start();
			}
			catch (ExecutableException e) {
				handleException(e, "Start voice activity detection failed", "generic.error");
			}
		}
	}

	private void stop() {
		if (recorder.started() || recorder.suspended()) {
			try {
				recorder.stop();
			}
			catch (ExecutableException e) {
				handleException(e, "Stop voice activity detection failed", "generic.error");
			}
		}
	}

	private void destroy() {
		try {
			recorder.destroy();

			activityDetector.dispose();
		}
		catch (ExecutableException e) {
			handleException(e, "Destroy voice activity detection failed", "generic.error");
		}
	}

	private AudioRecorder createAudioRecorder() {
		String inputDeviceName = audioConfig.getCaptureDeviceName();
		double volume = audioConfig.getMasterRecordingVolume();
		Double devVolume = audioConfig.getRecordingVolume(inputDeviceName);

		if (nonNull(devVolume)) {
			volume = devVolume;
		}

		AudioRecorder recorder = audioSystemProvider.createAudioRecorder();
		recorder.setAudioDeviceName(inputDeviceName);
		recorder.setAudioVolume(volume);

		return recorder;
	}
}
