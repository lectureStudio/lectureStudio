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
import org.lecturestudio.web.api.filter.IpRangeRule;

public class IpRangeRuleTableModel extends TableModelBase<IpRangeRule> {

	public IpRangeRuleTableModel(TableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		IpRangeRule rule = getItem(rowIndex);

		if (columnIndex == 0) {
			return rule.getFrom();
		}
		else if (columnIndex == 1) {
			return rule.getTo();
		}

		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		IpRangeRule rule = getItem(row);

		if (nonNull(rule)) {
			if (column == 0) {
				rule.setFrom((String) value);
			}
			else if (column == 1) {
				rule.setTo((String) value);
			}

			fireTableCellUpdated(row, column);
		}
	}
}
