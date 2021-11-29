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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import dev.onvoid.webrtc.media.Device;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.audio.AudioConverter;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.audio.AudioDeviceModule;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioPlaybackProgressListener;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.source.AudioSource;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.audio.AudioPlayer;

/**
 * AudioPlayer implementation based on the WebRTC audio stack. This player can
 * utilize WebRTC audio processing filters, such as HighPassFilter,
 * NoiseSuppression, EchoCancellation, VoiceDetection, etc.
 *
 * @author Alex Andres
 */
public class WebRtcAudioPlayer extends ExecutableBase implements AudioPlayer {

	private AudioDeviceModule deviceModule;

	private AudioDevice playbackDevice;

	private AudioSource source;

	private AudioConverter audioConverter;

	/** The playback progress listener. */
	private AudioPlaybackProgressListener progressListener;

	private Time progress = new Time();
	private Time duration;

	private double volume;

	private byte[] buffer;

	/** The audio source size. */
	private long inputSize;

	/** The current audio source reading position. */
	private long inputPos;


	@Override
	public void setAudioProgressListener(AudioPlaybackProgressListener listener) {
		this.progressListener = listener;
	}

	@Override
	public void seek(int timeMs) throws Exception {
		inputPos = source.seekMs(timeMs);
	}

	@Override
	public void setAudioDeviceName(String deviceName) {
		playbackDevice = getDeviceByName(MediaDevices.getAudioRenderDevices(),
				deviceName);
	}

	@Override
	public void setAudioSource(AudioSource source) {
		this.source = source;

		try {
			this.inputSize = source.getInputSize();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setAudioVolume(double volume) {
		this.volume = volume;

		if (started()) {
			setAudioModuleVolume();
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		requireNonNull(source, "Audio source must be set");
		requireNonNull(source.getAudioFormat(), "Audio format must be set");
		requireNonNull(playbackDevice, "Audio playback device must be set");

		AudioFormat format = source.getAudioFormat();

		// Calculate bytes per millisecond.
		float bytesPerMs = AudioUtils.getBytesPerSecond(format) / 1000f;

		progress = new Time(0);
		duration = new Time((long) (inputSize / bytesPerMs));

		final int nSamplesIn = format.getSampleRate() / 100;
		final int nBytesIn = nSamplesIn * format.getChannels() * 2;

		buffer = new byte[nBytesIn];

		try {
			source.reset();

			deviceModule = new AudioDeviceModule();
			deviceModule.setPlayoutDevice(playbackDevice);
			deviceModule.setAudioSource(new dev.onvoid.webrtc.media.audio.AudioSource() {

				int bytesRead = 0;
				int processResult;

				@Override
				public int onPlaybackData(byte[] audioSamples, int nSamples,
						int nBytesPerSample, int nChannels, int samplesPerSec) {

					if (isNull(audioConverter)) {
						audioConverter = new AudioConverter(
								format.getSampleRate(), format.getChannels(),
								samplesPerSec, nChannels);
					}

					try {
						bytesRead = source.read(buffer, 0, nBytesIn);
						inputPos += bytesRead;

						// Audio conversion.
						audioConverter.convert(buffer, audioSamples);
					}
					catch (IOException e) {
						panic(e, "Read audio samples failed");
						return -1;
					}

					if (processResult != 0) {
						panic(new IllegalAccessError("Error code: " + processResult),
								"Process audio samples failed");
						return -1;
					}

					if (bytesRead > 0) {
						updateProgress();
					}
					else {
						stopPlayback();
					}

					return bytesRead;
				}
			});
			deviceModule.initPlayout();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		try {
			deviceModule.stopPlayout();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			deviceModule.initPlayout();

			setAudioModuleVolume();

			deviceModule.startPlayout();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			deviceModule.stopPlayout();

			source.reset();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}

		inputPos = 0;
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			deviceModule.dispose();
			deviceModule = null;
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}

		try {
			source.close();
		}
		catch (IOException e) {
			throw new ExecutableException(e);
		}
	}

	private void setAudioModuleVolume() {
		int maxVolume = deviceModule.getMaxSpeakerVolume();
		int minVolume = deviceModule.getMinSpeakerVolume();
		int v = (int) (minVolume + (maxVolume - minVolume) * volume);

		deviceModule.setSpeakerVolume(v);
	}

	private void panic(Throwable error, String description) {
		logException(error, description);

		stopPlayback();
	}

	private void updateProgress() {
		if (nonNull(progressListener) && started()) {
			progress.setMillis((long) (inputPos / (double) inputSize * duration.getMillis()));

			progressListener.onAudioProgress(progress, duration);
		}
	}

	private void stopPlayback() {
		CompletableFuture.runAsync(() -> {
			try {
				stop();
			}
			catch (ExecutableException e) {
				logException(e, "Stop playback failed");
			}
		});
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
