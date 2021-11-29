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

package org.lecturestudio.media.camera;

import static java.util.Objects.isNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraDiscovery;
import org.lecturestudio.core.camera.CameraDriver;
import org.lecturestudio.core.camera.CameraException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The CameraService manages CameraDrivers and retrieves connected cameras to
 * the system.
 *
 * @author Alex Andres
 */
@Singleton
public final class CameraService {

	private final static Logger LOG = LogManager.getLogger(CameraService.class);

	/** The singleton instance. */
	private static CameraService instance;

	/** The camera driver that allows access to connected cameras. */
	private CameraDriver driver;

	/** The camera discovery that observes camera connections. */
	private final CameraDiscovery discovery;


	@Inject
	public CameraService(CameraDriver driver) {
		this.discovery = new CameraDiscovery();

		setCameraDriver(driver);
	}

	/**
	 * Get the CameraService singleton instance.
	 *
	 * @return the CameraService instance.
	 */
	public static CameraService get() {
		if (instance == null) {
			instance = new CameraService(null);
		}

		return instance;
	}

	/**
	 * Get the camera driver used by this service.
	 *
	 * @return the camera driver.
	 */
	public CameraDriver getCameraDriver() {
		return driver;
	}

	/**
	 * Get the names of all connected cameras.
	 *
	 * @return an array with the names of all connected cameras.
	 */
	public String[] getCameraNames() {
		Camera[] cameras = getCameraDriver().getCameras();
		String[] names = new String[cameras.length];

		for (int i = 0; i < cameras.length; i++) {
			Camera camera = cameras[i];
			names[i] = camera.getName();
		}

		return names;
	}

	/**
	 * Get a camera with the specified name.
	 *
	 * @param name The name of the camera.
	 *
	 * @return the retrieved camera, or null, if no such camera with the given
	 * name exists.
	 */
	public Camera getCamera(String name) {
		if (isNull(name)) {
			return null;
		}

		Camera[] cameras = getCameraDriver().getCameras();

		for (Camera camera : cameras) {
			if (name.equals(camera.getName())) {
				return camera;
			}
		}

		return null;
	}

	private void setCameraDriver(CameraDriver driver) {
		if (!driver.equals(this.driver)) {
			if (discovery.isRunning()) {
				discovery.stop();
			}

			// Shutdown all cameras of old driver.
			if (this.driver != null) {
				for (Camera camera : this.driver.getCameras()) {
					try {
						camera.close();
					}
					catch (CameraException e) {
						LOG.error("Close camera failed.", e);
					}
				}
			}

			this.driver = driver;
			this.discovery.start(driver);
		}
	}

}
