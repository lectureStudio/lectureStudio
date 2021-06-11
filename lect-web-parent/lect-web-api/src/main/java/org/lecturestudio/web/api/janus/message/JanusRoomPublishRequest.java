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
public class JanusRoomPublishRequest extends JanusRoomRequest {

	private Integer bitrate;

	private Boolean record;


	/**
	 * Create a new {@code JanusJoinRoomRequest}.
	 */
	public JanusRoomPublishRequest() {
		setRequestType(JanusRoomRequestType.PUBLISH);
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
