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

package org.lecturestudio.core.text;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TextAttributes implements Serializable {

	private static final long serialVersionUID = -3665418380734563104L;

	private final Map<String, Boolean> attributes = new HashMap<>(2);


	public TextAttributes() {
		attributes.put("underline", Boolean.FALSE);
		attributes.put("strikethrough", Boolean.FALSE);
	}

	public boolean isUnderline() {
		Boolean underline = (Boolean) attributes.getOrDefault("underline", Boolean.FALSE);
		return Boolean.TRUE.equals(underline);
	}

	public void setUnderline(boolean underline) {
		attributes.put("underline", underline);
	}

	public boolean isStrikethrough() {
		Boolean strikethrough = attributes.getOrDefault("strikethrough", Boolean.FALSE);
		return Boolean.TRUE.equals(strikethrough);
	}

	public void setStrikethrough(boolean strikethrough) {
		attributes.put("strikethrough", strikethrough);
	}

	public void setAttribute(String name, Boolean value) {
		if (!attributes.containsKey(name)) {
			return;
		}

		attributes.put(name, value);
	}

	public TextAttributes clone() {
		TextAttributes clone = new TextAttributes();

		for (Entry<String, Boolean> entry : attributes.entrySet()) {
			clone.setAttribute(entry.getKey(), entry.getValue());
		}

		return clone;
	}

}
