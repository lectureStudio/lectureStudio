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

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The FrameGrabber asynchronously captures video frames from a camera and
 * writes them to the {@link FrameGrabberCallback}.
 *
 * @author Alex Andres
 */
public class FrameGrabber implements Thread.UncaughtExceptionHandler {

	private final static Logger LOG = LogManager.getLogger(FrameGrabber.class);

	/** The callback that receives the captured frames. */
	private FrameGrabberCallback callback;

	/** The camera that captures the frames. */
	private Camera camera;

	/** The format th. */
	private CameraFormat format;

	/** Represents the current camera capturing state. */
	private AtomicBoolean running = new AtomicBoolean(false);

	/* Capturing thread. */
	private Thread captureThread;


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
			captureThread = new Thread(new CaptureTask(), "Camera Capture Thread");
			captureThread.setDaemon(true);
			captureThread.setUncaughtExceptionHandler(this);
			captureThread.start();
		}
	}

	/**
	 * Stop capturing video frames from the camera and release all assigned
	 * resources.
	 */
	public void stop() {
		if (running.get() && captureThread != null) {
			// Perform clean shutdown.
			try {
				if (!captureThread.isInterrupted()) {
					captureThread.join(1000);
				}
			}
			catch (InterruptedException e) {
				// Ignore
			}

			captureThread = null;
		}

		running.set(false);

		callback = null;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOG.error("Exception in thread " + t.getName(), e);
	}



	/**
	 * The frame capture task.
	 */
	private class CaptureTask implements Runnable {

		@Override
		public void run() {
			camera.setFormat(format);

			if (!camera.isOpened()) {
				try {
					camera.open();
				}
				catch (CameraException e) {
					e.printStackTrace();
				}
			}

			while (camera.isOpened() && running.get()) {
				BufferedImage image = camera.getImage();

				if (image != null && callback != null) {
					callback.onFrame(image);
				}
			}

			try {
				camera.close();
			}
			catch (CameraException e) {
				LOG.error("Close camera failed.", e);
			}

			stop();
		}
	}

}
