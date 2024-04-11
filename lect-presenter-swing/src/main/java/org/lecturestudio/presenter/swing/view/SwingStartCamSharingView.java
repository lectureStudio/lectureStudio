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

package org.lecturestudio.presenter.swing.view;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.StartCamSharingView;
import org.lecturestudio.presenter.swing.component.CameraSourceView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.*;
import java.util.List;

import static java.util.Objects.nonNull;

@SwingView(name = "start-cam-sharing")
public class SwingStartCamSharingView extends JPanel implements StartCamSharingView, ListChangeListener<ObservableList<Camera>> {

	private final ResourceBundle resources;

	private JPanel inputsContainer;

	private JButton closeButton;

	private JButton shareButton;

	private ObjectProperty<List<Camera>> selectedSources;

	private ConsumerAction<Boolean> viewVisibleAction;


	@Inject
	SwingStartCamSharingView(ResourceBundle resources) {
		super();

		this.resources = resources;

	}

	@Override
	public void setInputSources(ObservableList<Camera> sources) {
		sources.addListener(this);
	}

	@Override
	public void bindSelectedSources(ObjectProperty<List<Camera>> selectedSources) {
		this.selectedSources = selectedSources;

		selectedSources.addListener((o, oldValue, newValue) -> {
			shareButton.setEnabled(nonNull(newValue) && !newValue.isEmpty());
		});

		shareButton.setEnabled(nonNull(selectedSources.get()) && !selectedSources.get().isEmpty());
	}


	@Override
	public void setOnCloseAction(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnStartAction(Action action) {
		SwingUtils.bindAction(shareButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		inputsContainer.setMinimumSize(new Dimension(600, 220));
		inputsContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		inputsContainer.setLayout(new FlowLayout());

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				executeAction(viewVisibleAction, true);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				executeAction(viewVisibleAction, false);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
		});

		updateNoSources();

	}

	private void onSourceSelected(CameraSourceView cameraSourceView) {
		List<Camera> cameras = Optional.ofNullable(selectedSources.get()).map(ArrayList::new).orElseGet(ArrayList::new);
		Camera sourceViewCamera = cameraSourceView.getCamera();
		boolean selected = cameras.contains(sourceViewCamera);


		if (selected) {
			cameraSourceView.setSelected(false);
			cameras.remove(sourceViewCamera);
		} else {
			cameraSourceView.setSelected(true);
			cameras.add(sourceViewCamera);
		}

		// Make it to an unmodifiable list, so that the list can't be changed from outside
		selectedSources.set(Collections.unmodifiableList(cameras));
	}

	@Override
	public void setOnViewVisible(ConsumerAction<Boolean> action) {
		this.viewVisibleAction = action;
	}

	@Override
	public void startCameraPreviews() {
		System.out.println("Starting camera previews of all cameras");
		for (Component component : inputsContainer.getComponents()) {
			if (component instanceof CameraSourceView cameraSourceView) {
				cameraSourceView.startCam();
			}
		}
	}

	@Override
	public void stopCameraPreviews() {
		for (Component component : inputsContainer.getComponents()) {
			if (component instanceof CameraSourceView cameraSourceView) {
				cameraSourceView.stopCam();
			}
		}
	}


	// CODE FOR THE LISTENER

	private boolean noSources = false;

	private void updateNoSources() {
		if (inputsContainer.getComponents().length == 0 && !noSources) {
			inputsContainer.removeAll();
			inputsContainer.add(new JLabel("No sources available"));
			inputsContainer.revalidate();
			inputsContainer.repaint();
			noSources = true;
			System.out.println("No sources available");
		}
	}

	@Override
	public void listItemsInserted(ObservableList<Camera> list,
								  int startIndex, int itemCount) {
		var screenSource = list.get(startIndex);

		addCameraPreview(screenSource);
	}

	@Override
	public void listItemsRemoved(ObservableList<Camera> list,
								 int startIndex, int itemCount) {

		for (var component : inputsContainer.getComponents()) {
			if (component instanceof CameraSourceView cameraSourceView) {

				if (!list.contains(cameraSourceView.getCamera())) {
//						cameraPreviewPanel.removeSelectionConsumer(selectConsumer);
					cameraSourceView.stopCam();
					inputsContainer.remove(cameraSourceView);
					inputsContainer.revalidate();
					inputsContainer.repaint();
					break;
				}
			}
		}
		updateNoSources();
	}

	@Override
	public void listChanged(ObservableList<Camera> list) {
		addScreenSources(list);
	}

	private void addScreenSources(List<Camera> list) {
		inputsContainer.removeAll();
		if (list.isEmpty()) {
			updateNoSources();
		}

		for (var cam : list) {
			addCameraPreview(cam);
		}
	}

	private void addCameraPreview(Camera camera) {
		// Remove no sources label if it is present
		if (noSources) {
			inputsContainer.removeAll();
			noSources = false;
		}
		CameraSourceView cameraSourceView = new CameraSourceView(resources);
		cameraSourceView.setCamera(camera);
//		cameraSourceView.setMinimumSize(new Dimension(200, 150));
//		cameraSourceView.setMaximumSize(new Dimension(200, 150));
//		cameraSourceView.setPreferredSize(new Dimension(200, 150));

		cameraSourceView.addSelectionConsumer(this::onSourceSelected);


		inputsContainer.add(cameraSourceView);

		inputsContainer.revalidate();
		inputsContainer.repaint();


		System.out.println("Added camera: " + camera.getName());
//		cameraSourceView.startCam();
	}
}