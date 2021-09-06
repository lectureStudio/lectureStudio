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

import org.lecturestudio.web.api.janus.JanusParticipantType;
import org.lecturestudio.web.api.janus.json.ParticipantTypeAdapter;

/**
 * Join room request to associate the participant as an publisher.
 *
 * @author Alex Andres
 */
public class JanusRoomJoinRequest extends JanusRoomRequest {

	@JsonbProperty("ptype")
	@JsonbTypeAdapter(ParticipantTypeAdapter.class)
	private JanusParticipantType participantType;

	@JsonbProperty("room")
	private BigInteger roomId;

	@JsonbProperty("feed")
	private BigInteger publisherId;

	@JsonbProperty("display")
	private String displayName;

	@JsonbProperty("token")
	private String invitationToken;


	/**
	 * Create a new {@code JanusJoinRoomRequest}.
	 */
	public JanusRoomJoinRequest() {
		setRequestType(JanusRoomRequestType.JOIN);
	}

	/**
	 * Set the participant type this request describes. This setting is
	 * mandatory.
	 *
	 * @param participantType The request participant type.
	 */
	public void setParticipantType(JanusParticipantType participantType) {
		this.participantType = participantType;
	}

	/**
	 * Set the unique ID of the room to join.
	 *
	 * @param id The unique room ID.
	 */
	public void setRoomId(BigInteger id) {
		roomId = id;
	}

	/**
	 * Set the unique ID to register for the publisher. Optional, will be chosen
	 * by the plugin if missing.
	 *
	 * @param id The unique publisher ID.
	 */
	public void setPublisherId(BigInteger id) {
		publisherId = id;
	}

	/**
	 * Set the display name for the publisher. This setting is optional.
	 *
	 * @param name The display name for the publisher.
	 */
	public void setDisplayName(String name) {
		displayName = name;
	}

	/**
	 * Set the invitation token, in case the room has an ACL.
	 *
	 * @param token The invitation token.
	 */
	public void setInvitationToken(String token) {
		invitationToken = token;
	}
}
