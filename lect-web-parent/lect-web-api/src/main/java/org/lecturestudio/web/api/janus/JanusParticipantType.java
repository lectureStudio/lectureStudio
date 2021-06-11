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

package org.lecturestudio.web.api.janus;

import java.util.NoSuchElementException;

/**
 * Janus video room participant types.
 *
 * @author Alex Andres
 *
 * @apiNote https://janus.conf.meetecho.com/docs/videoroom.html
 */
public enum JanusParticipantType {

	/**
	 * Publishers stream their media.
	 */
	PUBLISHER("publisher"),

	/**
	 * Subscribers receive media from publishers.
	 */
	SUBSCRIBER("subscriber");


	private final String type;


	JanusParticipantType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static JanusParticipantType fromString(String typeStr) {
		for (var value : JanusParticipantType.values()) {
			if (value.getType().equals(typeStr)) {
				return value;
			}
		}

		throw new NoSuchElementException();
	}
}
