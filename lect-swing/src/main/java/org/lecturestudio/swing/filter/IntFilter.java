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

package org.lecturestudio.swing.filter;

import static java.util.Objects.isNull;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class IntFilter extends DocumentFilter {

	private final int maxLength;


	public IntFilter() {
		this(128);
	}

	public IntFilter(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String string,
			AttributeSet attr) throws BadLocationException {
		string = getFilteredString(string, offset);
		super.insertString(fb, offset, string, attr);
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String string,
			AttributeSet attr) throws BadLocationException {
		string = getFilteredString(string, offset);
		super.replace(fb, offset, length, string, attr);
	}

	private String getFilteredString(String value, int offset) {
		if (isNull(value)) {
			return null;
		}
		if (offset >= maxLength) {
			return "";
		}
		if (value.length() > maxLength) {
			value = value.substring(0, maxLength);
		}

		StringBuilder builder = new StringBuilder(value);

		for (int i = builder.length() - 1; i >= 0; i--) {
			int cp = builder.codePointAt(i);
			if (!Character.isDigit(cp) && cp != '-') {
				builder.deleteCharAt(i);

				if (Character.isSupplementaryCodePoint(cp)) {
					i--;
					builder.deleteCharAt(i);
				}
			}
		}

		return builder.toString();
	}
}
