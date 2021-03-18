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

package org.lecturestudio.core.converter;

import static java.util.Objects.isNull;

import java.util.Locale;

public class IntegerStringConverter extends NumberStringConverter<Integer> {

	public IntegerStringConverter() {
		this(Locale.getDefault());
	}

	public IntegerStringConverter(Locale locale) {
		this(locale, null);
	}

	public IntegerStringConverter(String pattern) {
		this(Locale.getDefault(), pattern);
	}

	public IntegerStringConverter(Locale locale, String pattern) {
		super(locale, pattern);
	}

	@Override
	public Integer from(String value) {
		Number number = super.from(value);

		if (isNull(number)) {
			return null;
		}

		return number.intValue();
	}
}
