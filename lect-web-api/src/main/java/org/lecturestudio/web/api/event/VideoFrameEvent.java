/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.event;

import java.math.BigInteger;

import dev.onvoid.webrtc.media.video.VideoFrame;

/**
 * Basic event class to transport received video frames by the WebRTC stack.
 *
 * @author Alex Andres
 */
public abstract class VideoFrameEvent {

	private final VideoFrame frame;

	private final BigInteger peerId;


	/**
	 * Creates a new VideoFrameEvent with the specified video frame.
	 *
	 * @param frame  The video frame received from a local or remote video track.
	 * @param peerId The unique identifier assigned to the video frame publisher.
	 */
	public VideoFrameEvent(VideoFrame frame, BigInteger peerId) {
		this.frame = frame;
		this.peerId = peerId;
	}

	/**
	 * @return The video frame received from a local or remote video track.
	 */
	public VideoFrame getFrame() {
		return frame;
	}

	/**
	 * Get the unique ID of the publishing peer.
	 *
	 * @return The unique ID of the video frame origin.
	 */
	public BigInteger getPeerId() {
		return peerId;
	}
}
