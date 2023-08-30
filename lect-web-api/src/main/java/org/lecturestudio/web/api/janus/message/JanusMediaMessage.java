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
 * Message describing media events in case media (audio/video) have been
 * negotiated.
 *
 * @author Alex Andres
 */
public class JanusMediaMessage extends JanusPluginMessage {

	private String type;

	private Boolean receiving;


	/**
	 * Create a new {@code JanusMediaMessage} with the specified parameters.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param handleId  The unique integer plugin handle ID.
	 */
	public JanusMediaMessage(BigInteger sessionId, BigInteger handleId) {
		super(sessionId, handleId);

		setEventType(JanusMessageType.MEDIA);
	}

	/**
	 * Get the media type Janus is receiving or not.
	 *
	 * @return The media type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the media type Janus is receiving or not.
	 *
	 * @param type The media type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Check whether Janus is receiving the media described by the type in this
	 * message.
	 *
	 * @return True if Janus is receiving.
	 */
	public Boolean isReceiving() {
		return receiving;
	}

	/**
	 * Set whether Janus is receiving the media.
	 *
	 * @param receiving True if Janus is receiving.
	 */
	public void setReceiving(Boolean receiving) {
		this.receiving = receiving;
	}
}
