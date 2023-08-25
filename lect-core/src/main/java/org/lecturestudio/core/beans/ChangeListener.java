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

package org.lecturestudio.core.beans;

import java.io.IOException;

/**
 * A {@link ChangeListener} is notified whenever the value of an {@link Observable} has
 * changed.
 *
 * @param <T> The type of the observed data.
 *
 * @author Alex Andres
 */
@FunctionalInterface
public interface ChangeListener<T> {

	/**
	 * This method is called when the value of an {@link Observable} has changed.
	 *
	 * @param observable The {@link Observable} of which the value has changed.
	 * @param oldValue   The old value.
	 * @param newValue   The new value.
	 */
	void changed(Observable<? extends T> observable, T oldValue, T newValue);

}
