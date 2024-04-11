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

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.StartCamSharingView;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.*;

import static java.util.Objects.nonNull;

public class StartCamSharingPresenter extends Presenter<StartCamSharingView> {
	private final CameraService camService;

	private ScheduledExecutorService executorService;


	private ConsumerAction<Camera> onShareAction;


	private ScheduledFuture<?> future;

	private ObservableList<Camera> inputSources;

	private ObjectProperty<List<Camera>> selectedSource;

	private boolean camsStarted;


	@Inject
	StartCamSharingPresenter(PresenterContext context,
							 StartCamSharingView view,
							 CameraService camService) {
		super(context, view);
		this.camService = camService;

		//TODO: Do something with that
//		context.getConfiguration().getStreamConfig();
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		executorService = Executors.newScheduledThreadPool(1);
		inputSources = new ObservableArrayList<>();
		selectedSource = new ObjectProperty<>();


		future = executorService.scheduleAtFixedRate(() -> {
			updateAvailableCams();
		}, 0, 20, TimeUnit.SECONDS);

		view.setInputSources(inputSources);
		view.bindSelectedSources(selectedSource);

		view.setOnStartAction(this::onShareClicked);
		view.setOnCloseAction(this::close);


		view.setOnViewVisible(this::onViewVisible);


	}

	@Override
	public void close() {
		dispose();

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.setScreenSharingStarted(false);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}


	public void setOnStartAction(ConsumerAction<Camera> action) {
		onShareAction = action;
	}


	private void onShareClicked() {
		dispose();

		if (nonNull(onShareAction)) {
			// Todo update onShareAction to return a list of cameras (A scene can have multiple cameras)
//			onShareAction.execute(selectedSource.get());
		}
	}

	private void dispose() {
		if (nonNull(future) && !future.isCancelled()) {
			future.cancel(true);
		}
		executorService.shutdownNow();

		super.close();
	}

	private void onViewVisible(boolean visible) {

		if (visible == camsStarted) {
			return;
		}

		camsStarted = visible;

		if (visible) {
			view.startCameraPreviews();
		} else {
			view.stopCameraPreviews();
		}

	}


	private void updateAvailableCams() {
		System.out.println("Updating available cams");
		var cams = camService.getCameraDriver().getCameras();

		System.out.println("Cams: " + cams.length);

		for (var cam : cams) {
			if (inputSources.contains(cam)) {
				continue;
			}

			if (nonNull(cam)) {
				inputSources.add(cam);
				System.out.println("Added cam: " + cam.getName());
			}
		}
	}
}