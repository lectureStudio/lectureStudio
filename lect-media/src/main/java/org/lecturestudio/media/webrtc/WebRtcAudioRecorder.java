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
import static java.util.Objects.requireNonNull;

import dev.onvoid.webrtc.media.Device;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.audio.AudioDeviceModule;
import dev.onvoid.webrtc.media.audio.AudioProcessingConfig;

import java.util.List;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioProcessingStats;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.AudioRecorder;

/**
 * AudioRecorder implementation based on the WebRTC audio stack. This recorder
 * utilizes WebRTC audio processing filters, such as HighPassFilter,
 * NoiseSuppression, EchoCancellation, VoiceDetection, etc.
 *
 * @author Alex Andres
 */
public class WebRtcAudioRecorder extends ExecutableBase implements AudioRecorder {

	private AudioDeviceModule deviceModule;

	private AudioDevice captureDevice;

	private AudioProcessingSettings processingSettings;

	private AudioSink sink;

	private WebRtcAudioSinkNode sinkNode;

	private double volume;


	@Override
	public void setAudioDeviceName(String deviceName) {
		captureDevice = getDeviceByName(MediaDevices.getAudioCaptureDevices(),
				deviceName);
	}

	@Override
	public void setAudioSink(AudioSink sink) {
		this.sink = sink;
	}

	@Override
	public void setAudioVolume(double volume) {
		this.volume = volume;

		if (started()) {
			setAudioModuleVolume();
		}
	}

	@Override
	public AudioProcessingStats getAudioProcessingStats() {
		if (started() && sinkNode instanceof WebRtcAudioProcessingNode) {
			WebRtcAudioProcessingNode processingNode = (WebRtcAudioProcessingNode) sinkNode;
			return processingNode.getAudioProcessingStats();
		}

		return null;
	}

	@Override
	public void setAudioProcessingSettings(AudioProcessingSettings settings) {
		this.processingSettings = settings;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		requireNonNull(sink, "Audio sink must be set");
		requireNonNull(sink.getAudioFormat(), "Audio format must be set");
		requireNonNull(captureDevice, "Audio recording device must be set");

		if (nonNull(processingSettings)) {
			WebRtcAudioProcessingNode processingNode = new WebRtcAudioProcessingNode();
			processingNode.setAudioSinkFormat(sink.getAudioFormat());
			processingNode.setAudioProcessingConfig(getAudioProcessingConfig());
			processingNode.setAudioSinkNode(new AudioSinkNode(sink));

			sinkNode = processingNode;
		}
		else {
			sinkNode = new AudioSinkNode(sink);
		}

		try {
			sinkNode.initialize();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}

		deviceModule = new AudioDeviceModule();
		deviceModule.setRecordingDevice(captureDevice);
		deviceModule.setAudioSink((audioSamples, nSamples, nBytesPerSample,
				nChannels, samplesPerSec, totalDelayMS, clockDrift) -> {

			try {
				sinkNode.onData(audioSamples, nSamples, nBytesPerSample,
						nChannels, samplesPerSec, totalDelayMS, clockDrift);
			}
			catch (Throwable e) {
				logException(e, "Write audio to sink failed");

				deviceModule.stopRecording();
			}
		});
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		try {
			deviceModule.stopRecording();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			deviceModule.initRecording();

			setAudioModuleVolume();

			deviceModule.startRecording();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			deviceModule.stopRecording();
			deviceModule.dispose();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}

		try {
			sinkNode.destroy();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		deviceModule = null;
	}

	private void setAudioModuleVolume() {
		int maxVolume = deviceModule.getMaxMicrophoneVolume();
		int minVolume = deviceModule.getMinMicrophoneVolume();
		int v = (int) (minVolume + (maxVolume - minVolume) * volume);

		deviceModule.setMicrophoneVolume(v);
	}

	private AudioProcessingConfig getAudioProcessingConfig() {
		return AudioProcessingConfigConverter.INSTANCE.from(processingSettings);
	}

	/**
	 * Searches the provided list for a device with the provided name.
	 *
	 * @param devices The device list in which to search for the device.
	 * @param name    The name of the device to search for.
	 * @param <T>     The device type.
	 *
	 * @return The device with the specified name or {@code null} if not found.
	 */
	private <T extends Device> T getDeviceByName(List<T> devices, String name) {
		return devices.stream()
				.filter(device -> device.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
