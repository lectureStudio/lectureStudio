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

package org.lecturestudio.web.api.janus.message;

/**
 * Room request to start publishing media to a room.
 *
 * @author Alex Andres
 */
public class JanusRoomPublishRequest extends JanusRoomRequest {

	private Integer bitrate;

	private Boolean record;

	private Boolean audio;

	private Boolean video;

	private Boolean data;


	/**
	 * Create a new {@code JanusJoinRoomRequest}.
	 */
	public JanusRoomPublishRequest() {
		setRequestType(JanusRoomRequestType.PUBLISH);
	}

	/**
	 * Set whether or not audio should be relayed. {@code True} by default.
	 *
	 * @param enable True to relay audio to participants.
	 */
	public void setAudio(Boolean enable) {
		audio = enable;
	}

	/**
	 * Set whether or not video should be relayed. {@code True} by default.
	 *
	 * @param enable True to relay video to participants.
	 */
	public void setVideo(Boolean enable) {
		video = enable;
	}

	/**
	 * Set whether or not data should be relayed. {@code True} by default.
	 *
	 * @param enable True to relay data to participants.
	 */
	public void setData(Boolean enable) {
		data = enable;
	}

	/**
	 * Set the bitrate cap to return via Receiver Estimated Maximum Bitrate
	 * (REMB), overrides the global room value if present. This setting is
	 * optional.
	 *
	 * @param bitrate The bitrate cap for this publisher.
	 */
	public void setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
	}

	/**
	 * Set whether this publisher should be recorded or not. This setting is
	 * optional.
	 *
	 * @param record True to record this publisher.
	 */
	public void setRecord(Boolean record) {
		this.record = record;
	}
}
