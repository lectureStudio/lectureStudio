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

package org.lecturestudio.presenter.javafx.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.AspectRatio;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.camera.CameraProfile;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.javafx.beans.ConvertibleObjectProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.beans.converter.CameraFormatConverter;
import org.lecturestudio.javafx.control.CameraView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.CameraSettingsView;

@FxmlView(name = "camera-settings", presenter = org.lecturestudio.presenter.api.presenter.CameraSettingsPresenter.class)
public class FxCameraSettingsView extends GridPane implements CameraSettingsView {

	@FXML
	private ComboBox<String> camerasCombo;

	@FXML
	private ComboBox<AspectRatio> cameraFormatsCombo;

	@FXML
	private ComboBox<CameraProfile> cameraResolutionsCombo;

	@FXML
	private CameraView cameraView;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;

	private ConsumerAction<Boolean> viewVisibleAction;


	public FxCameraSettingsView() {
		super();
	}

	@Override
	public void setCameraName(StringProperty cameraName) {
		camerasCombo.valueProperty().bindBidirectional(new LectStringProperty(cameraName));
	}

	@Override
	public void setCameraNames(String[] cameraNames) {
		if (isNull(cameraNames)) {
			return;
		}

		FxUtils.invoke(() -> {
			camerasCombo.getItems().setAll(cameraNames);
		});
	}

	@Override
	public void setCameraProfile(CameraProfile cameraProfile) {

	}

	@Override
	public void setCameraProfiles(CameraProfile[] cameraProfiles) {
		FxUtils.invoke(() -> {
			cameraResolutionsCombo.getItems().setAll(cameraProfiles);
		});
	}

	@Override
	public void setCameraViewRect(ObjectProperty<Rectangle2D> viewRect) {
		Converter<Rectangle2D, CameraFormat> cameraFormatConv = new CameraFormatConverter(viewRect);
		ConvertibleObjectProperty<Rectangle2D, CameraFormat> property = new ConvertibleObjectProperty<>(viewRect, cameraFormatConv);

//		cameraResolutionsCombo.valueProperty().bindBidirectional(property);

		cameraView.captureRectProperty().bindBidirectional(new LectObjectProperty<>(viewRect));
	}

	@Override
	public void setCameraAspectRatio(AspectRatio ratio) {
		FxUtils.invoke(() -> {
			cameraFormatsCombo.getSelectionModel().select(ratio);
		});
	}

	@Override
	public void setCameraAspectRatios(AspectRatio[] ratios) {
		FxUtils.invoke(() -> {
			cameraFormatsCombo.getItems().setAll(ratios);
		});
	}

	@Override
	public void setOnCameraAspectRatio(ConsumerAction<AspectRatio> action) {
		cameraFormatsCombo.valueProperty().addListener((observable, oldRatio, newRatio) -> {
			executeAction(action, newRatio);
		});
	}

	@Override
	public void setOnCameraProfile(ConsumerAction<CameraProfile> action) {

	}

	@Override
	public void setOnViewVisible(ConsumerAction<Boolean> action) {
		this.viewVisibleAction = action;
	}

	@Override
	public void setCamera(Camera camera) {
		cameraView.setCamera(camera);
	}

	@Override
	public void setCameraFormat(CameraFormat cameraFormat) {
		cameraView.setCameraFormat(cameraFormat);
	}

	@Override
	public void setCameraError(String errorMessage) {

	}

	@Override
	public void startCameraPreview() {
		cameraView.startCapture();
	}

	@Override
	public void stopCameraPreview() {
		cameraView.stopCapture();
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@FXML
	private void initialize() {
		// Handle camera capture state.
		sceneProperty().addListener((observable, oldScene, newScene) -> {
			boolean capture = nonNull(newScene) && getParent().isVisible();

			if (nonNull(oldScene)) {
				executeAction(viewVisibleAction, capture);
			}
			if (nonNull(newScene)) {
				executeAction(viewVisibleAction, capture);
			}
		});

		parentProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Parent> observable, Parent oldParent, Parent newParent) {
				if (nonNull(newParent)) {
					parentProperty().removeListener(this);

					newParent.visibleProperty().addListener(observable1 -> {
						boolean capture = nonNull(getParent()) && getParent().isVisible();

						executeAction(viewVisibleAction, capture);
					});
				}
			}
		});
	}

}
