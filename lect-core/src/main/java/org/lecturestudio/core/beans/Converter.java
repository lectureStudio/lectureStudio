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

/**
 * Interface for converting objects between type {@link S} and type {@link T}.
 *
 * @param <S> first type
 * @param <T> second type
 */
public interface Converter<S, T> {

	/**
	 * Converts {@code value} from type {@link S} to type {@link T}.
	 *
	 * @param value The value with type {@link S} to be converted.
	 * @return The value with type {@link T}.
	 */
	T to(S value);

	/**
	 * Converts {@code value} from type {@link T} to type {@link S}.
	 *
	 * @param value The value with type {@link T} to be converted.
	 * @return The value with type {@link S}.
	 */
	S from(T value);

}
