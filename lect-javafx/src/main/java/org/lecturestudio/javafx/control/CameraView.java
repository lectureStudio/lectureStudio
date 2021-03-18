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

package org.lecturestudio.javafx.control;

import static java.util.Objects.nonNull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Rectangle2D;

public class CameraView extends Control {

	private static final String DEFAULT_STYLE_CLASS = "camera-view";

	/** Camera to capture images. */
	private final ObjectProperty<Camera> camera = new SimpleObjectProperty<>();

	/** Camera image capture format. */
	private final ObjectProperty<CameraFormat> cameraFormat = new SimpleObjectProperty<>();

	/** User-defined capture rectangle. */
	private final ObjectProperty<Rectangle2D> captureRect = new SimpleObjectProperty<>();

	/** Represents the current camera capturing state. */
	private final ReadOnlyBooleanWrapper capture = new ReadOnlyBooleanWrapper();


	public CameraView() {
		initialize();
	}

	public final ObjectProperty<Camera> cameraProperty() {
		return camera;
	}

	public Camera getCamera() {
		return cameraProperty().get();
	}

	public void setCamera(Camera camera) {
		Camera oldCamera = getCamera();

		if (nonNull(camera) && nonNull(oldCamera) && !oldCamera.getName().equals(camera.getName())) {
			if (oldCamera.isOpened()) {
				stopCapture();
			}
		}

		cameraProperty().set(camera);
	}

	public final ObjectProperty<CameraFormat> cameraFormatProperty() {
		return cameraFormat;
	}

	public CameraFormat getCameraFormat() {
		return cameraFormatProperty().get();
	}

	public synchronized void setCameraFormat(CameraFormat format) {
		if (isCapturing()) {
			stopCapture();
		}

		if (nonNull(getCamera())) {
			getCamera().setFormat(format);
		}

		cameraFormatProperty().set(format);
	}

	public final ObjectProperty<Rectangle2D> captureRectProperty() {
		return captureRect;
	}

	public Rectangle2D getCaptureRect() {
		return captureRectProperty().get();
	}

	public void setCaptureRect(Rectangle2D rect) {
		captureRectProperty().set(rect);
	}

	public final ReadOnlyBooleanProperty captureProperty() {
		return capture.getReadOnlyProperty();
	}

	public boolean isCapturing() {
		return capture.get();
	}

	public void startCapture() {
		capture.set(true);
	}

	public void stopCapture() {
		capture.set(false);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new CameraViewSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}

}
