/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.util.Objects;

import org.lecturestudio.core.model.Quality;

/**
 * Represents the image size and frame rate with which a camera device should
 * capture video frames. The profile is described by the specified {@code
 * Quality} type.
 *
 * @author Alex Andres
 */
public class CameraProfile {

	private final CameraFormat format;

	private final Quality quality;

	private final int bitrate;


	/**
	 * Creates a new {@code CameraProfile} with the provided parameters.
	 *
	 * @param format  The camera capture format.
	 * @param quality The quality type describing this profile.
	 * @param bitrate The bitrate (kbps) of the camera stream.
	 */
	public CameraProfile(CameraFormat format, Quality quality, int bitrate) {
		this.format = format;
		this.quality = quality;
		this.bitrate = bitrate;
	}

	/**
	 * @return The camera capture format.
	 */
	public CameraFormat getFormat() {
		return format;
	}

	/**
	 * @return The quality type describing this profile.
	 */
	public Quality getQuality() {
		return quality;
	}

	/**
	 * @return The bitrate (kbps) of the camera stream.
	 */
	public int getBitrate() {
		return bitrate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CameraProfile profile = (CameraProfile) o;

		return Objects.equals(format, profile.format)
				&& quality == profile.quality;
	}

	@Override
	public int hashCode() {
		return Objects.hash(format, quality);
	}
}
