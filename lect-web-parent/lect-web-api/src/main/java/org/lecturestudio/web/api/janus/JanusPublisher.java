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
	 * Get the user friendly display name of the active publisher.
	 *
	 * @return The display name of the publisher.
	 */
	public String getDisplayName() {
		return display;
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
}
