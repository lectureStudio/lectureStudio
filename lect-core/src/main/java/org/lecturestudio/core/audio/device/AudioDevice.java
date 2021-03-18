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

package org.lecturestudio.core.audio.device;

import java.util.Arrays;
import java.util.List;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Common class to provide a consistent mechanism for audio devices.
 *
 * @author Alex Andres
 */
public abstract class AudioDevice {

	/** An array of all available sample rates to support. */
	public static final int[] SUPPORTED_SAMPLE_RATES = new int[] {
		8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000
	};

	/** The audio format to be used by the audio device. */
	private AudioFormat audioFormat;

	/** The audio volume for playback or recording. */
	private double volume = 1;

	/** The measured audio signal power level of the last processed audio chunk. */
	private double signalPowerLevel = 0;

	/** Indicated whether the audio is muted or not. */
	private boolean mute = false;


	/**
	 * Get the name of the audio device assigned by the operating system.
	 *
	 * @return the name of the audio device.
	 */
	abstract public String getName();

	/**
	 * Open the audio device and prepare the device to capture or play audio.
	 *
	 * @throws Exception If the audio device failed to open.
	 */
	abstract public void open() throws Exception;

	/**
	 * Close the audio device and release all previously assigned resources.
	 *
	 * @throws Exception If the audio device could not be closed.
	 */
	abstract public void close() throws Exception;

	/**
	 * Start capturing or playing audio by the device.
	 *
	 * @throws Exception If the device failed to start.
	 */
	abstract public void start() throws Exception;

	/**
	 * Stop capturing or playing audio by the device.
	 *
	 * @throws Exception If the device failed to stop.
	 */
	abstract public void stop() throws Exception;

	/**
	 * Check if the audio device is opened.
	 *
	 * @return true if the device is opened, false otherwise.
	 */
	abstract public boolean isOpen();

	/**
	 * Get a list of all supported audio formats by this device.
	 *
	 * @return a list of all supported audio formats.
	 */
	abstract public List<AudioFormat> getSupportedFormats();

	/**
	 * Get the current audio buffer size. The buffer size reflects the latency
	 * of the audio signal.
	 *
	 * @return the current audio buffer size.
	 */
	abstract public int getBufferSize();


	/**
	 * Check if the specified audio format is supported by the device.
	 *
	 * @param format The audio format to check.
	 *
	 * @return true if the audio format is supported, false otherwise.
	 */
	public boolean supportsAudioFormat(AudioFormat format) {
		return getSupportedFormats().contains(format);
	}

	/**
	 * Set the audio format to be used by the audio device for playback or
	 * recording.
	 *
	 * @param format The audio format to be used.
	 */
	public void setAudioFormat(AudioFormat format) {
		this.audioFormat = format;
	}

	/**
	 * Get the audio format of the device.
	 *
	 * @return the audio format.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	/**
	 * Get the volume of the device with which it plays or records audio.
	 *
	 * @return the volume of audio.
	 */
	public double getVolume() {
		return volume;
	}

	/**
	 * Set the audio volume for playback or recording. The volume value must be
	 * in the range of [0,1].
	 *
	 * @param volume The new volume value.
	 */
	public void setVolume(double volume) {
		if (volume < 0 || volume > 1)
			return;

		this.volume = volume;
	}

	/**
	 * Check whether audio signal is muted or not.
	 *
	 * @return true if the audio signal is muted, false otherwise.
	 */
	public boolean isMuted() {
		return mute;
	}

	/**
	 * Set whether to mute the audio signal of this device.
	 *
	 * @param mute True to mute the audio signal, false otherwise.
	 */
	public void setMute(boolean mute) {
		this.mute = mute;
	}

	/**
	 * Get the signal power level of the last processed audio data chunk.
	 *
	 * @return the current signal power level of audio.
	 */
	public double getSignalPowerLevel() {
		if (isMuted()) {
			return 0;
		}

		return signalPowerLevel;
	}

	/**
	 * Set the signal power level of the last processed audio data chunk.
	 *
	 * @param level the current signal power level of audio.
	 */
	protected void setSignalPowerLevel(float level) {
		this.signalPowerLevel = level;
	}

	/**
	 * AGC algorithm to adjust the speech level of an audio signal to a
	 * specified value in dBFS.
	 *
	 * @param input       Audio input samples with values in range [-1, 1].
	 * @param output      Audio output samples with values in range [-1, 1].
	 * @param gainLevel   Output power level in dBFS.
	 * @param sampleCount Number of samples.
	 */
	protected void AGC(float[] input, float[] output, float gainLevel, int sampleCount) {
		// Convert power gain level into normal power.
		float power = (float) Math.pow(10, (gainLevel / 10));

		// Calculate the energy of the input signal.
		float energy = 0;
		for (int i = 0; i < sampleCount; i++) {
			energy += input[i] * input[i];
		}

		// Calculate the amplification factor.
		float amp = (float) Math.sqrt((power * sampleCount) / energy);

		// Scale the input signal to achieve the required output power.
		for (int i = 0; i < sampleCount; i++) {
			output[i] = input[i] * amp;
		}
	}

	void applyGain(byte[] buffer, int offset, int length) {
		if (volume == 1) {
			signalPowerLevel = getSignalPowerLevel(buffer);
			return;
		}

		if (volume == 0) {
			signalPowerLevel = 0;
			Arrays.fill(buffer, offset, length - offset, (byte) 0);
			return;
		}

		float energy = 0;

		for (int i = 0; i < buffer.length; i += 2) {
			int value = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
			value = (int) (volume * value);

			if (value > Short.MAX_VALUE) {
				value = Short.MAX_VALUE;
			}
			else if (value < Short.MIN_VALUE) {
				value = Short.MIN_VALUE;
			}

			float norm = (float) value / Short.MAX_VALUE;
			energy += norm * norm;

			buffer[i] = (byte) value;
			buffer[i + 1] = (byte) (value >> 8);
		}

		signalPowerLevel = (float) (10 * Math.log10(energy / (buffer.length / 2f)) + 96) / 96;
	}

	private float getSignalPowerLevel(byte[] buffer) {
		float energy = 0;

		for (int i = 0; i < buffer.length; i += 2) {
			int value = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));

			float norm = (float) value / Short.MAX_VALUE;
			energy += norm * norm;
		}

		return (float) (10 * Math.log10(energy / (buffer.length / 2f)) + 96) / 96;
	}

}
