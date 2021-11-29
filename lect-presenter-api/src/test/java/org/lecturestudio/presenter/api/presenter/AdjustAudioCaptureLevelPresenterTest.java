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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.bus.event.AudioSignalEvent;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.AdjustAudioCaptureLevelView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdjustAudioCaptureLevelPresenterTest extends PresenterTest {

	private AdjustAudioCaptureLevelPresenter presenter;

	private AdjustAudioCaptureLevelMockView view;


	@BeforeEach
	void setup() {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");

		view = new AdjustAudioCaptureLevelMockView();

		presenter = new AdjustAudioCaptureLevelPresenter(context, view);
		presenter.initialize();
	}

	@AfterEach
	void dispose() {
		presenter.close();
		presenter.destroy();
	}

	@Test
	void testAudioCaptureLevel() {
		view.beginAction.execute();
		presenter.onEvent(new AudioSignalEvent(0.77));
		view.finishAction.execute();

		AudioConfiguration config = context.getConfiguration().getAudioConfig();

		assertEquals(0.77, config.getRecordingVolume(config.getCaptureDeviceName()).doubleValue());
	}



	private static class AdjustAudioCaptureLevelMockView implements AdjustAudioCaptureLevelView {

		Action beginAction;

		Action finishAction;


		@Override
		public void setAudioLevel(double value) {
			assertEquals(0.77, value);
		}

		@Override
		public void setAudioLevelCaptureStarted(boolean started) {
			assertTrue(started);
		}

		@Override
		public void setCaptureDeviceName(String name) {
			assertEquals("dummy", name);
		}

		@Override
		public void setOnBegin(Action action) {
			assertNotNull(action);

			beginAction = action;
		}

		@Override
		public void setOnCancel(Action action) {
			assertNotNull(action);
		}

		@Override
		public void setOnFinish(Action action) {
			assertNotNull(action);

			finishAction = action;
		}

	}

}