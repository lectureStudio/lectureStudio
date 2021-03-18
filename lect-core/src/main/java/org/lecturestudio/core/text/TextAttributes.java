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

public class TextAttributes implements Serializable {
	
	private static final long serialVersionUID = -3665418380734563104L;
	
	private final Map<String, Object> attributes = new HashMap<>(2);
	
	public TextAttributes() {
		attributes.put("underline", Boolean.FALSE);
		attributes.put("strikethrough", Boolean.FALSE);
	}
	
	public boolean isUnderline() {
		Boolean underline = (Boolean) attributes.get("underline");
		return underline.equals(Boolean.TRUE);
	}
	
	public boolean isStrikethrough() {
		Boolean strikethrough = (Boolean) attributes.get("strikethrough");
		return strikethrough.equals(Boolean.TRUE);
	}
	
	public void setAttribute(String name, Object value) {
		if (!attributes.containsKey(name)) {
			return;
		}
		
		attributes.put(name, value);
	}
	
	public TextAttributes clone() {
		TextAttributes clone = new TextAttributes();
		
		for (String key : attributes.keySet()) {
			clone.setAttribute(key, attributes.get(key));
		}
		
		return clone;
	}
	
}
