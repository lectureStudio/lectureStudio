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

package org.lecturestudio.core.crypto;

/**
 * The {@link Authority} describes the user's identity and needed credentials to
 * authenticate a user during the challenge.
 *
 * @author Alex Andres
 */
public class Authority {

	/** User's identity 'I', e.g. user name, e-mail address etc. */
	private String identity;

	/** User's password. */
	private String password;


	/**
	 * Create a {@link Authority} with the specified user identity and password.
	 *
	 * @param identity The user's identity.
	 * @param password The user's password.
	 */
	public Authority(String identity, String password) {
		setIdentity(identity);
		setPassword(password);
	}

	/**
	 * Get the identity of the user.
	 *
	 * @return The identity of the user.
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Set the new identity of the user.
	 *
	 * @param identity The user's identity to set.
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * Get the password of the user.
	 *
	 * @return The password of the user.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the new password of the user.
	 *
	 * @param password The user's password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
