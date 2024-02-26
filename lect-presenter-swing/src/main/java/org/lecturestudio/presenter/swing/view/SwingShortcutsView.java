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

package org.lecturestudio.presenter.swing.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.view.ShortcutsView;
import org.lecturestudio.presenter.swing.view.model.ShortcutsTableModel;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

@SwingView(name = "shortcuts")
public class SwingShortcutsView extends ContentPane implements ShortcutsView {

	private JTextField searchTextField;

	private JTable shortcutsTable;

	private JButton closeButton;


	public SwingShortcutsView() {
		super();
	}

	@Override
	public void setShortcuts(Map<String, List<Shortcut>> shortcuts) {
		SwingUtils.invoke(() -> {
			List<SimpleEntry<String, List<Shortcut>>> entries = new ArrayList<>();

			for (var entry : shortcuts.entrySet()) {
				entries.add(new SimpleEntry<>(entry.getKey(), entry.getValue()));
			}

			SwingUtils.invoke(() -> {
				ShortcutsTableModel model = (ShortcutsTableModel) shortcutsTable.getModel();
				model.setItems(entries);
			});
		});
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		TableModel tableModel = new ShortcutsTableModel(shortcutsTable.getColumnModel());
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableModel);

		shortcutsTable.setModel(tableModel);
		shortcutsTable.setRowSorter(rowSorter);

		searchTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				filterTable();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterTable();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				throw new UnsupportedOperationException("Not supported yet");
			}

			private void filterTable() {
				String text = searchTextField.getText();

				if (text.trim().isEmpty()) {
					rowSorter.setRowFilter(null);
				}
				else {
					rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
				}
			}
		});
		searchTextField.addHierarchyListener(e -> {
			searchTextField.requestFocusInWindow();
		});
	}
}
