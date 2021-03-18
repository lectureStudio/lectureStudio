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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class FloatFilter extends DocumentFilter {

	private final int maxLength;


	public FloatFilter() {
		this(128);
	}
	
	public FloatFilter(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		string = getFilteredString(fb, string, offset, string.length());
		super.insertString(fb, offset, string, attr);
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {
		string = getFilteredString(fb, string, offset, length);
		super.replace(fb, offset, length, string, attr);
	}
	
	private String getFilteredString(FilterBypass fb, String string, int offset, int length) {
		if (string == null)
			return string;
		
		if (offset >= maxLength)
			return "";
		
		if (string.length() > maxLength) {
			string = string.substring(0, maxLength);
		}
		
		
		string = string.replaceAll("[^0-9.]", "").trim();
		String tmp = string;
		
		if (length == 0) {
			Document doc = fb.getDocument();
			try {
				tmp = doc.getText(0, doc.getLength()).trim() + string;
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Float.parseFloat(tmp);
		}
		catch (Exception e) {
			return "";
		}
		
        return string;
	}
	
}
