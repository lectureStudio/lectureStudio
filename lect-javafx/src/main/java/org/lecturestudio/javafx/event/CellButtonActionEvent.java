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

package org.lecturestudio.javafx.event;

import javafx.event.ActionEvent;

public class CellButtonActionEvent extends ActionEvent {

	private static final long serialVersionUID = -8113594422881751913L;

	/** The intermediate UI model bound to the source. */
	private final Object item;


	public CellButtonActionEvent(Object source, Object item) {
		super(source, null);

		this.item = item;
	}

	/**
	 * Returns the intermediate UI model.
	 * 
	 * @return the UI model item.
	 */
	public Object getCellItem() {
		return item;
	}

}
