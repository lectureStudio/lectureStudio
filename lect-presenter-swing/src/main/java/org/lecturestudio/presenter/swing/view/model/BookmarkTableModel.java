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

package org.lecturestudio.presenter.swing.view.model;

import javax.swing.table.TableColumnModel;

import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.swing.table.TableModelBase;

public class BookmarkTableModel extends TableModelBase<Bookmark> {

	public BookmarkTableModel(TableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Bookmark bookmark = getItem(rowIndex);

		switch (columnIndex) {
			case 0:
				return bookmark.getPage().getDocument().getName();

			case 1:
				return bookmark.getPage().getPageNumber() + 1;

			case 2:
				return bookmark.getShortcut();
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 3;
	}
}
