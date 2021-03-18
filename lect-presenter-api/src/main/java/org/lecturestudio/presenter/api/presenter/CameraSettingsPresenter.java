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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.camera.AspectRatio;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.camera.CameraFormats;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.view.CameraSettingsView;

public class CameraSettingsPresenter extends Presenter<CameraSettingsView> {

	private final CameraService camService;

	private final StreamConfiguration streamConfig;

	private boolean capture;


	@Inject
	CameraSettingsPresenter(ApplicationContext context, CameraSettingsView view, CameraService camService) {
		super(context, view);

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		this.camService = camService;
		this.streamConfig = config.getStreamConfig();
	}

	@Override
	public void initialize() {
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		if (isNull(cameraConfig.getViewRect())) {
			setDefaultViewRect();
		}
		if (isNull(streamConfig.getCameraName())) {
			setDefaultCameraName();
		}

		AspectRatio aspectRatio = getAspectRatio(cameraConfig.getViewRect());
		Camera camera = camService.getCamera(streamConfig.getCameraName());

		setCameraAspectRatio(aspectRatio);

		view.setCameraNames(camService.getCameraNames());
		view.setCameraName(streamConfig.cameraNameProperty());
		view.setCameraAspectRatios(AspectRatio.values());
		view.setCameraAspectRatio(aspectRatio);
		view.setCameraViewRect(cameraConfig.viewRectProperty());
		view.setOnCameraAspectRatioChanged(this::setCameraAspectRatio);
		view.setOnReset(this::reset);

		if (isNull(camera) || !camera.isOpened()) {
			// Disable camera control if the camera is already opened, e.g. streaming.
			view.setOnViewVisible(this::captureCamera);
		}

		setViewCamera(streamConfig.getCameraName());

		streamConfig.cameraNameProperty().addListener((observable, oldCamera, newCamera) -> {
			if (isNull(newCamera)) {
				setDefaultCameraName();
				return;
			}

			setViewCamera(newCamera);
		});
	}

	private void captureCamera(boolean capture) {
		if (this.capture == capture) {
			return;
		}

		this.capture = capture;

		if (capture) {
			view.startCameraPreview();
		}
		else {
			view.stopCameraPreview();
		}
	}

	private void setViewCamera(String cameraName) {
		if (capture) {
			view.stopCameraPreview();
		}

		Camera camera = camService.getCamera(cameraName);

		if (nonNull(camera)) {
			CameraFormat highestFormat = camera.getHighestFormat(30);

			streamConfig.setCameraFormat(highestFormat);

			view.setCamera(camera);

			if (camera.isOpened()) {
				return;
			}

			view.setCameraFormat(highestFormat);

			if (capture) {
				view.startCameraPreview();
			}
		}
	}

	private CameraFormat[] getCameraFormats(AspectRatio ratio) {
		CameraFormat[] formats = null;

		if (ratio == AspectRatio.Standard) {
			formats = CameraFormats.Standard;
		}
		else if (ratio == AspectRatio.Widescreen) {
			formats = CameraFormats.Widescreen;
		}

		CameraFormat highestFormat = streamConfig.getCameraFormat();

		return filterCameraFormats(formats, highestFormat);
	}

	private void setCameraAspectRatio(AspectRatio ratio) {
		CameraFormat[] formats = getCameraFormats(ratio);

		view.setCameraFormats(formats);

		updateViewRect(formats);
	}

	private void updateViewRect(CameraFormat[] formats) {
		if (isNull(formats) || formats.length < 1) {
			return;
		}

		Rectangle2D viewRect = streamConfig.getCameraCodecConfig().getViewRect();

		CameraFormat format = formats[formats.length - 1];

		double x = nonNull(viewRect) ? viewRect.getX() : 0;
		double y = nonNull(viewRect) ? viewRect.getY() : 0;

		streamConfig.getCameraCodecConfig().setViewRect(new Rectangle2D(x, y, format.getWidth(), format.getHeight()));
	}

	private CameraFormat[] filterCameraFormats(CameraFormat[] list, CameraFormat highest) {
		if (isNull(highest)) {
			return list;
		}

		List<CameraFormat> formatList = new ArrayList<>();

		if (nonNull(list)) {
			for (CameraFormat format : list) {
				if (highest.getWidth() >= format.getWidth() && highest.getHeight() >= format.getHeight()) {
					formatList.add(format);
				}
			}
		}

		return formatList.toArray(new CameraFormat[0]);
	}

	private AspectRatio getAspectRatio(Rectangle2D rect) {
		AspectRatio ratio = AspectRatio.Standard;

		if (nonNull(rect)) {
			if (rect.getHeight() / rect.getWidth() < 0.75) {
				ratio = AspectRatio.Widescreen;
			}
		}

		return ratio;
	}

	private void setDefaultCameraName() {
		String[] cameraNames = camService.getCameraNames();

		if (cameraNames.length > 0) {
			// Select first available camera.
			streamConfig.setCameraName(cameraNames[0]);
		}
	}

	private void setDefaultViewRect() {
		view.setCameraAspectRatio(AspectRatio.Standard);

		updateViewRect(getCameraFormats(AspectRatio.Standard));
	}

	public void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		streamConfig.setCameraName(defaultConfig.getStreamConfig().getCameraName());
		streamConfig.getCameraCodecConfig().setViewRect(defaultConfig.getStreamConfig().getCameraCodecConfig().getViewRect());
	}

}
