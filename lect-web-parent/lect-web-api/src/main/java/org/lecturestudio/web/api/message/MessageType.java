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

package org.lecturestudio.web.api.message;

import java.util.NoSuchElementException;

/**
 * Event types used to classify messages when communicating with the web api
 * server.
 *
 * @author Alex Andres
 */
public enum MessageType {

	/**
	 * Text message.
	 */
	TEXT("text"),

	/**
	 * Quiz answer with selected options.
	 */
	QUIZ_ANSWER("quiz_answer");


	private final String type;


	MessageType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static MessageType fromString(String typeStr) {
		for (var value : MessageType.values()) {
			if (value.getType().equals(typeStr)) {
				return value;
			}
		}

		throw new NoSuchElementException();
	}

}
