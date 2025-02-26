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

package org.lecturestudio.web.api.event;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.math.BigInteger;

/**
 * A VideoFrameEvent containing a VideoFrame that was received from a local
 * video track that is sending captured camera video frames to remote peers.
 *
 * @author Alex Andres
 */
public class LocalVideoFrameEvent extends VideoFrameEvent {

	/**
	 * Creates a new LocalVideoFrameEvent with the specified video frame.
	 *
	 * @param frame  The video frame received from a local or remote video track.
	 * @param peerId The unique identifier assigned to the video frame publisher.
	 */
	public LocalVideoFrameEvent(VideoFrame frame, BigInteger peerId) {
		super(frame, peerId);
	}
}
