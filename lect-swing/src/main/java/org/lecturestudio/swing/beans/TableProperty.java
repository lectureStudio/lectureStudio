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

package org.lecturestudio.swing.beans;

import static java.util.Objects.isNull;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.swing.table.TableModelBase;

public class TableProperty<T> extends ObjectProperty<T> {

	private final JTable table;

	private boolean valid = true;


	public TableProperty(JTable table) {
		this.table = table;

		table.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (table.getSelectedRow() < 0) {
				// Happens if model::clear() is called.
				TableModelBase<T> model = getModel();
				ListSelectionModel selectionModel = table.getSelectionModel();

				// Try to select the previously selected row.
				for (int i = 0; i < model.getRowCount(); i++) {
					T item = model.getItem(i);

					if (item.equals(super.get())) {
						selectionModel.setSelectionInterval(i, i);
						return;
					}
				}

				// By default select first row.
				selectionModel.setSelectionInterval(0, 0);
				return;
			}

			super.set(getModel().getItem(table.getSelectedRow()));
		});

		table.addPropertyChangeListener("model", evt -> {
			TableModelBase<T> newModel = getModel();
			ListSelectionModel selectionModel = table.getSelectionModel();

			for (int i = 0; i < newModel.getRowCount(); i++) {
				T item = newModel.getItem(i);

				if (item.equals(super.get())) {
					selectionModel.setSelectionInterval(i, i);
				}
			}
		});
	}

	@Override
	public T get() {
		if (table.getSelectedRow() < 0) {
			return null;
		}

		TableModelBase<T> model = getModel();

		return model.getItem(table.getSelectedRow());
	}

	@Override
	public void set(T value) {
		final T current = get();

		if (value == current) {
			return;
		}
		if (isNull(current) || !current.equals(value)) {
			if (valid) {
				valid = false;

				TableModelBase<T> model = getModel();

				for (int i = 0; i < model.getRowCount(); i++) {
					T item = model.getItem(i);

					if (item.equals(value)) {
						ListSelectionModel selectionModel = table.getSelectionModel();
						selectionModel.setSelectionInterval(i, i);
					}
				}

				super.set(value);
				valid = true;
			}
		}
	}

	private TableModelBase<T> getModel() {
		return (TableModelBase<T>) table.getModel();
	}
}
