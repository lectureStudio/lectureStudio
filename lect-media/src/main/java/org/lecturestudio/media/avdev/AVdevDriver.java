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

package org.lecturestudio.media.avdev;

import org.lecturestudio.avdev.VideoCaptureDevice;
import org.lecturestudio.avdev.VideoDeviceManager;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * AVdev camera driver implementation.
 *
 * @author Alex Andres
 */
public class AVdevDriver implements CameraDriver {

	/** Maintain all connected cameras for consistency. */
	private final TreeSet<Camera> cameras = new TreeSet<>();


	@Override
	public Camera[] getCameras() {
		List<Camera> cameraList = new ArrayList<>();

		VideoDeviceManager manager = VideoDeviceManager.getInstance();
		List<VideoCaptureDevice> devices = manager.getVideoCaptureDevices();

		if (devices != null) {
			for (VideoCaptureDevice device : devices) {
				cameraList.add(new AVdevCamera(device));
			}
		}

		// If camera is gone, perform intersection on set.
		if (cameras.size() > cameraList.size()) {
			cameras.retainAll(cameraList);
		}
		else {
			cameras.addAll(cameraList);
		}

		return cameras.toArray(new Camera[0]);
	}

}
