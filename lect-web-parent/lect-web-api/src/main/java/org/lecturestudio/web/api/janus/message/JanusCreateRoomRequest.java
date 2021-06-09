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
import java.util.List;

/**
 * Request container used to create a new video room.
 *
 * @author Alex Andres
 */
public class JanusCreateRoomRequest extends JanusRoomRequest {

	private BigInteger room;

	private boolean permanent;

	private boolean is_private;

	private String description;

	private String secret;

	private String pin;

	private List<String> allowed;


	/**
	 * Create a new {@code JanusCreateRoomRequest}.
	 */
	public JanusCreateRoomRequest() {
		setRequestType(JanusRoomRequestType.CREATE);
	}

	/**
	 * Get the unique numeric room ID, optional, chosen by plugin if missing.
	 *
	 * @return The unique numeric room ID.
	 */
	public BigInteger getRoom() {
		return room;
	}

	/**
	 * Set the unique numeric room ID. Optional, chosen by plugin if missing.
	 *
	 * @param room the unique room ID.
	 */
	public void setRoom(BigInteger room) {
		this.room = room;
	}

	/**
	 * Whether the room should be saved in the config file.
	 *
	 * @return True to save the room.
	 */
	public boolean isPermanent() {
		return permanent;
	}

	/**
	 * Set whether the room should be saved in the config file. {@code False} by
	 * default.
	 *
	 * @param permanent True to save the room.
	 */
	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	/**
	 * Get the pretty description of the room.
	 *
	 * @return The pretty description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the pretty description of the room. This setting is optional.
	 *
	 * @param description The pretty description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the password required to edit/destroy the room.
	 *
	 * @return The required password to edit/destroy the room.
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Set the password required to edit/destroy the room. This setting is
	 * optional.
	 *
	 * @param secret The required password to edit/destroy the room.
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * Get the password required to join the room.
	 *
	 * @return The password of the room.
	 */
	public String getPin() {
		return pin;
	}

	/**
	 * Set the password required to join the room. This setting is optional.
	 *
	 * @param pin The password of the room.
	 */
	public void setPin(String pin) {
		this.pin = pin;
	}

	/**
	 * Whether the room should appear in a room list request.
	 *
	 * @return True to hide the room in the room listing.
	 */
	public boolean isPrivate() {
		return is_private;
	}

	/**
	 * Set whether the room should appear in a room list request.
	 *
	 * @param isPrivate True to hide the room in the room listing.
	 */
	public void setIsPrivate(boolean isPrivate) {
		this.is_private = isPrivate;
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
