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

package org.lecturestudio.swing.components;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Dimension2D;

/**
 * Simply implementation of JPanel allowing users to render pictures taken with
 * a camera.
 *
 * @author Alex Andres
 */
public class CameraPanel extends JPanel {

    private static final long serialVersionUID = 5792962512394656227L;

	/** Represents the current camera capturing state. */
	private final AtomicBoolean started = new AtomicBoolean(false);

	/** Camera image painter. */
	private CameraView canvas;

    /** Camera to fetch images. */
    private Camera camera;


    /**
     * Creates an empty camera panel.
     */
    public CameraPanel() {
	    initialize();
    }

	/**
	 * Set the camera of which the captured frames should be shown.
	 *
	 * @param camera the camera to capture.
	 */
	public void setCamera(Camera camera) {
		if (isNull(camera)) {
			throw new NullPointerException();
		}

		if (nonNull(this.camera) && !this.camera.getName().equals(camera.getName())) {
			if (this.camera.isOpened()) {
				stopCapture();
			}
		}

		this.camera = camera;
	}

	/**
	 * Set the new capture format.
	 *
	 * @param format the camera capture format.
	 */
	public void setCameraFormat(CameraFormat format) {
		if (nonNull(format)) {
			canvas.clearImage();
			setCanvasSize(format);
		}
		if (nonNull(camera)) {
			boolean capturing = started.get() && camera.isOpened();

			if (capturing) {
				stopCapture();
			}

			camera.setFormat(format);

			if (capturing) {
				startCapture();
			}
		}
	}

	/**
	 * Start camera capturing.
	 */
	public void startCapture() {
		if (started.compareAndSet(false, true)) {
			setCanvasSize(getCameraFormat());

			camera.setImageSize(new Dimension2D(canvas.getWidth(), canvas.getHeight()));
			camera.setImageConsumer(image -> canvas.showImage(image));

			try {
				camera.open();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Stop camera capturing.
	 */
	public void stopCapture() {
		if (started.compareAndSet(true, false)) {
			if (nonNull(camera)) {
				try {
					camera.close();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			canvas.clearImage();
		}
	}

	/**
	 * Set the camera status message.
	 *
	 * @param message The status message.
	 */
	public void setStatusMessage(String message) {
		canvas.setStatusMessage(message);
	}

	protected CameraFormat getCameraFormat() {
		if (isNull(camera)) {
			return null;
		}
		return camera.getFormat();
	}

	/**
	 * Fit canvas size to panel's preferred size, no matter how large the
	 * camera image is.
	 */
    private void setCanvasSize(CameraFormat format) {
		// Set correct aspect ratio to capture format.
		int width = getPreferredSize().width;
		int height = (int) (format.getHeight() / (float) format.getWidth() * width);
		Dimension size = new Dimension(width, height);

		setPreferredSize(size);

		canvas.setPreferredSize(size);
		canvas.setSize(size);
    }

	private void initialize() {
		setLayout(null);
		setBorder(new EmptyBorder(0, 0, 0, 0));

		canvas = new CameraView();

		add(canvas);
	}
}
