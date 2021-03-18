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

package org.lecturestudio.presenter.api.model;

import java.util.Objects;

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.StringProperty;

public class Contact {

	private final StringProperty id;

	private final StringProperty name;

	private final IntegerProperty unreadMessages;


	public Contact() {
		this(null, null);
	}

	public Contact(String id, String name) {
		this.id = new StringProperty(id);
		this.name = new StringProperty(name);
		this.unreadMessages = new IntegerProperty(0);
	}

	public String getId() {
		return id.get();
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public StringProperty nameProperty() {
		return name;
	}

	public int getUnreadMessages() {
		return unreadMessages.get();
	}

	public void setUnreadMessages(int count) {
		this.unreadMessages.set(count);
	}

	public IntegerProperty unreadMessagesProperty() {
		return unreadMessages;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Contact contact = (Contact) o;

		return Objects.equals(getId(), contact.getId()) &&
				Objects.equals(getName(), contact.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getName());
	}

	@Override
	public String toString() {
		return String.format("%s@%d [id=%s, name=%s, unreadMessages=%s]",
				Contact.class.getSimpleName(), hashCode(),
				getId(), getName(), getUnreadMessages());
	}
}
