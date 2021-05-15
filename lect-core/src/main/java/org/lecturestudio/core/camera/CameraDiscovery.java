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

import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.camera.bus.CameraBus;

/**
 * The {@link CameraDiscovery} runs an background task to discover connected cameras and
 * check if an camera got disconnected. If a camera got connected or
 * disconnected an {@link CameraEvent} will be published on the camera-bus.
 *
 * @author Alex Andres
 */
public final class CameraDiscovery {

	/* Scan every 3 seconds. */
	private static final int DISCOVERY_INTERVAL = 3000;

	/* Represents the current discovery state: discovering or not. */
	private final AtomicBoolean open = new AtomicBoolean(false);

	/* Scheduled discovery executor. */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	/* Current discovery task that may be cancelled. */
	private ScheduledFuture<?> scheduledTask;

	/* Current camera driver that retrieves connected cameras. */
	private CameraDriver cameraDriver;

	/* Recently seen connected cameras. */
	private final TreeSet<Camera> lastCameras = new TreeSet<>();


	/**
	 * Start the camera discovery task with the specified {@link CameraDriver}.
	 *
	 * @param driver The {@link CameraDriver} that observes connected cameras.
	 */
	public void start(CameraDriver driver) {
	  	if (open.compareAndSet(false, true)) {
		    cameraDriver = driver;
		    scheduledTask = scheduler.scheduleWithFixedDelay(new DiscoverTask(),
					DISCOVERY_INTERVAL, DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);

		    if (scheduledTask.isCancelled())
			    open.set(false);
	    }
	}

	/**
	 * Stop the camera discovery task.
	 */
	public void stop() {
		if (open.compareAndSet(true, false)) {
			if (!scheduledTask.isCancelled())
				scheduledTask.cancel(false);
		}
	}

	/**
	 * Check if the camera discovery task is running or not.
	 *
	 * @return True if the task is running, false otherwise.
	 */
	public boolean isRunning() {
		return open.get();
	}



	private class DiscoverTask implements Runnable {

		@Override
		public void run() {
			Camera[] cameraList = cameraDriver.getCameras();

			synchronized (lastCameras) {
				boolean connected = lastCameras.size() < cameraList.length;
				if (lastCameras.size() == cameraList.length) {
					int oldHash = 0;
					int newHash = 0;

					for (Camera oldCamera : lastCameras.toArray(new Camera[0])) {
						oldHash ^= oldCamera.getName().hashCode();
					}
					for (Camera newCamera : cameraList) {
						newHash ^= newCamera.getName().hashCode();
					}

					if (oldHash != newHash) {
						update(cameraList, CameraEvent.Type.Both);
					}
				}
				else if (connected) {
					update(cameraList, CameraEvent.Type.Connected);
				}
				else {
					update(cameraList, CameraEvent.Type.Disconnected);
				}
			}
		}

		private void update(Camera[] cameraList, CameraEvent.Type type) {
			// If camera is gone, perform intersection on set.
			if (type == CameraEvent.Type.Disconnected) {
				lastCameras.retainAll(Arrays.asList(cameraList));
			}
			else {
				lastCameras.addAll(Arrays.asList(cameraList));
			}

			// Publish event.
			CameraBus.post(new CameraEvent(lastCameras.toArray(new Camera[0]), type));
		}
	}

}
