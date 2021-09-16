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

package org.lecturestudio.presenter.swing.combobox;

import org.lecturestudio.web.api.model.DLZRoom;

import javax.swing.*;
import java.awt.*;

import static java.util.Objects.nonNull;

/**
 * @author Daniel Schröter, Alex Andres
 * Class which initializes an combobox containing DLZRooms
 */
public class DLZRoomRenderer extends DefaultListCellRenderer {

	/**
	 * representing an entry of the combobox
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		DLZRoom room = (DLZRoom) value;

		if (nonNull(room)) {
			setText(String.format("%s", room.getName()));
		}
		else {
			setText("");
		}

		return this;
	}
}
