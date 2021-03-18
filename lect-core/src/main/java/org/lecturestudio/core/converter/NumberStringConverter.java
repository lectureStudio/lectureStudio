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
import static java.util.Objects.nonNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.lecturestudio.core.beans.Converter;

public class NumberStringConverter<T extends Number> implements Converter<T, String> {

	private Locale locale;

	private String pattern;


	public NumberStringConverter() {
		this(Locale.getDefault());
	}

	public NumberStringConverter(Locale locale) {
		this(locale, null);
	}

	public NumberStringConverter(String pattern) {
		this(Locale.getDefault(), pattern);
	}

	public NumberStringConverter(Locale locale, String pattern) {
		this.locale = locale;
		this.pattern = pattern;
	}

	@Override
	public String to(Number value) {
		if (isNull(value)) {
			return "";
		}

		NumberFormat formatter = getNumberFormat();

		return formatter.format(value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T from(String value) {
		try {
			if (isNull(value)) {
				return null;
			}

			value = value.trim();

			if (value.length() < 1) {
				return null;
			}

			NumberFormat parser = getNumberFormat();

			return (T) parser.parse(value);
		}
		catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Returns a {@code NumberFormat} instance for formatting and parsing a
	 * string.
	 *
	 * @return a {@code NumberFormat} instance.
	 */
	protected NumberFormat getNumberFormat() {
		Locale lc = isNull(locale) ? Locale.getDefault() : locale;

		if (nonNull(pattern)) {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(lc);
			return new DecimalFormat(pattern, symbols);
		}
		else {
			return NumberFormat.getNumberInstance(lc);
		}
	}
}
