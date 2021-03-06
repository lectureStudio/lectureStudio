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

import static java.util.Objects.nonNull;

import javax.swing.table.TableColumnModel;

import org.lecturestudio.swing.table.TableModelBase;
import org.lecturestudio.web.api.filter.RegexRule;

public class QuizRegexTableModel extends TableModelBase<RegexRule> {

	public QuizRegexTableModel(TableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RegexRule rule = getItem(rowIndex);

		if (columnIndex == 0) {
			return rule.getRegex();
		}

		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		RegexRule rule = getItem(row);

		if (nonNull(rule)) {
			rule.setRegex((String) value);
			fireTableCellUpdated(row, column);
		}
	}
}
