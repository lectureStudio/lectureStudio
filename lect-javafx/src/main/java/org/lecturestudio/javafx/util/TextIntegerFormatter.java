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

import java.util.function.UnaryOperator;

import javafx.beans.NamedArg;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

public class TextIntegerFormatter extends TextFormatter<Integer> {

	private static final IntegerStringConverter CONVERTER = new IntegerStringConverter();


	public TextIntegerFormatter() {
		this(0);
	}

	public TextIntegerFormatter(int defaultValue) {
		this(defaultValue, null, null);
	}

	public TextIntegerFormatter(@NamedArg("minValue") Integer min,
								@NamedArg("maxValue") Integer max) {
		this(0, min, max);
	}

	public TextIntegerFormatter(@NamedArg("defaultValue") Integer defaultValue,
								@NamedArg("minValue") Integer min,
								@NamedArg("maxValue") Integer max) {
		super(CONVERTER, defaultValue, new MinMaxFilter(min, max));
	}



	private static class MinMaxFilter implements UnaryOperator<Change> {

		private final Integer min;

		private final Integer max;


		MinMaxFilter(Integer min, Integer max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public Change apply(Change change) {
			String newText = change.getControlNewText();
			if (newText.matches("-?([1-9][0-9]*|0)?")) {
				if (isNull(min) && isNull(max)) {
					return change;
				}

				int minValue = isNull(min) ? Integer.MIN_VALUE : min;
				int maxValue = isNull(max) ? Integer.MAX_VALUE : max;

				try {
					int n = Integer.parseInt(newText);

					return n >= minValue && n <= maxValue ? change : null;
				}
				catch (NumberFormatException e) {
					return null;
				}
			}
			return null;
		}
	}
}
