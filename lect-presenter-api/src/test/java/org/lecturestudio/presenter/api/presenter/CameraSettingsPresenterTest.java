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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.AspectRatio;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraDriver;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.view.CameraSettingsView;

class CameraSettingsPresenterTest extends PresenterTest {

	private CameraSettingsMockView view;


	@BeforeEach
	void setup() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		config.getStreamConfig().setCameraName("dummy");

		view = new CameraSettingsMockView();

		CameraService camService = new CameraService(new CameraDriverMock());

		CameraSettingsPresenter presenter = new CameraSettingsPresenter(context, view, camService);
		presenter.initialize();
	}

	@Test
	void testReset() {
		view.resetAction.execute();

		StreamConfiguration config = ((PresenterConfiguration) context.getConfiguration()).getStreamConfig();
		StreamConfiguration defaultConfig = new DefaultConfiguration().getStreamConfig();

		assertEquals(defaultConfig.getCameraName(), config.getCameraName());
		assertEquals(defaultConfig.getCameraCodecConfig().getViewRect(), config.getCameraCodecConfig().getViewRect());
	}



	private static class CameraDriverMock implements CameraDriver {

		@Override
		public Camera[] getCameras() {
			return new Camera[0];
		}
	}



	private static class CameraSettingsMockView implements CameraSettingsView {

		Action resetAction;


		@Override
		public void setCameraName(StringProperty cameraName) {
			assertNotNull(cameraName);
			assertEquals("dummy", cameraName.get());
		}

		@Override
		public void setCameraNames(String[] cameraNames) {
		}

		@Override
		public void setCameraFormats(CameraFormat[] cameraFormats) {

		}

		@Override
		public void setCameraViewRect(ObjectProperty<Rectangle2D> viewRect) {

		}

		@Override
		public void setCameraAspectRatio(AspectRatio ratio) {

		}

		@Override
		public void setCameraAspectRatios(AspectRatio[] ratios) {

		}

		@Override
		public void setOnCameraAspectRatioChanged(ConsumerAction<AspectRatio> action) {

		}

		@Override
		public void setOnViewVisible(ConsumerAction<Boolean> action) {

		}

		@Override
		public void setCamera(Camera camera) {

		}

		@Override
		public void setCameraFormat(CameraFormat cameraFormat) {

		}

		@Override
		public void startCameraPreview() {

		}

		@Override
		public void stopCameraPreview() {

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