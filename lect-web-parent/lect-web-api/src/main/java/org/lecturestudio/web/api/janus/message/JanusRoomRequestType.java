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
 * Request types used to classify messages when communicating with the
 * video-room plugin that runs on the Janus WebRTC Server.
 *
 * @author Alex Andres
 *
 * @apiNote https://janus.conf.meetecho.com/docs/videoroom.html
 */
public enum JanusRoomRequestType {

	/**
	 * To create a new video room dynamically.
	 */
	CREATE("create"),

	/**
	 * To remove a video room and destroy it, kicking all the users out as part
	 * of the process.
	 */
	DESTROY("destroy"),

	/**
	 * To dynamically edit some room properties (e.g., the PIN).
	 */
	EDIT("edit"),

	/**
	 * To check whether a specific video room exists.
	 */
	EXISTS("exists"),

	/**
	 * To list all the available rooms.
	 */
	LIST("list"),

	/**
	 * To list all the active participants of a specific room and their
	 * details.
	 */
	LIST_PARTICIPANTS("listparticipants"),

	/**
	 * To configure whether to check tokens or add/remove people who can join a
	 * room.
	 */
	ALLOWED("allowed"),

	/**
	 * To kick participants.
	 */
	KICK("kick"),

	/**
	 * To forcibly mute/unmute any of the media streams sent by participants
	 * (i.e., audio, video and data streams).
	 */
	MODERATE("moderate"),

	/**
	 * To enable or disable recording on all participants while the conference
	 * is in progress.
	 */
	ENABLE_RECORDING("enable_recording"),

	/**
	 * To get a list of all the forwarders in a specific room.
	 */
	LIST_FORWARDERS("listforwarders"),

	// Asynchronous requests.

	/**
	 * To join a video room.
	 */
	JOIN("join"),

	/**
	 * To combine the join and configure requests in a single one (just for
	 * publishers).
	 */
	JOIN_AND_CONFIGURE("joinandconfigure"),

	/**
	 * To modify some of the participation settings (e.g., bitrate cap).
	 */
	CONFIGURE("configure"),

	/**
	 * To leave a video room for good (or, in the case of viewers, definitely
	 * closes a subscription).
	 */
	LEAVE("leave"),

	/**
	 * To start sending media to broadcast to the other participants.
	 */
	PUBLISH("publish"),

	/**
	 * To stop sending media to broadcast to the other participants.
	 */
	UNPUBLISH("unpublish"),

	/**
	 * To start receiving media from a publisher you've subscribed to previously
	 * by means of a join.
	 */
	START("start"),

	/**
	 * To pause the delivery of the media.
	 */
	PAUSE("pause"),

	/**
	 * To change the source of the media flowing over a specific PeerConnection
	 * (e.g., I was watching Alice, I want to watch Bob now) without having to
	 * create a new handle for that.
	 */
	SWITCH("switch");


	private final String type;


	JanusRoomRequestType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static JanusRoomRequestType fromString(String typeStr) {
		for (var value : JanusRoomRequestType.values()) {
			if (value.getType().equals(typeStr)) {
				return value;
			}
		}

		return null;
	}
}
