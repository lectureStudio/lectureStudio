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

package org.lecturestudio.swing.table;

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class ButtonEditor extends DefaultCellEditor {

	private static final long serialVersionUID = 3060320592061946170L;

	private final JButton button;

	private Action action;

	private ButtonListener listener;

	private int row;

	private Object object;


	public ButtonEditor() {
		super(new JCheckBox());

		button = new JButton();
		button.setFocusPainted(false);
		button.setOpaque(true);
		button.addActionListener(e -> fireEditingStopped());
	}

	public ButtonEditor(JCheckBox checkBox, Icon icon, ButtonListener listener) {
		super(checkBox);

		this.listener = listener;
		button = new JButton(icon);
		button.setOpaque(true);
		button.setFocusPainted(false);
		button.addActionListener(e -> fireEditingStopped());
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public void setIcon(Icon icon) {
		button.setIcon(icon);
	}

	public void setText(String text) {
		button.setText(text);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.row = row;
		this.object = value;

		return button;
	}

	@Override
	public Object getCellEditorValue() {
		return object;
	}

	@Override
	protected void fireEditingStopped() {
		super.fireEditingStopped();

		if (nonNull(action)) {
			ActionEvent event = new ActionEvent(button,
					ActionEvent.ACTION_PERFORMED, "" + row);
			action.actionPerformed(event);
		}
		if (nonNull(listener)) {
			listener.buttonClicked(row, object);
		}
	}
}
