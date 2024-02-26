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

import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.swing.converter.KeyEventConverter;
import org.lecturestudio.swing.table.TableModelBase;

import javax.swing.table.TableColumnModel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.stream.Collectors;

public class ShortcutsTableModel extends TableModelBase<SimpleEntry<String, List<Shortcut>>> {

	public ShortcutsTableModel(TableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SimpleEntry<String, List<Shortcut>> entry = getItem(rowIndex);

		if (columnIndex == 0) {
			return entry.getKey();
		}
		else if (columnIndex == 1) {
			List<Shortcut> mapList = entry.getValue();

			return mapList.stream().map(shortcut -> {
				KeyEvent keyEvent = KeyEventConverter.INSTANCE.to(shortcut.getKeyEvent());
				String modifierString = InputEvent.getModifiersExText(keyEvent.getModifiersEx());
				String keyString = KeyEvent.getKeyText(keyEvent.getKeyCode());

				if (!modifierString.isEmpty() || !modifierString.isBlank()) {
					keyString = modifierString + " + " + keyString;
				}
				return keyString;
			}).collect(Collectors.joining(", "));
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
