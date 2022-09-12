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

import java.util.ArrayList;
import java.util.List;

/**
 * Room request to start publishing media to a room.
 *
 * @author Alex Andres
 */
public class JanusRoomPublishRequest extends JanusRoomRequest {

	private Integer bitrate;

	private Boolean record;

	private List<StreamDescription> descriptions;


	/**
	 * Create a new {@code JanusJoinRoomRequest}.
	 */
	public JanusRoomPublishRequest() {
		setRequestType(JanusRoomRequestType.CONFIGURE);

		descriptions = new ArrayList<>();
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

	/**
	 * Adds a stream description in order to provide more information about the
	 * streams being published (e.g., to let other participants know that the
	 * first video is a camera, while the second video is a screen share).
	 *
	 * @param mid         The unique mid of a stream being published.
	 * @param description The description of the stream (e.g., camera).
	 */
	public void addStreamDescription(String mid, String description) {
		descriptions.add(new StreamDescription(mid, description));
	}

	/**
	 * Get all provided stream descriptions.
	 *
	 * @return A list of stream descriptions.
	 */
	public List<StreamDescription> getDescriptions() {
		return descriptions;
	}



	public static class StreamDescription {

		/** Unique mid of a stream being published. */
		public String mid;

		/** Text description of the stream (e.g., camera). */
		public String description;


		StreamDescription(String mid, String description) {
			this.mid = mid;
			this.description = description;
		}

		public String getMid() {
			return mid;
		}

		public String getDescription() {
			return description;
		}
	}
}
