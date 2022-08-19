/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;

import java.util.Set;

import org.lecturestudio.web.api.model.UserPrivilege;

public class UserPrivilegeService {

	private Set<UserPrivilege> privileges;


	public Set<UserPrivilege> getPrivileges() {
		return privileges;
	}

	public void setPrivileges(Set<UserPrivilege> privileges) {
		this.privileges = privileges;
	}

	public boolean hasPrivilege(String name) {
		if (isNull(privileges)) {
			return false;
		}

		return privileges.contains(new UserPrivilege(name));
	}

	public boolean hasAnyPrivilege(String... names) {
		if (isNull(privileges) || isNull(names)) {
			return false;
		}

		for (String name : names) {
			if (hasPrivilege(name)) {
				return true;
			}
		}

		return false;
	}

	public boolean canUseChat() {
		return canReadMessages() || canWriteMessages();
	}

	public boolean canWriteMessages() {
		return hasAnyPrivilege("CHAT_WRITE", "CHAT_WRITE_PRIVATELY",
				"CHAT_WRITE_TO_ORGANISATOR");
	}

	public boolean canReadMessages() {
		return hasPrivilege("CHAT_READ");
	}
}
