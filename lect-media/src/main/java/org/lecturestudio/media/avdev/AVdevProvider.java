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

package org.lecturestudio.media.avdev;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lecturestudio.avdev.AudioCaptureDevice;
import org.lecturestudio.avdev.AudioDeviceManager;
import org.lecturestudio.avdev.AudioPlaybackDevice;
import org.lecturestudio.avdev.Device;
import org.lecturestudio.avdev.HotplugListener;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.audio.bus.event.AudioDeviceHotplugEvent;
import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.system.AbstractSoundSystemProvider;

/**
 * AVdev sound system provider implementation.
 *
 * @author Alex Andres
 */
public class AVdevProvider extends AbstractSoundSystemProvider {

	/**
	 * Create a new AVdevProvider instance.
	 */
	public AVdevProvider() {
		AudioDeviceManager manager = AudioDeviceManager.getInstance();

		if (manager != null) {
			HotplugListener listener = new HotplugListener() {

				@Override
				public void deviceDisconnected(Device device) {
					AudioBus.post(new AudioDeviceHotplugEvent(device.getName(),
							AudioDeviceHotplugEvent.Type.Disconnected));
				}

				@Override
				public void deviceConnected(Device device) {
					AudioBus.post(new AudioDeviceHotplugEvent(device.getName(),
							AudioDeviceHotplugEvent.Type.Connected));
				}
			};
			manager.attachHotplugListener(listener);
		}
	}

	@Override
	public AudioInputDevice getDefaultInputDevice() {
		AudioDeviceManager manager = AudioDeviceManager.getInstance();
		AudioCaptureDevice device = manager.getDefaultAudioCaptureDevice();

		return new AVdevAudioInputDevice(device);
	}

	@Override
	public AudioOutputDevice getDefaultOutputDevice() {
		AudioDeviceManager manager = AudioDeviceManager.getInstance();
		AudioPlaybackDevice device = manager.getDefaultAudioPlaybackDevice();

		return new AVdevAudioOutputDevice(device);
	}

	@Override
	public AudioInputDevice[] getInputDevices() {
		List<AudioInputDevice> inputDevices = new ArrayList<>();

		AudioDeviceManager manager = AudioDeviceManager.getInstance();
		List<AudioCaptureDevice> devices = manager.getAudioCaptureDevices();

		for (AudioCaptureDevice dev : devices) {
			Optional<AudioInputDevice> result = inputDevices.stream()
					.filter(device -> device.getName().equals(dev.getName()))
					.findAny();

			if (result.isEmpty()) {
				inputDevices.add(new AVdevAudioInputDevice(dev));
			}
		}

		return inputDevices.toArray(new AudioInputDevice[0]);
	}

	@Override
	public AudioOutputDevice[] getOutputDevices() {
		List<AudioOutputDevice> outputDevices = new ArrayList<>();

		AudioDeviceManager manager = AudioDeviceManager.getInstance();
		List<AudioPlaybackDevice> devices = manager.getAudioPlaybackDevices();

		for (AudioPlaybackDevice dev : devices) {
			outputDevices.add(new AVdevAudioOutputDevice(dev));
		}

		return outputDevices.toArray(new AudioOutputDevice[0]);
	}

	@Override
	public String getProviderName() {
		return "AVdev";
	}

}
