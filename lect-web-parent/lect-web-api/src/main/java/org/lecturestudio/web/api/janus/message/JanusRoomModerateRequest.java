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

/**
 * Request to moderate (un/mute) a participant.
 *
 * @author Alex Andres
 */
public class JanusRoomModerateRequest extends JanusRoomRequest {

	private BigInteger id;

	private String secret;

	private String mid;

	private Boolean mute;


	/**
	 * Create a new {@code JanusRoomModerateRequest}.
	 */
	public JanusRoomModerateRequest() {
		setRequestType(JanusRoomRequestType.MODERATE);
	}

	/**
	 * Set the unique ID of the participant to moderate.
	 *
	 * @param id The unique participant ID.
	 */
	public void setParticipantId(BigInteger id) {
		this.id = id;
	}

	/**
	 * Get the secret required to moderate the room.
	 *
	 * @return The required secret to moderate the room.
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Set the secret required to moderate the room. This setting is mandatory
	 * if configured.
	 *
	 * @param secret The required secret to moderate the room.
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * Get the mid of the media m-line to refer to for this moderate request.
	 *
	 * @return The mid of the media to moderate.
	 */
	public String getMid() {
		return mid;
	}

	/**
	 * Set the mid of the media m-line to refer to for this moderate request.
	 *
	 * @param mid The mid of the media to moderate.
	 */
	public void setMid(String mid) {
		this.mid = mid;
	}

	/**
	 * Get whether the media addressed by the mid should be muted.
	 *
	 * @return True to mute the media.
	 */
	public Boolean getMute() {
		return mute;
	}

	/**
	 * Set whether the media addressed by the mid should be muted.
	 *
	 * @param mute True to mute the media.
	 */
	public void setMute(boolean mute) {
		this.mute = mute;
	}
}
