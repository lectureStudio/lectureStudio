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

import java.math.BigInteger;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A Janus WebRTC video room publisher.
 *
 * @author Alex Andres
 *
 * @apiNote https://janus.conf.meetecho.com/docs/videoroom.html
 */
public class JanusPublisher {

	private BigInteger id;

	private String display;

	private Boolean talking;


	/**
	 * Get the unique ID of the active publisher.
	 *
	 * @return The unique ID of publisher.
	 */
	public BigInteger getId() {
		return id;
	}

	/**
	 * Set the publisher's unique ID.
	 *
	 * @param id The unique ID of publisher.
	 */
	public void setId(BigInteger id) {
		this.id = id;
	}

	/**
	 * Get the user-friendly display name of the active publisher.
	 *
	 * @return The display name of the publisher.
	 */
	public String getDisplayName() {
		return display;
	}

	/**
	 * Set the publisher's user-friendly display name.
	 *
	 * @param name The display name of the publisher.
	 */
	public void setDisplayName(String name) {
		this.display = name;
	}

	/**
	 * Check whether the publisher stream has audio activity or not. This is
	 * only valid if audio levels are used.
	 *
	 * @return True if the publisher is currently talking.
	 */
	public Boolean isTalking() {
		return talking;
	}

	/**
	 * Set whether the publisher stream has audio activity or not.
	 *
	 * @param talking True if the publisher is currently talking.
	 */
	public void setIsTalking(boolean talking) {
		this.talking = talking;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JanusPublisher)) {
			return false;
		}

		JanusPublisher publisher = (JanusPublisher) o;

		return Objects.equals(id, publisher.id)
				&& Objects.equals(display, publisher.display);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, display);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ",
				JanusPublisher.class.getSimpleName() + "[", "]").add("id=" + id)
				.add("display='" + display + "'").toString();
	}
}
