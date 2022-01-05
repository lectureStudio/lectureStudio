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
import java.util.function.Consumer;

import org.lecturestudio.core.geometry.Dimension2D;

/**
 * Base camera implementation that manages the state and camera format.
 *
 * @author Alex Andres
 */
public abstract class AbstractCamera implements Camera {

	/** Represents the current camera state: capturing or not. */
	protected final AtomicBoolean open = new AtomicBoolean(false);

	/** The current camera and image format. */
	protected CameraFormat format;

	/** Supported formats by the camera, may be default or real supported ones. */
	protected CameraFormat[] formats;

	/** The desired image size the captured frame should be scaled to. */
	protected Dimension2D imageSize;

	/** The captured image callback. */
	protected Consumer<BufferedImage> imageConsumer;


	@Override
	public void setFormat(CameraFormat format) {
		final boolean opened = open.get();

		if (opened) {
			try {
				close();
			}
			catch (CameraException e) {
				throw new RuntimeException(e);
			}
		}

		this.format = format;

		if (opened) {
			try {
				open();
			}
			catch (CameraException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public CameraFormat getFormat() {
		return format;
	}

	@Override
	public boolean isOpened() {
		return open.get();
	}

	@Override
	public CameraFormat getHighestFormat(double fps) {
		CameraFormat[] formats = getSupportedFormats();

		if (formats == null || formats.length < 1) {
			return null;
		}

		CameraFormat highest = formats[0];

		for (CameraFormat format : formats) {
			if (format.getFrameRate() >= fps && format.compareTo(highest) > -1) {
				highest = format;
			}
		}

		return highest;
	}

	@Override
	public void setImageSize(Dimension2D size) {
		this.imageSize = size;
	}

	@Override
	public void setImageConsumer(Consumer<BufferedImage> consumer) {
		imageConsumer = consumer;
	}

	@Override
	public int compareTo(Camera o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Camera other = (Camera) obj;

		return getName().equals(other.getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
