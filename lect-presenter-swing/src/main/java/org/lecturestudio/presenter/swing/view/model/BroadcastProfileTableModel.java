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

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.swing.table.TableModelBase;

public class BroadcastProfileTableModel extends TableModelBase<BroadcastProfile> {

	public BroadcastProfileTableModel(TableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		BroadcastProfile profile = getItem(rowIndex);

		switch (columnIndex) {
			case 0:
				return profile.getName();
			case 1:
				return profile.getBroadcastAddress();
			case 2:
				return profile.getBroadcastPort();
			case 3:
				return profile.getBroadcastTlsPort();
		}

		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		BroadcastProfile profile = getItem(row);

		if (nonNull(profile)) {
			if (column == 0) {
				profile.setName((String) value);
			}
			else if (column == 1) {
				profile.setBroadcastAddress((String) value);
			}
			else if (column == 2) {
				profile.setBroadcastPort((Integer) value);
			}
			else if (column == 3) {
				profile.setBroadcastTlsPort((Integer) value);
			}

			fireTableCellUpdated(row, column);
		}
	}
}
