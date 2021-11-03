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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Named;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.StartServices;
import org.lecturestudio.presenter.api.view.StartStreamView;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.service.StreamProviderService;

public class StartStreamPresenter extends Presenter<StartStreamView> {

	private final CameraService camService;

	/** The services to start with the start-action. */
	private StartServices startServices;

	/** The action that is executed when the saving process has been aborted. */
	private ConsumerAction<StartServices> startAction;

	@Inject
	@Named("stream.publisher.api.url")
	private String streamPublisherApiUrl;

	private boolean capture;


	@Inject
	StartStreamPresenter(ApplicationContext context, StartStreamView view,
			CameraService camService) {
		super(context, view);

		this.camService = camService;
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		startServices = new StartServices();
		startServices.startMessenger.set(streamConfig.getMessengerEnabled());
		startServices.startMessenger.addListener((observable, oldValue, newValue) -> {
			streamConfig.setMessengerEnabled(newValue);
		});

		loadCourses();

		setViewCamera(streamConfig.getCameraName());
		captureCamera(true);

		view.setOnStart(this::onStart);
		view.setOnClose(this::close);
	}

	@Override
	public void close() {
		captureCamera(false);

		super.close();

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.setStreamStarted(false);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnStart(ConsumerAction<StartServices> action) {
		startAction = action;
	}

	private void onStart() {
		captureCamera(false);

		super.close();

		if (nonNull(startAction)) {
			startAction.execute(startServices);
		}
	}

	private void loadCourses() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl(streamPublisherApiUrl);

		StreamProviderService streamProviderService = new StreamProviderService(
				parameters, streamConfig::getAccessToken);

		try {
			List<Course> courses = streamProviderService.getCourses();
			Course selectedCourse = streamConfig.getCourse();

			if (isNull(selectedCourse) && !courses.isEmpty()) {
				// Set first available lecture by default.
				streamConfig.setCourse(courses.get(0));
			}
			else if (!courses.contains(selectedCourse)) {
				streamConfig.setCourse(courses.get(0));
			}

			view.setCourses(courses);
			view.setCourse(streamConfig.courseProperty());
			view.setEnableMicrophone(config.getStreamConfig().enableMicrophoneProperty());
			view.setEnableCamera(config.getStreamConfig().enableCameraProperty());
			view.setEnableMessenger(startServices.startMessenger);
		}
		catch (Exception e) {
			view.setCourses(List.of());
			view.setError(context.getDictionary().get("start.stream.service.error"));

			streamConfig.setCourse(null);
		}
	}

	private void captureCamera(boolean capture) {
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
	}

	private void startCameraPreview() {
		CompletableFuture.runAsync(() -> {
			try {
				view.setCameraStatus(context.getDictionary()
						.get("start.stream.camera.starting"));
				view.startCameraPreview();
				view.setCameraStatus(null);
			}
			catch (Throwable e) {
				view.setCameraStatus(context.getDictionary()
						.get("start.stream.camera.unavailable"));
			}
		});
	}

	private void stopCameraPreview() {
		view.stopCameraPreview();
	}

	private void setViewCamera(String cameraName) {
		if (capture) {
			stopCameraPreview();
		}

		Camera camera = camService.getCamera(cameraName);

		if (nonNull(camera)) {
			view.setCamera(camera);

			if (camera.isOpened()) {
				return;
			}

			view.setCameraFormat(camera.getHighestFormat(30));

			if (capture) {
				startCameraPreview();
			}
		}
	}
}