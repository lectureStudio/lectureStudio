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

import java.util.List;

/**
 * Request container used to edit a video room.
 *
 * @author Alex Andres
 */
public class JanusEditRoomMessage extends JanusRoomRequest {

	private Integer new_publishers;

	private Integer new_bitrate;

	private Boolean new_is_private;

	private String new_description;

	private String new_secret;

	private String new_pin;

	private List<String> allowed;


	/**
	 * Create a new {@code JanusEditRoomRequest}.
	 */
	public JanusEditRoomMessage() {
		setRequestType(JanusRoomRequestType.EDIT);
	}

	/**
	 * Get the maximum number of concurrent senders.
	 *
	 * @return The maximum number senders.
	 */
	public int getPublishers() {
		return new_publishers;
	}

	/**
	 * Set the maximum number of concurrent senders, e.g. 6 for a video
	 * conference or 1 for a webinar.
	 *
	 * @param new_publishers The maximum number senders.
	 */
	public void setPublishers(int new_publishers) {
		this.new_publishers = new_publishers;
	}

	/**
	 * Get the maximum video bitrate for senders.
	 *
	 * @return The maximum video bitrate for senders.
	 */
	public int getBitrate() {
		return new_bitrate;
	}

	/**
	 * Get the maximum video bitrate for senders, e.g. 128000.
	 *
	 * @param new_bitrate The maximum video bitrate for senders.
	 */
	public void setBitrate(int new_bitrate) {
		this.new_bitrate = new_bitrate;
	}

	/**
	 * Get the pretty description of the room.
	 *
	 * @return The pretty description.
	 */
	public String getDescription() {
		return new_description;
	}

	/**
	 * Set the pretty description of the room. This setting is optional.
	 *
	 * @param new_description The pretty description.
	 */
	public void setDescription(String new_description) {
		this.new_description = new_description;
	}

	/**
	 * Get the password required to edit/destroy the room.
	 *
	 * @return The required password to edit/destroy the room.
	 */
	public String getSecret() {
		return new_secret;
	}

	/**
	 * Set the password required to edit/destroy the room. This setting is
	 * optional.
	 *
	 * @param new_secret The required password to edit/destroy the room.
	 */
	public void setSecret(String new_secret) {
		this.new_secret = new_secret;
	}

	/**
	 * Get the password required to join the room.
	 *
	 * @return The password of the room.
	 */
	public String getPin() {
		return new_pin;
	}

	/**
	 * Set the password required to join the room. This setting is optional.
	 *
	 * @param new_pin The password of the room.
	 */
	public void setPin(String new_pin) {
		this.new_pin = new_pin;
	}

	/**
	 * Whether the room should appear in a room list request.
	 *
	 * @return True to hide the room in the room listing.
	 */
	public boolean isPrivate() {
		return new_is_private;
	}

	/**
	 * Set whether the room should appear in a room list request.
	 *
	 * @param isPrivate True to hide the room in the room listing.
	 */
	public void setIsPrivate(boolean isPrivate) {
		this.new_is_private = isPrivate;
	}

	/**
	 * Get a list of string tokens users can use to join the room.
	 *
	 * @return A list of string tokens that allow to join the room.
	 */
	public List<String> getAllowed() {
		return allowed;
	}

	/**
	 * Set a list of string tokens users can use to join the room. This setting
	 * is optional.
	 *
	 * @param allowed A list of string tokens that allow to join the room.
	 */
	public void setAllowed(List<String> allowed) {
		this.allowed = allowed;
	}
}
