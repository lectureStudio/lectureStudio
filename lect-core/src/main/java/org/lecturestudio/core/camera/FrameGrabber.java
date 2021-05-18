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

package org.lecturestudio.core.camera;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The FrameGrabber asynchronously captures video frames from a camera and
 * writes them to the {@link FrameGrabberCallback}.
 *
 * @author Alex Andres
 */
public class FrameGrabber {

	/** The callback that receives the captured frames. */
	private final FrameGrabberCallback callback;

	/** The camera that captures the frames. */
	private final Camera camera;

	/** The format th. */
	private final CameraFormat format;

	/** Represents the current camera capturing state. */
	private final AtomicBoolean running = new AtomicBoolean(false);


	/**
	 * Create a FrameGrabber with the specified frame callback, the camera and
	 * the capturing format.
	 *
	 * @param callback The callback that receives the captured frames.
	 * @param camera   The camera that captures the frames.
	 * @param format   The camera format that describes the format of the
	 *                 frames.
	 */
	public FrameGrabber(FrameGrabberCallback callback, Camera camera, CameraFormat format) {
		this.callback = callback;
		this.camera = camera;
		this.format = format;
	}

	/**
	 * Start capturing video frames from the camera.
	 */
	public void start() {
		if (running.compareAndSet(false, true)) {
			camera.setFormat(format);
			camera.setImageConsumer(callback::onFrame);

			try {
				camera.open();
			}
			catch (CameraException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Stop capturing video frames from the camera and release all assigned
	 * resources.
	 */
	public void stop() {
		if (running.compareAndSet(true, false)) {
			try {
				camera.close();
			}
			catch (CameraException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
