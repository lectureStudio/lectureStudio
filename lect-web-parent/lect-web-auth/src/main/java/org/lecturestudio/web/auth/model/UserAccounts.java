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

package org.lecturestudio.web.auth.model;

import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public class UserAccounts {

	private final ConcurrentHashMap<String, UserAccount> accounts = new ConcurrentHashMap<>();


	public void setAccount(UserAccount account) {
		accounts.putIfAbsent(account.getName(), account);
	}

	public UserAccount getAccount(String name) {
		return accounts.get(name);
	}

	public UserAccount removeAccount(String name) {
		return accounts.remove(name);
	}

}
