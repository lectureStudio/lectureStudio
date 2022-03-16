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

import static java.util.Objects.isNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

public abstract class TableModelBase<T> extends AbstractTableModel {

	private final List<String> columnNames;

	private final List<T> data;

	private final Map<Integer, Class<?>> explicitColumnTypes;


	public TableModelBase() {
		this(new ArrayList<>(), new ArrayList<>(), new HashMap<>());
	}

	public TableModelBase(TableColumnModel columnModel) {
		this(Collections.list(columnModel.getColumns()).stream()
				.map(c -> (String) c.getHeaderValue())
				.collect(Collectors.toList()), new HashMap<>());
	}

	public TableModelBase(TableColumnModel columnModel, Map<Integer, Class<?>> explicitColumnTypes) {
		this(Collections.list(columnModel.getColumns()).stream()
				.map(c -> (String) c.getHeaderValue())
				.collect(Collectors.toList()), explicitColumnTypes);
	}

	public TableModelBase(List<String> columnNames, Map<Integer, Class<?>> explicitColumnTypes) {
		this(new ArrayList<>(), columnNames, explicitColumnTypes);
	}

	public TableModelBase(List<T> data, List<String> columnNames, Map<Integer, Class<?>> explicitColumnTypes) {
		this.data = data;
		this.columnNames = columnNames;
		this.explicitColumnTypes = explicitColumnTypes;
	}

	public void addItem(T item) {
		data.add(item);

		fireTableDataChanged();
	}

	public void addItems(List<T> items) {
		data.addAll(items);

		fireTableDataChanged();
	}

	public void setItems(List<T> items) {
		data.clear();

		addItems(items);
	}

	public T getItem(int row) {
		return data.get(row);
	}

	public void removeItem(int row) {
		data.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public void removeItem(T item) {
		int index = data.indexOf(item);

		if (data.remove(item)) {
			fireTableRowsDeleted(index, index);
		}
	}

	public boolean removeIf(Predicate<? super T> filter) {
		boolean removed = data.removeIf(filter);

		if (removed) {
			fireTableDataChanged();
		}

		return removed;
	}

	public int getRow(T item) {
		for (int i = 0; i < data.size(); i++) {
			if (item == data.get(i)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (explicitColumnTypes.containsKey(columnIndex)) {
			return explicitColumnTypes.get(columnIndex);
		}

		Object value = getValueAt(0, columnIndex);

		if (isNull(value)) {
			return String.class;
		}

		return value.getClass();
	}

	@Override
	public String getColumnName(int column) {
		if (column < 0 || column > columnNames.size() - 1) {
			return super.getColumnName(column);
		}

		return columnNames.get(column);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col > -1;
	}
}
