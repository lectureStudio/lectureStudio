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
 * Message used to trickle ICE candidates.
 *
 * @author Alex Andres
 */
public class JanusTrickleMessage extends JanusPluginMessage {

	static class Candidate {
		String sdpMid;
		int sdpMLineIndex;
		String candidate;
	}

	private final Candidate candidate = new Candidate();


	/**
	 * Create a new {@code JanusTrickleMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param handleId  The unique integer plugin handle ID.
	 */
	public JanusTrickleMessage(BigInteger sessionId, BigInteger handleId) {
		super(sessionId, handleId);

		setEventType(JanusMessageType.TRICKLE);
	}

	/**
	 * Set the media stream identification tag for the media component the
	 * candidate is associated with.
	 *
	 * @param mid The media stream identification tag.
	 */
	public void setSdpMid(String mid) {
		candidate.sdpMid = mid;
	}

	/**
	 * Set the index (starting at zero) of the media description in the SDP the
	 * candidate is associated with.
	 *
	 * @param index The index of the media description in the SDP.
	 */
	public void setSdpMLineIndex(int index) {
		candidate.sdpMLineIndex = index;
	}

	/**
	 * Set the SDP string representation of the candidate.
	 *
	 * @param sdp The SDP of the candidate.
	 */
	public void setSdp(String sdp) {
		candidate.candidate = sdp;
	}
}
