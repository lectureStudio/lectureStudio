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

package org.lecturestudio.presenter.api.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.FloatProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.MicrophoneSettingsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MicrophoneSettingsPresenterTest extends PresenterTest {

	private MicrophoneSettingsMockView view;


	@BeforeEach
	void setup() {
		AudioConfiguration config = context.getConfiguration().getAudioConfig();
		config.setInputDeviceName("dummy");
		config.setSoundSystem("dummy");

		view = new MicrophoneSettingsMockView();

		MicrophoneSettingsPresenter presenter = new MicrophoneSettingsPresenter(context, view);
		presenter.initialize();
	}

	@Test
	void testReset() {
		view.resetAction.execute();

		AudioConfiguration config = context.getConfiguration().getAudioConfig();
		AudioConfiguration defaultConfig = new DefaultConfiguration().getAudioConfig();

		assertEquals(defaultConfig.getSoundSystem(), config.getSoundSystem());
		assertEquals(defaultConfig.getInputDeviceName(), config.getInputDeviceName());
		assertEquals(defaultConfig.getDefaultRecordingVolume(), config.getDefaultRecordingVolume());
		assertEquals(defaultConfig.getRecordingVolumes(), config.getRecordingVolumes());
	}



	private static class MicrophoneSettingsMockView implements MicrophoneSettingsView {

		Action resetAction;


		@Override
		public void setViewEnabled(boolean enabled) {

		}

		@Override
		public void setAudioCaptureDevice(StringProperty captureDeviceName) {
			assertEquals("dummy", captureDeviceName.get());
		}

		@Override
		public void setAudioCaptureDevices(AudioInputDevice[] captureDevices) {

		}

		@Override
		public void setAudioCaptureLevel(double value) {

		}

		@Override
		public void bindAudioCaptureLevel(FloatProperty levelProperty) {

		}

		@Override
		public void bindTestCaptureEnabled(BooleanProperty enable) {

		}

		@Override
		public void bindTestPlaybackEnabled(BooleanProperty enable) {

		}

		@Override
		public void setOnTestCapture(BooleanProperty recordProperty) {

		}

		@Override
		public void setOnTestPlayback(BooleanProperty playProperty) {

		}

		@Override
		public void setOnAdjustAudioCaptureLevel(Action action) {

		}

		@Override
		public void setOnViewVisible(ConsumerAction<Boolean> action) {

		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnReset(Action action) {
			assertNotNull(action);

			resetAction = action;
		}
	}
}
