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

package org.lecturestudio.core.app.dictionary;

/**
 * The Dictionary provides an interface to a string translation dictionary. The
 * dictionary format and storage method depends on the implementation.
 *
 * @author Alex Andres
 */
public interface Dictionary {

	/**
	 * Returns the value to which the key is mapped in this dictionary. If the
	 * dictionary contains an entry for the specified key, the associated value
	 * is returned, otherwise {@code null} is returned.
	 *
	 * @param key A key in the dictionary.
	 *
	 * @return the value associated with the specified key.
	 *
	 * @throws NullPointerException If the key is {@code null}.
	 */
	String get(String key) throws NullPointerException;

	/**
	 * Checks the dictionary for an existing key.
	 *
	 * @param key A key in the dictionary.
	 *
	 * @return {@code true} if the dictionary contains an entry for the specified key, otherwise {@code false}.
	 */
	boolean contains(String key);

}
