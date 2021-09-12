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

import java.util.NoSuchElementException;

/**
 * Event types used to classify messages when communicating with the video-room
 * plugin that runs on the Janus WebRTC Server.
 *
 * @author Alex Andres
 *
 * @apiNote https://janus.conf.meetecho.com/docs/videoroom.html
 */
public enum JanusRoomEventType {

	/**
	 * Generic video room event.
	 */
	EVENT("event"),

	/**
	 * A successful request.
	 */
	SUCCESS("success"),

	// Publishers

	/**
	 * The new video room has been created.
	 */
	CREATED("created"),

	/**
	 * The video room has been destroyed, kicking all the users out as part
	 * of the process.
	 */
	DESTROYED("destroyed"),

	/**
	 * The video room has been edited.
	 */
	EDITED("edited"),

	/**
	 * A list of participants in the response.
	 */
	PARTICIPANTS("participants"),

	/**
	 * A publisher is joining a video room.
	 */
	JOINING("joining"),

	/**
	 * A publisher has joined a video room.
	 */
	JOINED("joined"),

	/**
	 * A publisher started talking.
	 */
	TALKING("talking"),

	/**
	 * A publisher stopped talking.
	 */
	STOPPED_TALKING("stopped-talking"),

	/**
	 * A new RTP forwarder has been added for an existing publisher.
	 */
	RTP_FORWARD("rtp_forward"),

	/**
	 * An RTP forwarder has been stopped for an existing publisher.
	 */
	STOP_RTP_FORWARD("stop_rtp_forward"),

	/**
	 * A list of RTP forwarders in the response.
	 */
	FORWARDERS("forwarders"),

	// Subscribers

	/**
	 * A participant has joined a video room.
	 */
	ATTACHED("attached"),

	/**
	 * The subscription has been updated.
	 */
	UPDATED("updated");


	private final String type;


	JanusRoomEventType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static JanusRoomEventType fromString(String typeStr) {
		for (var value : JanusRoomEventType.values()) {
			if (value.getType().equals(typeStr)) {
				return value;
			}
		}

		throw new NoSuchElementException();
	}
}
