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

import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.MessageWebServiceState;
import org.lecturestudio.presenter.api.service.QuizWebServiceState;
import org.lecturestudio.presenter.api.view.MenuView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.web.api.model.quiz.Quiz;

@SwingView(name = "main-menu", presenter = org.lecturestudio.presenter.api.presenter.MenuPresenter.class)
public class SwingMenuView extends JMenuBar implements MenuView {

	private ConsumerAction<Bookmark> openBookmarkAction;

	private ConsumerAction<File> openDocumentAction;

	private ConsumerAction<Quiz> openEmbeddedQuizAction;

	private ConsumerAction<URI> openPageUriAction;

	private ConsumerAction<File> openPageFileLinkAction;

	private JMenu fileMenu;

	private JMenu bookmarksMenu;

	private JMenu pageActionsMenu;

	private JMenu embeddedQuizMenu;

	private JMenuItem openDocumentMenuItem;

	private JMenuItem closeDocumentMenuItem;

	private JMenuItem saveDocumentsMenuItem;

	private JMenuItem saveQuizMenuItem;

	private JMenuItem exitMenuItem;

	private JMenuItem undoMenuItem;

	private JMenuItem redoMenuItem;

	private JMenuItem settingsMenuItem;

	private JCheckBoxMenuItem outlineMenuItem;

	private JCheckBoxMenuItem fullscreenMenuItem;

	private JCheckBoxMenuItem advancedSettingsMenuItem;

	private JMenuItem newWhiteboardMenuItem;

	private JMenuItem newWhiteboardPageMenuItem;

	private JMenuItem deleteWhiteboardPageMenuItem;

	private JCheckBoxMenuItem gridMenuItem;

	private JMenuItem startRecordingMenuItem;

	private JMenuItem stopRecordingMenuItem;

	private JMenuItem startStreamingMenuItem;

	private JMenuItem stopStreamingMenuItem;

	private JMenuItem startMessengerMenuItem;

	private JMenuItem stopMessengerMenuItem;

	private JCheckBoxMenuItem showMessengerWindowMenuItem;

	private JMenuItem selectQuizMenuItem;

	private JMenuItem newQuizMenuItem;

	private JMenuItem closeQuizMenuItem;

	private JMenuItem clearBookmarksMenuItem;

	private JMenuItem newBookmarkMenuItem;

	private JMenuItem gotoBookmarkMenuItem;

	private JMenuItem previousBookmarkMenuItem;

	private JMenuItem logMenuItem;

	private JMenuItem aboutMenuItem;

	private JMenu timeMenu;

	private JMenu recordIndicatorMenu;

	private JMenu quizIndicatorMenu;

	private JMenu messengerIndicatorMenu;

	private JMenu streamIndicatorMenu;


	SwingMenuView() {
		super();
	}

	@Override
	public void setDocument(Document doc) {
		final boolean hasDocument = nonNull(doc);
		final boolean isWhiteboard = nonNull(doc) && doc.isWhiteboard();

		SwingUtils.invoke(() -> {
			closeDocumentMenuItem.setEnabled(hasDocument);
			saveDocumentsMenuItem.setEnabled(hasDocument);
			newWhiteboardPageMenuItem.setEnabled(isWhiteboard);
			deleteWhiteboardPageMenuItem.setEnabled(isWhiteboard);
			gridMenuItem.setEnabled(isWhiteboard);
			selectQuizMenuItem.setEnabled(hasDocument);
			newQuizMenuItem.setEnabled(hasDocument);
			clearBookmarksMenuItem.setEnabled(hasDocument);
			newBookmarkMenuItem.setEnabled(hasDocument);
			gotoBookmarkMenuItem.setEnabled(hasDocument);
			previousBookmarkMenuItem.setEnabled(hasDocument);
			startRecordingMenuItem.setEnabled(hasDocument);
			startStreamingMenuItem.setEnabled(hasDocument);
			startMessengerMenuItem.setEnabled(hasDocument);
		});
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		SwingUtils.invoke(() -> {
			boolean hasUndo = false;
			boolean hasRedo = false;
			boolean hasGrid = false;

			if (nonNull(page)) {
				hasUndo = page.hasUndoActions();
				hasRedo = page.hasRedoActions();
			}
			if (nonNull(parameter)) {
				hasGrid = parameter.showGrid();
			}

			undoMenuItem.setEnabled(hasUndo);
			redoMenuItem.setEnabled(hasRedo);
			gridMenuItem.setSelected(hasGrid);
		});
	}

	@Override
	public void setRecentDocuments(List<RecentDocument> recentDocs) {
		// Remove recent document items.
		for (Component item : fileMenu.getMenuComponents()) {
			if (nonNull(item)) {
				String name = item.getName();

				if (nonNull(name) && name.equals("recent-doc")) {
					fileMenu.remove(item);
				}
			}
		}

		int offset = List.of(fileMenu.getMenuComponents()).indexOf(saveQuizMenuItem) + 1;

		if (!recentDocs.isEmpty()) {
			JSeparator separator = new JPopupMenu.Separator();
			separator.setName("recent-doc");

			fileMenu.add(separator, offset++);
		}

		for (RecentDocument recentDoc : recentDocs) {
			File file = new File(recentDoc.getDocumentPath());

			JMenuItem docItem = new JMenuItem(file.getName());
			docItem.setName("recent-doc");
			docItem.addActionListener(event -> {
				if (nonNull(openDocumentAction)) {
					openDocumentAction.execute(file);
				}
			});

			fileMenu.add(docItem, offset++);
		}
	}

	@Override
	public void setOnOpenDocument(Action action) {
		SwingUtils.bindAction(openDocumentMenuItem, action);
	}

	@Override
	public void setOnOpenDocument(ConsumerAction<File> action) {
		this.openDocumentAction = action;
	}

	@Override
	public void setOnCloseDocument(Action action) {
		SwingUtils.bindAction(closeDocumentMenuItem, action);
	}

	@Override
	public void setOnSaveDocuments(Action action) {
		SwingUtils.bindAction(saveDocumentsMenuItem, action);
	}

	@Override
	public void setOnSaveQuizResults(Action action) {
		SwingUtils.bindAction(saveQuizMenuItem, action);
	}

	@Override
	public void setOnExit(Action action) {
		SwingUtils.bindAction(exitMenuItem, action);
	}

	@Override
	public void setOnUndo(Action action) {
		SwingUtils.bindAction(undoMenuItem, action);
	}

	@Override
	public void setOnRedo(Action action) {
		SwingUtils.bindAction(redoMenuItem, action);
	}

	@Override
	public void setOnSettings(Action action) {
		SwingUtils.bindAction(settingsMenuItem, action);
	}

	@Override
	public void bindShowOutline(BooleanProperty showProperty) {
		SwingUtils.bindBidirectional(outlineMenuItem, showProperty);
	}

	@Override
	public void setAdvancedSettings(boolean selected) {
		advancedSettingsMenuItem.setSelected(selected);
	}

	@Override
	public void bindFullscreen(BooleanProperty fullscreen) {
		SwingUtils.bindBidirectional(fullscreenMenuItem, fullscreen);
	}

	@Override
	public void setOnAdvancedSettings(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(advancedSettingsMenuItem, action);
	}

	@Override
	public void setOnNewWhiteboard(Action action) {
		SwingUtils.bindAction(newWhiteboardMenuItem, action);
	}

	@Override
	public void setOnNewWhiteboardPage(Action action) {
		SwingUtils.bindAction(newWhiteboardPageMenuItem, action);
	}

	@Override
	public void setOnDeleteWhiteboardPage(Action action) {
		SwingUtils.bindAction(deleteWhiteboardPageMenuItem, action);
	}

	@Override
	public void setOnShowGrid(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(gridMenuItem, action);
	}

	@Override
	public void setOnStartRecording(Action action) {
		SwingUtils.bindAction(startRecordingMenuItem, action);
	}

	@Override
	public void setOnStopRecording(Action action) {
		SwingUtils.bindAction(stopRecordingMenuItem, action);
	}

	@Override
	public void setOnStartStreaming(Action action) {
		SwingUtils.bindAction(startStreamingMenuItem, action);
	}

	@Override
	public void setOnStopStreaming(Action action) {
		SwingUtils.bindAction(stopStreamingMenuItem, action);
	}

	@Override
	public void setOnStartMessenger(Action action) {
		SwingUtils.bindAction(startMessengerMenuItem, action);
	}

	@Override
	public void setOnStopMessenger(Action action) {
		SwingUtils.bindAction(stopMessengerMenuItem, action);
	}

	@Override
	public void setOnShowMessengerWindow(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(showMessengerWindowMenuItem, action);
	}

	@Override
	public void setOnShowSelectQuizView(Action action) {
		SwingUtils.bindAction(selectQuizMenuItem, action);
	}

	@Override
	public void setOnShowNewQuizView(Action action) {
		SwingUtils.bindAction(newQuizMenuItem, action);
	}

	@Override
	public void setOnCloseQuiz(Action action) {
		SwingUtils.bindAction(closeQuizMenuItem, action);
	}

	@Override
	public void setMessengerWindowVisible(boolean visible) {
		showMessengerWindowMenuItem.setSelected(visible);
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			startMessengerMenuItem.setEnabled(!started);
			stopMessengerMenuItem.setEnabled(started);
			showMessengerWindowMenuItem.setEnabled(started);

			setIndicatorState(messengerIndicatorMenu, state);
		});
	}

	@Override
	public void setQuizState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			saveQuizMenuItem.setEnabled(started);
			closeQuizMenuItem.setEnabled(started);

			setIndicatorState(quizIndicatorMenu, state);
		});
	}

	@Override
	public void setRecordingState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			startRecordingMenuItem.setEnabled(!started);
			stopRecordingMenuItem.setEnabled(started);

			updateRecTimeLabel(state);

			setIndicatorState(recordIndicatorMenu, state);
		});
	}

	@Override
	public void setStreamingState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			startStreamingMenuItem.setEnabled(!started);
			stopStreamingMenuItem.setEnabled(started);

			setIndicatorState(streamIndicatorMenu, state);
		});
	}

	@Override
	public void setOnControlRecording(ConsumerAction<Boolean> action) {

	}

	@Override
	public void setOnControlRecordingSettings(Action action) {

	}

	@Override
	public void setOnControlStreaming(ConsumerAction<Boolean> action) {

	}

	@Override
	public void setOnControlStreamingSettings(Action action) {

	}

	@Override
	public void setOnControlMessenger(ConsumerAction<Boolean> action) {

	}

	@Override
	public void setOnControlMessengerWindow(ConsumerAction<Boolean> action) {

	}

	@Override
	public void setOnControlMessengerSettings(Action action) {

	}

	@Override
	public void setOnControlCamera(ConsumerAction<Boolean> action) {

	}

	@Override
	public void setOnControlCameraSettings(Action action) {

	}

	@Override
	public void setBookmarks(Bookmarks bookmarks) {
		SwingUtils.invoke(() -> {
			List<Bookmark> bookmarkList = bookmarks.getAllBookmarks();
			int fixedMenuItems = 5;

			// Remove all bookmark menu items.
			if (bookmarksMenu.getItemCount() > fixedMenuItems) {
				int pos = 0;

				while (pos < bookmarksMenu.getItemCount() - fixedMenuItems) {
					bookmarksMenu.remove(pos);
				}
			}

			if (bookmarksMenu.getItemCount() == fixedMenuItems && bookmarkList.size() > 0) {
				bookmarksMenu.add(new JPopupMenu.Separator(), 0);
			}

			int count = 0;

			for (Bookmark bookmark : bookmarkList) {
				Document doc = bookmark.getPage().getDocument();
				String docName = doc.getName();
				String text = docName + ": " + (doc.getPageIndex(bookmark.getPage()) + 1);

				JMenuItem bookmarkItem = new JMenuItem(text);
				bookmarkItem.setAccelerator(KeyStroke.getKeyStroke(bookmark.getShortcut()));
				bookmarkItem.addActionListener(event -> {
					if (nonNull(openBookmarkAction)) {
						openBookmarkAction.execute(bookmark);
					}
				});

				bookmarksMenu.add(bookmarkItem, count++);
			}
		});
	}

	@Override
	public void setOnClearBookmarks(Action action) {
		SwingUtils.bindAction(clearBookmarksMenuItem, action);
	}

	@Override
	public void setOnShowNewBookmarkView(Action action) {
		SwingUtils.bindAction(newBookmarkMenuItem, action);
	}

	@Override
	public void setOnShowGotoBookmarkView(Action action) {
		SwingUtils.bindAction(gotoBookmarkMenuItem, action);
	}

	@Override
	public void setOnPreviousBookmark(Action action) {
		SwingUtils.bindAction(previousBookmarkMenuItem, action);
	}

	@Override
	public void setOnOpenBookmark(ConsumerAction<Bookmark> action) {
		this.openBookmarkAction = action;
	}

	@Override
	public void setPageURIs(List<URI> uris) {
		SwingUtils.invoke(() -> {
			for (int i = 0; i < pageActionsMenu.getItemCount(); i++) {
				JMenuItem item = pageActionsMenu.getItem(i);

				if (item.getActionCommand().equals("pageURI")) {
					pageActionsMenu.remove(item);
				}
			}

			if (nonNull(uris) && !uris.isEmpty()) {
				for (final URI uri : uris) {
					JMenuItem uriItem = new JMenuItem(uri.toString());
					uriItem.setActionCommand("pageURI");
					uriItem.addActionListener(event -> {
						if (nonNull(openPageUriAction)) {
							openPageUriAction.execute(uri);
						}
					});

					pageActionsMenu.add(uriItem);
				}
			}

			pageActionsMenu.setVisible(pageActionsMenu.getItemCount() > 0);
		});
	}

	@Override
	public void setPageFileLinks(List<File> fileLinks) {
		SwingUtils.invoke(() -> {
			for (int i = 0; i < pageActionsMenu.getItemCount(); i++) {
				JMenuItem item = pageActionsMenu.getItem(i);

				if (item.getActionCommand().equals("pageFileLink")) {
					pageActionsMenu.remove(item);
				}
			}

			if (nonNull(fileLinks) && !fileLinks.isEmpty()) {
				for (final File file : fileLinks) {
					JMenuItem fileItem = new JMenuItem(file.getPath());
					fileItem.setActionCommand("pageFileLink");
					fileItem.addActionListener(event -> {
						if (nonNull(openPageFileLinkAction)) {
							openPageFileLinkAction.execute(file);
						}
					});

					pageActionsMenu.add(fileItem);
				}
			}

			pageActionsMenu.setVisible(pageActionsMenu.getItemCount() > 0);
		});
	}

	@Override
	public void setOnOpenPageURI(ConsumerAction<URI> action) {
		this.openPageUriAction = action;
	}

	@Override
	public void setOnOpenPageFileLink(ConsumerAction<File> action) {
		this.openPageFileLinkAction = action;
	}

	@Override
	public void setPageQuizzes(List<Quiz> quizzes) {
		SwingUtils.invoke(() -> {
			embeddedQuizMenu.removeAll();

			boolean hasQuizzes = nonNull(quizzes) && !quizzes.isEmpty();

			if (hasQuizzes) {
				for (final Quiz quiz : quizzes) {
					JMenuItem quizItem = new JMenuItem(quiz.getQuestion());
					quizItem.addActionListener(event -> {
						if (nonNull(openEmbeddedQuizAction)) {
							openEmbeddedQuizAction.execute(quiz);
						}
					});

					embeddedQuizMenu.add(quizItem);
				}
			}

			embeddedQuizMenu.setVisible(hasQuizzes);
		});
	}

	@Override
	public void setOnOpenPageQuiz(ConsumerAction<Quiz> action) {
		this.openEmbeddedQuizAction = action;
	}

	@Override
	public void setOnOpenLog(Action action) {
		SwingUtils.bindAction(logMenuItem, action);
	}

	@Override
	public void setOnOpenAbout(Action action) {
		SwingUtils.bindAction(aboutMenuItem, action);
	}

	@Override
	public void setCurrentTime(String time) {
		SwingUtils.invoke(() -> timeMenu.setText(time));
	}

	@Override
	public void setRecordingTime(Time time) {
		SwingUtils.invoke(() -> recordIndicatorMenu.setText(time.toString()));
	}

	@Override
	public void setMessageServiceState(MessageWebServiceState state) {
		SwingUtils.invoke(() -> {
			messengerIndicatorMenu.setText(Long.toString(state.messageCount));
		});
	}

	@Override
	public void setQuizServiceState(QuizWebServiceState state) {
		SwingUtils.invoke(() -> {
			quizIndicatorMenu.setText(Long.toString(state.answerCount));
		});
	}

	private void setIndicatorState(JMenuItem styleable, ExecutableState state) {
		if (state == ExecutableState.Started) {
			styleable.setOpaque(true);
			styleable.setBackground(new Color(210, 210, 210));
		}
		else if (state == ExecutableState.Suspended) {
			styleable.setOpaque(true);
			styleable.setBackground(new Color(245, 138, 0));
		}
		else if (state == ExecutableState.Stopped || state == ExecutableState.Error) {
			styleable.setOpaque(false);
			styleable.setBackground(null);
			styleable.setText("");
			styleable.setText(null);
		}
	}

	private void updateRecTimeLabel(ExecutableState state) {
		if (state == ExecutableState.Stopped) {
			recordIndicatorMenu.setText(null);
		}
	}
}
