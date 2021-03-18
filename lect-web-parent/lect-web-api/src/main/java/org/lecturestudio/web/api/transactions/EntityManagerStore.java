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

package org.lecturestudio.web.api.transactions;

import javax.persistence.EntityManager;

public interface EntityManagerStore {

	/**
	 * Looks for the current entity manager and returns it. If no entity manager
	 * was found, this method logs a warn message and returns null. This will cause a NullPointerException in most
	 * cases and will cause a stack trace starting from your service method.
	 *
	 * @return the currently used entity manager or {@code null} if none was found
	 */
	EntityManager get();

	/**
	 * Creates an entity manager and stores it in a stack. The use of a stack allows to implement
	 * transaction with a 'requires new' behaviour.
	 *
	 * @return the created entity manager
	 */
	EntityManager createAndRegister();

	/**
	 * Removes an entity manager from the thread local stack. It needs to be created using the
	 * {@link #createAndRegister()} method.
	 *
	 * @param entityManager - the entity manager to remove
	 * @throws IllegalStateException in case the entity manager was not found on the stack
	 */
	void unregister(EntityManager entityManager);
}
