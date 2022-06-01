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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.lecturestudio.core.camera.AspectRatio;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraProfile;
import org.lecturestudio.core.camera.CameraProfiles;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.CameraSettingsView;

public class CameraSettingsPresenter extends Presenter<CameraSettingsView> {

	private final CameraService camService;

	private final StreamConfiguration streamConfig;

	private final AtomicBoolean configure;

	private boolean capture;


	@Inject
	CameraSettingsPresenter(PresenterContext context, CameraSettingsView view, CameraService camService) {
		super(context, view);

		this.camService = camService;
		this.streamConfig = context.getConfiguration().getStreamConfig();
		this.configure = new AtomicBoolean();
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

		Camera camera = camService.getCamera(streamConfig.getCameraName());

		view.setCameraNames(camService.getCameraNames());
		view.setCameraName(streamConfig.cameraNameProperty());
		view.setCameraViewRect(cameraConfig.viewRectProperty());
		view.setOnCameraAspectRatio(this::setCameraAspectRatio);
		view.setOnCameraProfile(this::setCameraProfile);
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
		CompletableFuture.runAsync(() -> {
			if (this.capture == capture) {
				return;
			}

			this.capture = capture;

			if (capture) {
				startCameraPreview();
			}
			else {
				stopCameraPreview();
			}
		});
	}

	private void startCameraPreview() {
		PresenterContext presenterContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		boolean camEnabled = presenterContext.getStreamStarted() &&
				config.getStreamConfig().getCameraEnabled();

		if (camEnabled) {
			// Do not interfere with the running stream.
			view.setCameraError(context.getDictionary()
					.get("camera.settings.camera.unavailable"));
			return;
		}

		try {
			view.setCameraError(context.getDictionary()
					.get("camera.settings.camera.starting"));

			view.startCameraPreview();
			view.setCameraError(null);
		}
		catch (Throwable e) {
			view.setCameraError(context.getDictionary()
					.get("camera.settings.camera.unavailable"));
		}
	}

	private void stopCameraPreview() {
		PresenterContext presenterContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		boolean camEnabled = presenterContext.getStreamStarted() &&
				config.getStreamConfig().getCameraEnabled();

		if (camEnabled) {
			// Do not interfere with the running stream.
			return;
		}

		view.stopCameraPreview();
	}

	private void setViewCamera(final String cameraName) {
		CompletableFuture.runAsync(() -> {
			configure.set(true);

			if (capture) {
				stopCameraPreview();
			}

			Camera camera = camService.getCamera(cameraName);

			if (nonNull(camera)) {
				VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();
				Set<AspectRatio> ratioSet = getSupportedRatios(camera);
				AspectRatio ratio = AspectRatio.forRect(cameraConfig.getViewRect());

				if (!ratioSet.contains(ratio)) {
					ratio = ratioSet.iterator().next();
				}

				CameraProfile[] profiles = CameraProfiles.forRatio(ratio);
				view.setCameraProfiles(profiles);

				updateViewRect(profiles);

				view.setCameraAspectRatios(ratioSet.toArray(AspectRatio[]::new));
				view.setCameraAspectRatio(ratio);

				streamConfig.setCameraFormat(camera.getHighestFormat(30));

				view.setCamera(camera);

				if (camera.isOpened()) {
					configure.set(false);
					return;
				}

				CameraProfile profile = getCameraProfile(profiles);

				if (isNull(profile)) {
					profile = profiles[profiles.length - 1];
				}

				view.setCameraFormat(profile.getFormat());

				if (capture) {
					startCameraPreview();
				}
			}

			configure.set(false);
		});
	}

	private Set<AspectRatio> getSupportedRatios(Camera camera) {
		Set<AspectRatio> ratioSet = new HashSet<>();

		if (nonNull(camera)) {
			for (var format : camera.getSupportedFormats()) {
				AspectRatio ratio = AspectRatio.forRect(
						new Rectangle2D(0, 0, format.getWidth(), format.getHeight()));

				ratioSet.add(ratio);
			}
		}

		return ratioSet;
	}

	private void setCameraAspectRatio(AspectRatio ratio) {
		if (configure.get()) {
			return;
		}

		CompletableFuture.runAsync(() -> {
			if (capture) {
				stopCameraPreview();
			}

			CameraProfile[] profiles = CameraProfiles.forRatio(ratio);

			view.setCameraProfiles(profiles);

			updateViewRect(profiles);

			if (capture) {
				startCameraPreview();
			}
		});
	}

	private void setCameraProfile(CameraProfile profile) {
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();
		Rectangle2D viewRect = cameraConfig.getViewRect();

		double x = nonNull(viewRect) ? viewRect.getX() : 0;
		double y = nonNull(viewRect) ? viewRect.getY() : 0;

		cameraConfig.setBitRate(profile.getBitrate());
		cameraConfig.setViewRect(new Rectangle2D(x, y,
				profile.getFormat().getWidth(),
				profile.getFormat().getHeight()));
	}

	private void updateViewRect(CameraProfile[] profiles) {
		if (isNull(profiles) || profiles.length < 1) {
			return;
		}

		CameraProfile profile = getCameraProfile(profiles);

		if (isNull(profile)) {
			profile = profiles[profiles.length - 1];
		}

		setCameraProfile(profile);

		view.setCameraProfile(profile);
		view.setCameraFormat(profile.getFormat());
	}

	private CameraProfile getCameraProfile(CameraProfile[] profiles) {
		Rectangle2D rect = streamConfig.getCameraCodecConfig().getViewRect();

		if (isNull(rect)) {
			return null;
		}

		return Arrays.stream(profiles).filter(p -> {
			return p.getFormat().getWidth() == rect.getWidth()
					&& p.getFormat().getHeight() == rect.getHeight();
			}).findFirst().orElse(null);
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

		updateViewRect(CameraProfiles.forRatio(AspectRatio.Standard));
	}

	public void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		streamConfig.setCameraName(defaultConfig.getStreamConfig().getCameraName());
		streamConfig.getCameraCodecConfig().setViewRect(defaultConfig.getStreamConfig().getCameraCodecConfig().getViewRect());
	}

}
