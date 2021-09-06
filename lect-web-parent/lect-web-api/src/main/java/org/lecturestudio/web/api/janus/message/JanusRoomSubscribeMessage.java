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
 * Message to send a JSEP answer with subscription related data to the video
 * room.
 *
 * @author Alex Andres
 */
public class JanusRoomSubscribeMessage extends JanusPluginDataMessage {

	static class JSEP {
		String type;
		String sdp;
	}

	private final JSEP jsep = new JSEP();


	/**
	 * Create a new {@code JanusPluginDataMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param handleId  The unique integer plugin handle ID.
	 */
	public JanusRoomSubscribeMessage(BigInteger sessionId,
			BigInteger handleId) {
		super(sessionId, handleId);

		setEventType(JanusMessageType.MESSAGE);

		jsep.type = "answer";
	}

	/**
	 * Set the Session Description Protocol (SDP) offer to start negotiating
	 * session capabilities.
	 *
	 * @param sdp The SDP answer.
	 */
	public void setSdp(String sdp) {
		jsep.sdp = sdp;
	}
}
