/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.model;

import java.io.Serializable;
import java.util.Objects;

public class Message extends ServiceModel implements Comparable<Message>, Cloneable, Serializable {

	private String text;


	public Message() {
		this(null);
	}

	public Message(String text) {
		setText(text);
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public int compareTo(Message other) {
		return text.compareToIgnoreCase(other.text);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Message other = (Message) o;

		return getServiceId() == other.getServiceId() && Objects.equals(text, other.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getServiceId(), text);
	}

	@Override
	public Message clone() {
		return new Message(text);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + text;
	}
	
}
