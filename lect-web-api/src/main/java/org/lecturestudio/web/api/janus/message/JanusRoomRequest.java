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

import java.math.BigInteger;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTypeAdapter;

import org.lecturestudio.web.api.janus.json.RoomRequestTypeAdapter;

/**
 * Basic request message implementation to perform room related requests with
 * the video-room plugin running on the Janus WebRTC Server.
 *
 * @author Alex Andres
 */
public class JanusRoomRequest {

	@JsonbProperty("request")
	@JsonbTypeAdapter(RoomRequestTypeAdapter.class)
	private JanusRoomRequestType requestType;

	@JsonbProperty("room")
	private BigInteger room;


	/**
	 * Get the event type this request describes.
	 *
	 * @return The request event type.
	 */
	public JanusRoomRequestType getRequestType() {
		return requestType;
	}

	/**
	 * Set the event type this request describes.
	 *
	 * @param requestType The request event type.
	 */
	public void setRequestType(JanusRoomRequestType requestType) {
		this.requestType = requestType;
	}

	/**
	 * Get the unique numeric room ID, optional, since not all requests require
	 * this field.
	 *
	 * @return The unique numeric room ID.
	 */
	public BigInteger getRoomId() {
		return room;
	}

	/**
	 * Set the unique numeric room ID. Optional, since not all requests require
	 * this field.
	 *
	 * @param room the unique room ID.
	 */
	public void setRoomId(BigInteger room) {
		this.room = room;
	}
}
