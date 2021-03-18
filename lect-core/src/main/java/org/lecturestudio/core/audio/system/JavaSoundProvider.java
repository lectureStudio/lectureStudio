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

package org.lecturestudio.core.audio.system;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.device.JavaSoundInputDevice;
import org.lecturestudio.core.audio.device.JavaSoundOutputDevice;

/**
 * Java-based sound system provider implementation.
 *
 * @author Alex Andres
 */
public class JavaSoundProvider extends AbstractSoundSystemProvider {

	@Override
	public AudioInputDevice getDefaultInputDevice() {
		AudioInputDevice[] devices = getInputDevices();

		return (isNull(devices) || devices.length < 1) ? null : devices[0];
	}

	@Override
	public AudioOutputDevice getDefaultOutputDevice() {
		AudioOutputDevice[] devices = getOutputDevices();

		return (isNull(devices) || devices.length < 1) ? null : devices[0];
	}

	@Override
	public AudioInputDevice[] getInputDevices() {
		List<AudioInputDevice> inputDevices = new ArrayList<>();
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfo) {
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info targetInfo = new Line.Info(TargetDataLine.class);

			if (mixer.isLineSupported(targetInfo)) {
				JavaSoundInputDevice device = new JavaSoundInputDevice(info);
				inputDevices.add(device);
			}
		}

		return inputDevices.toArray(new AudioInputDevice[0]);
	}

	@Override
	public AudioOutputDevice[] getOutputDevices() {
		List<AudioOutputDevice> outputDevices = new ArrayList<>();
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfo) {
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info sourceInfo = new Line.Info(SourceDataLine.class);

			if (mixer.isLineSupported(sourceInfo)) {
				JavaSoundOutputDevice device = new JavaSoundOutputDevice(info);
				outputDevices.add(device);
			}
		}

		return outputDevices.toArray(new AudioOutputDevice[0]);
	}

	@Override
	public String getProviderName() {
		return "Java Sound";
	}

}
