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

package org.lecturestudio.web.api.message;

import static java.util.Objects.requireNonNullElse;

import java.time.ZonedDateTime;

public abstract class WebMessage {

	private String messageId;

	private String firstName;

	private String familyName;

	private String remoteAddress;

	private ZonedDateTime date;


	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getFirstName() {
		return requireNonNullElse(firstName, "");
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFamilyName() {
		return requireNonNullElse(familyName, "");
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @return the remoteAddress
	 */
	public String getRemoteAddress() {
		return requireNonNullElse(remoteAddress, "");
	}

	/**
	 * @param remoteAddress the remoteAddress to set
	 */
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/**
	 * @return the date
	 */
	public ZonedDateTime getDate() {
		return date;
	}

	/**
	 * @param date the Date to set
	 */
	public void setDate(ZonedDateTime date) {
		this.date = date;
	}
}
