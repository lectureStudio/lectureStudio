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

import static java.util.Objects.isNull;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.view.GotoBookmarkView;
import org.lecturestudio.presenter.swing.view.model.BookmarkTableModel;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "goto-bookmark")
public class SwingGotoBookmarkView extends ContentPane implements GotoBookmarkView {

	private ConsumerAction<Bookmark> gotoBookmarkAction;

	private ConsumerAction<Bookmark> deleteBookmarkAction;

	private JTextField acceleratorTextField;

	private JTable bookmarkTableView;

	private JButton closeButton;

	public javax.swing.Action deleteAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			BookmarkTableModel model = (BookmarkTableModel) bookmarkTableView.getModel();
			Bookmark bookmark = model.getItem(row);

			executeAction(deleteBookmarkAction, bookmark);
		}
	};


	SwingGotoBookmarkView() {
		super();
	}

	@Override
	public void setBookmarks(List<Bookmark> bookmarkList) {
		if (isNull(bookmarkList)) {
			return;
		}

		BookmarkTableModel model = (BookmarkTableModel) bookmarkTableView.getModel();
		model.setItems(bookmarkList);
	}

	@Override
	public void removeBookmark(Bookmark bookmark) {
		BookmarkTableModel model = (BookmarkTableModel) bookmarkTableView.getModel();
		model.removeItem(bookmark);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnGotoBookmark(ConsumerAction<Bookmark> action) {
		gotoBookmarkAction = action;
	}

	@Override
	public void setOnDeleteBookmark(ConsumerAction<Bookmark> action) {
		deleteBookmarkAction = action;
	}

	private void selectBookmark(int row) {
		if (row < 0) {
			return;
		}

		BookmarkTableModel model = (BookmarkTableModel) bookmarkTableView.getModel();
		Bookmark selectedItem = model.getItem(row);

		executeAction(gotoBookmarkAction, selectedItem);
	}

	@ViewPostConstruct
	private void initialize() {
		bookmarkTableView.setModel(new BookmarkTableModel(
				bookmarkTableView.getColumnModel()));

		// Enter-key handler.
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

		bookmarkTableView.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "Select");
		bookmarkTableView.getActionMap().put("Select", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				selectBookmark(table.getSelectedRow());
			}
		});

		// Mouse double-click handler.
		bookmarkTableView.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable table = (JTable) e.getSource();
					selectBookmark(table.getSelectedRow());
				}
			}
		});

		acceleratorTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				String s = acceleratorTextField.getText();
				s = isNull(s) ? "" : s.trim();

				if (!s.isEmpty() && !s.isBlank()) {
					char c = s.charAt(0);

					if (Character.isLetter(c) || Character.isDigit(c)) {
						Bookmark bookmark = new Bookmark(String.valueOf(c));
						executeAction(gotoBookmarkAction, bookmark);
					}
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {

			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}
		});
		acceleratorTextField.addHierarchyListener(e -> {
			acceleratorTextField.requestFocusInWindow();
		});
	}
}
