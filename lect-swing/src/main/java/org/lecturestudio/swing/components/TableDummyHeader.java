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

package org.lecturestudio.swing.components;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class TableDummyHeader extends JTableHeader {

	private static final long serialVersionUID = -1615904587901148734L;

//	private JTable table;

//	private CellRendererPane rendererPane = new CellRendererPane();


	public TableDummyHeader(JTable table) {
		this.table = table;
		
		setName("DummyHeader");
	}

//	@Override
//	protected void paintComponent(Graphics g) {
//		TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
//
//		JComponent component = (JComponent) renderer.getTableCellRendererComponent(table,
//																				   "",
//																				   false,
//																				   false,
//																				   -1,
//																				   table.getColumnCount());
//		component.setBounds(0, 0, getWidth(), table.getTableHeader().getHeight());
//		component.setOpaque(false);
//
//		rendererPane.paintComponent(g, component, null, 0, 0, getWidth(), table.getTableHeader().getHeight(), true);
//	}

}
