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

package org.lecturestudio.presenter.api.recording;

import java.util.List;

import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.system.AbstractSoundSystemProvider;

/**
 * Dummy sound system provider implementation.
 *
 * @author Alex Andres
 */
public class DummyAudioProvider extends AbstractSoundSystemProvider {

	@Override
	public AudioInputDevice getDefaultInputDevice() {
		return new DummyAudioInputDevice(null);
	}

	@Override
	public AudioOutputDevice getDefaultOutputDevice() {
		return new DummyAudioOutputDevice();
	}

	@Override
	public AudioInputDevice[] getInputDevices() {
		return List.of(new DummyAudioInputDevice(null))
				.toArray(new DummyAudioInputDevice[0]);
	}

	@Override
	public AudioOutputDevice[] getOutputDevices() {
		return List.of(new DummyAudioOutputDevice())
				.toArray(new DummyAudioOutputDevice[0]);
	}

	@Override
	public String getProviderName() {
		return "Dummy";
	}
}
