/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.net;

import java.util.Objects;

/**
 * Represents the image quality and frame rate with which local screen content
 * is shared with remote peers.
 *
 * @author Alex Andres
 */
public class ScreenShareProfile {

	private final ScreenShareProfileType type;

	private final int bitrate;

	private final int framerate;


	/**
	 * Creates a new {@code ScreenShareProfile} with the provided parameters.
	 *
	 * @param type      The profile type describing the quality of the
	 *                  screen-share.
	 * @param bitrate   The bitrate (kbps) of the screen stream.
	 * @param framerate The frame rate (fps) of the screen stream.
	 */
	public ScreenShareProfile(ScreenShareProfileType type, int bitrate,
			int framerate) {
		this.type = type;
		this.bitrate = bitrate;
		this.framerate = framerate;
	}

	/**
	 * Creates a new {@code ScreenShareProfile}. NOTE: Used for deserialization
	 * only!
	 */
	protected ScreenShareProfile() {
		this.type = null;
		this.bitrate = 0;
		this.framerate = 0;
	}

	/**
	 * @return The profile type describing the quality of the screen-share.
	 */
	public ScreenShareProfileType getType() {
		return type;
	}

	/**
	 * @return The bitrate (kbps) of the screen stream.
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * @return The frame rate (fps) of the screen stream.
	 */
	public int getFramerate() {
		return framerate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ScreenShareProfile that = (ScreenShareProfile) o;

		return bitrate == that.bitrate && framerate == that.framerate
				&& type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, bitrate, framerate);
	}
}
