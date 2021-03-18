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

package org.lecturestudio.javafx.util;

import static java.util.Objects.isNull;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.beans.NamedArg;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class TextDoubleFormatter extends TextFormatter<Double> {

	private static final StringConverter<Double> CONVERTER = new StringConverter<>() {

		private final NumberFormat numberFormat;

		{
			numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
		}

		@Override
		public String toString(Double value) {
			if (value == null) {
				return "";
			}

			return numberFormat.format(value);
		}

		@Override
		public Double fromString(String value) {
			try {
				if (value == null) {
					return null;
				}

				value = value.trim();

				if (value.length() < 1) {
					return null;
				}

				return numberFormat.parse(value).doubleValue();
			}
			catch (ParseException ex) {
				throw new RuntimeException(ex);
			}
		}
	};


	public TextDoubleFormatter() {
		this(0.0);
	}

	public TextDoubleFormatter(double defaultValue) {
		this(defaultValue, null, null);
	}

	public TextDoubleFormatter(@NamedArg("minValue") Double min,
								@NamedArg("maxValue") Double max) {
		this(0.0, min, max);
	}

	public TextDoubleFormatter(@NamedArg("defaultValue") Double defaultValue,
								@NamedArg("minValue") Double min,
								@NamedArg("maxValue") Double max) {
		super(CONVERTER, defaultValue, new MinMaxFilter(min, max));
	}



	private static class MinMaxFilter implements UnaryOperator<Change> {

		private final Double min;

		private final Double max;

		private final NumberFormat numberFormat;

		private final Pattern pattern;


		MinMaxFilter(Double min, Double max) {
			this.min = min;
			this.max = max;

			Locale locale = Locale.getDefault(Locale.Category.FORMAT);
			DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(locale);
			String separator = Pattern.quote(String.valueOf(formatSymbols.getDecimalSeparator()));

			numberFormat = NumberFormat.getNumberInstance(locale);
			pattern = Pattern.compile("\\d*|\\d+" + separator + "\\d*");
		}

		@Override
		public Change apply(Change change) {
			String newText = change.getControlNewText();

			if (pattern.matcher(newText).matches()) {
				if (isNull(min) && isNull(max)) {
					return change;
				}

				double minValue = isNull(min) ? Double.MIN_VALUE : min;
				double maxValue = isNull(max) ? Double.MAX_VALUE : max;

				try {
					double n = numberFormat.parse(newText).doubleValue();

					return n >= minValue && n <= maxValue ? change : null;
				}
				catch (Exception e) {
					return null;
				}
			}
			return null;
		}
	}
}
