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

import java.awt.LayoutManager;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.swixml.LogAware;
import org.swixml.Parser;
import org.swixml.processor.TagProcessor;
import org.w3c.dom.Element;

import static java.util.Objects.nonNull;

public class TableProcessor implements TagProcessor, LogAware {

	@Override
	public boolean process(Parser parser, Object parent, Element child,
			LayoutManager layoutMgr) throws Exception {
		if (!Parser.TAG_TABLECOLUMN.equalsIgnoreCase(child.getLocalName())) {
			return false;
		}
		if (!(parent instanceof JTable table)) {
			logger.warning("TableColumn tag is valid only inside Table tag. Ignored!");
			return false;
		}

		final TableColumn column = (TableColumn) parser.getSwing(child, null);
		column.setModelIndex(table.getColumnModel().getColumnCount());
		column.setHeaderValue(parser.engine.getLocalizer().getString(
				(String) column.getHeaderValue()));

		table.getColumnModel().addColumn(column);

		if (child.hasAttribute("prefWidth")) {
			String prefWidthStr = child.getAttribute("prefWidth");
			Integer prefWidth = getIntValue(prefWidthStr);

			if (nonNull(prefWidth)) {
				column.setPreferredWidth(prefWidth);
			}
		}
		if (child.hasAttribute("minWidth")) {
			String minWidthStr = child.getAttribute("minWidth");
			Integer minWidth = getIntValue(minWidthStr);

			if (nonNull(minWidth)) {
				column.setMinWidth(minWidth);
			}
		}
		if (child.hasAttribute("maxWidth")) {
			String maxWidthStr = child.getAttribute("maxWidth");
			Integer maxWidth = getIntValue(maxWidthStr);

			if (nonNull(maxWidth)) {
				column.setMaxWidth(maxWidth);
			}
		}

		return true;
	}

	private static Integer getIntValue(String value) {
		if (!value.isBlank()) {
			try {
				return Integer.valueOf(value);
			}
			catch (NumberFormatException e) {
				logger.warning("Invalid integer attribute value: " + value);
			}
		}
		return null;
	}
}
