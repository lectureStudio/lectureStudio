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

package org.lecturestudio.swing.swixml.processor;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.LayoutManager;
import java.lang.reflect.Field;

import org.swixml.LogAware;
import org.swixml.Parser;
import org.swixml.processor.ButtonGroupTagProcessor;
import org.swixml.processor.ConstraintsTagProcessor;
import org.swixml.processor.ScriptTagProcessor;
import org.swixml.processor.TagProcessor;
import org.w3c.dom.Element;

public class PanelProcessor implements TagProcessor, LogAware {

	@Override
	public boolean process(Parser parser, Object parent, Element child,
			LayoutManager layoutMgr) throws Exception {
		boolean result = false;

		if (!child.hasAttribute("ref")) {
			result = ButtonGroupTagProcessor.instance.process(parser, parent, child, layoutMgr);

			if (!result) {
				result = ScriptTagProcessor.instance.process(parser, parent, child, layoutMgr);
			}
			if (!result) {
				result = ConstraintsTagProcessor.instance.process(parser, parent, child, layoutMgr);
			}
		}
		else {
			String ref = child.getAttribute("ref");
			Field refField = getField(parent.getClass(), ref);

			if (nonNull(refField)) {
				if (!refField.canAccess(parent)) {
					refField.setAccessible(true);
				}

				parser.getSwing(child, refField.get(parent));

				result = true;
			}
		}

		return result;
	}

	private Field getField(Class<?> cls, String fieldName) {
		if (isNull(cls) || isNull(fieldName) || fieldName.isEmpty()) {
			return null;
		}

		try {
			return cls.getDeclaredField(fieldName);
		}
		catch (NoSuchFieldException e) {
			return getField(cls.getSuperclass(), fieldName);
		}
	}
}
