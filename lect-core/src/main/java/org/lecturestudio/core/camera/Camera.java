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
import java.util.function.Consumer;

import org.lecturestudio.core.geometry.Dimension2D;

/**
 * A Camera is used to retrieve frames from an camera device for video encoding
 * or showing a preview of the camera content to capture.
 *
 * @author Alex Andres
 */
public interface Camera extends Comparable<Camera> {

	/**
	 * Get the name of the camera.
	 *
	 * @return the name of the camera.
	 */
	String getName();

	/**
	 * Get the system device descriptor of the camera.
	 *
	 * @return the system device descriptor.
	 */
	String getDeviceDescriptor();

	/**
	 * Retrieve all supported camera formats describing the type of pictures
	 * the camera will produce.
	 *
	 * @return an array of all supported camera formats.
	 */
	CameraFormat[] getSupportedFormats();

	/**
	 * Get the current camera format with which the camera captures or is about
	 * to capture the frames.
	 *
	 * @return the current camera format.
	 */
	CameraFormat getFormat();

	/**
	 * Set the camera format with which the camera should capture the frames. If
	 * the camera is already capturing, this method call has no effects.
	 *
	 * @param format The new camera format.
	 */
	void setFormat(CameraFormat format);

	/**
	 * Set the image size to which the captured frame should be scaled. Calling
	 * this method is useful when the captured frame is rendered by an UI
	 * framework and fast scaling is desired.
	 *
	 * @param size The size of the image.
	 */
	void setImageSize(Dimension2D size);

	/**
	 * Retrieve a camera format with the highest image resolution the camera can
	 * capture with the specified frames per second.
	 *
	 * @param fps The frames per second.
	 *
	 * @return a camera format with the highest image resolution.
	 */
	CameraFormat getHighestFormat(double fps);

	/**
	 * Set the captured image callback that is notified when a new image has
	 * been captured by the camera.
	 *
	 * @param consumer The captured image callback.
	 */
	void setImageConsumer(Consumer<BufferedImage> consumer);

	/**
	 * Check if the camera is opened and capturing.
	 *
	 * @return true if the camera is opened and capturing, false otherwise.
	 */
	boolean isOpened();

	/**
	 * Start capturing video frames with the configured camera format.
	 *
	 * @throws CameraException If the camera device failed to start capturing.
	 */
	void open() throws CameraException;

	/**
	 * Start capturing video frames and release all assigned resources.
	 *
	 * @throws CameraException If the camera device failed to stop capturing.
	 */
	void close() throws CameraException;

}
