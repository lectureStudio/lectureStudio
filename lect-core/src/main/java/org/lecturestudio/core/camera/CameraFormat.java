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

import com.google.common.base.Objects;

/**
 * A CameraFormat represents the image size and frame rate with which a camera
 * device is capturing frames. The CameraFormat class also implements {@code
 * Comparable} to compare the image size with other formats.
 *
 * @author Alex Andres
 */
public class CameraFormat implements Comparable<CameraFormat> {

    /** The image width. */
	private final int width;

    /** The image height. */
	private final int height;

    /** The capturing frame rate. */
	private final double frameRate;


    /**
     * Create a new {@code CameraFormat} with the image size and frame rate
     * equal {@code zero}.
     */
    public CameraFormat() {
        this(0, 0, 0);
    }

    /**
     * Create a new {@code CameraFormat} with specified image size and frame
     * rate.
     *
     * @param width     The image width.
     * @param height    The image height.
     * @param frameRate The capturing frame rate.
     */
	public CameraFormat(int width, int height, double frameRate) {
		this.width = width;
		this.height = height;
		this.frameRate = frameRate;
	}

    /**
     * Get the image height.
     *
     * @return thr image height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the image width.
     *
     * @return the image width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the frame rate.
     *
     * @return the frame rate.
     */
	public double getFrameRate() {
		return frameRate;
	}

	@Override
	public int compareTo(CameraFormat o) {
        if (o == null) {
            return 0;
        }
		if (getWidth() == o.getWidth()) {
            return Integer.compare(getHeight(), o.getHeight());
        }

        return Integer.compare(getWidth(), o.getWidth());
    }

	@Override
	public int hashCode() {
		return Objects.hashCode(width, height);
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

		final CameraFormat other = (CameraFormat) obj;

		return Objects.equal(getWidth(), other.getWidth()) && Objects
				.equal(getHeight(), other.getHeight()) && Objects
				.equal(getFrameRate(), other.getFrameRate());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + width + "x" + height + " FPS: " + frameRate;
	}

}
