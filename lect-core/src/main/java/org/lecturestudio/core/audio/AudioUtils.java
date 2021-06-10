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

package org.lecturestudio.core.audio;

import static java.util.Objects.isNull;

import java.util.Arrays;

import org.lecturestudio.core.audio.codec.AudioCodecLoader;
import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.system.AudioSystemLoader;
import org.lecturestudio.core.audio.system.AudioSystemProvider;

/**
 * Audio-related utility methods.
 *
 * @author Alex Andres
 */
public class AudioUtils {

	/** the singleton instance of {@link AudioSystemLoader} */
	private static final AudioSystemLoader LOADER = AudioSystemLoader.getInstance();

	/**
	 * Get default audio capture device of the {@link AudioSystemProvider} with the specified name.
	 *
	 * @param providerName The name of the {@link AudioSystemProvider}.
	 * @return The default audio capture device of the {@link AudioSystemProvider}
	 * or null if the{@link AudioSystemProvider} could not be found.
	 */
	public static AudioInputDevice getDefaultAudioCaptureDevice(String providerName) {
		AudioSystemProvider provider = LOADER.getProvider(providerName);

		return isNull(provider) ? null : provider.getDefaultInputDevice();
	}

	/**
	 * Get default audio playback device of the {@link AudioSystemProvider} with the specified name.
	 *
	 * @param providerName The name of the {@link AudioSystemProvider}.
	 * @return The default audio playback device of the {@link AudioSystemProvider}
	 * or null if the{@link AudioSystemProvider} could not be found..
	 */
	public static AudioOutputDevice getDefaultAudioPlaybackDevice(String providerName) {
		AudioSystemProvider provider = LOADER.getProvider(providerName);

		return isNull(provider) ? null : provider.getDefaultOutputDevice();
	}

	/**
	 * Get all available audio capture devices of the {@link AudioSystemProvider} with the specified name.
	 *
	 * @param providerName The name of the {@link AudioSystemProvider}.
	 * @return All available audio capture devices of the {@link AudioSystemProvider}.
	 */
	public static AudioInputDevice[] getAudioCaptureDevices(String providerName) {
		AudioSystemProvider provider = LOADER.getProvider(providerName);

		return isNull(provider) ? new AudioInputDevice[0] : provider.getInputDevices();
	}

	/**
	 * Get all available audio playback devices of the {@link AudioSystemProvider} with the specified name.
	 *
	 * @param providerName The name of the {@link AudioSystemProvider}.
	 * @return All available audio playback devices of the {@link AudioSystemProvider}.
	 */
	public static AudioOutputDevice[] getAudioPlaybackDevices(String providerName) {
		AudioSystemProvider provider = LOADER.getProvider(providerName);

		return isNull(provider) ? new AudioOutputDevice[0] : provider.getOutputDevices();
	}

	/**
	 * Checks if an available audio capture devices of the {@link AudioSystemProvider} with the {@code providerName}
	 * has the same name as the specified {@code deviceName}.
	 *
	 * @param providerName The name of the {@link AudioSystemProvider}.
	 * @param deviceName The name of the device.
	 * @return {@code true} if an available audio capture device has the same name as the specified {@code deviceName},
	 * otherwise {@code false}.
	 */
	public static boolean hasAudioCaptureDevice(String providerName, String deviceName) {
		if (isNull(deviceName)) {
			return false;
		}

		return Arrays.stream(getAudioCaptureDevices(providerName))
				.anyMatch(device -> device.getName().equals(deviceName));
	}

	/**
	 * Get an {@link AudioInputDevice} with the specified device name that is registered
	 * with the given audio system provider.
	 *
	 * @param providerName The audio system provider name.
	 * @param deviceName   The audio capture device name.
	 *
	 * @return the retrieved {@link AudioInputDevice} or null if the capture device could not be found.
	 */
	public static AudioInputDevice getAudioInputDevice(String providerName, String deviceName) {
		AudioSystemProvider provider = LOADER.getProvider(providerName);

		if (isNull(provider)) {
			throw new NullPointerException("Audio provider is not available: " + providerName);
		}

		AudioInputDevice inputDevice = provider.getInputDevice(deviceName);

		if (isNull(inputDevice)) {
			throw new NullPointerException("Audio device is not available: " + deviceName);
		}

		return inputDevice;
	}

	/**
	 * Get an {@link AudioOutputDevice} with the specified device name that is
	 * registered with the given audio system provider.
	 *
	 * @param providerName The audio system provider name.
	 * @param deviceName   The audio playback device name.
	 *
	 * @return the retrieved {@link AudioOutputDevice} or null if the playback device could not be found.
	 */
	public static AudioOutputDevice getAudioOutputDevice(String providerName, String deviceName) {
		AudioSystemProvider provider = LOADER.getProvider(providerName);

		if (isNull(provider)) {
			provider = LOADER.getProvider("Java Sound");
		}

		AudioOutputDevice outputDevice = provider.getOutputDevice(deviceName);

		if (outputDevice == null) {
			// Get next best device.
			for (AudioOutputDevice device : provider.getOutputDevices()) {
				if (device != null) {
					return device;
				}
			}
		}

		return outputDevice;
	}

	/**
	 * Retrieve all supported audio codecs by the system.
	 *
	 * @return an array of names of supported audio codecs.
	 */
	public static String[] getSupportedAudioCodecs() {
		return AudioCodecLoader.getInstance().getProviderNames();
	}

	/**
	 * Compute the number of bytes per second that the specified audio format will require.
	 *
	 * @param audioFormat The audio format.
	 *
	 * @return The required number of bytes per second.
	 */
	public static int getBytesPerSecond(AudioFormat audioFormat) {
		return Math.round(audioFormat.getSampleRate() * audioFormat
				.getBytesPerSample() * audioFormat.getChannels());
	}

	/**
	 * Pack two sequential bytes into a {@code short} value according to the specified endianness.
	 *
	 * @param bytes     The bytes to pack, must of size 2.
	 * @param bigEndian True to pack with big-endian order, false to pack with
	 *                  little-endian order.
	 *
	 * @return two packed bytes as an {@code short} value.
	 */
	public static int toShort(byte[] bytes, boolean bigEndian) {
		if (bytes.length == 1) {
			return bytes[0];
		}

		if (bigEndian) {
			return ((bytes[0] << 8) | (0xff & bytes[1]));
		}
		else {
			return ((bytes[1] << 8) | (0xff & bytes[0]));
		}
	}

	/**
	 * Convert the specified normalized float value to an short value.
	 *
	 * @param value The normalized float value to convert.
	 *
	 * @return an short value.
	 */
	public static int toShort(double value) {
		return (int) (value * Short.MAX_VALUE);
	}

	/**
	 * Convert the specified integer value to a normalized float value in the range of [0,1].
	 *
	 * @param value The integer value to convert.
	 * @param frameSize The sample size in bytes.
	 * @param signed True to respect the sign bit, false otherwise.
	 *
	 * @return a normalized float value.
	 */
	public static float getNormalizedSampleValue(int value, int frameSize, boolean signed) {
		float relValue;
		int maxValue;

		if (signed) {
			maxValue = (frameSize == 2) ? Short.MAX_VALUE : Byte.MAX_VALUE;
		}
		else {
			maxValue = (frameSize == 2) ? 0xffff : 0xff;
		}

		relValue = (float) value / maxValue;

		return relValue;
	}

}
