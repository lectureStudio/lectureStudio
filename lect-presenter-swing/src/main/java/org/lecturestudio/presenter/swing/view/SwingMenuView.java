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
import java.util.List;

import javax.inject.Inject;
import javax.swing.*;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.QuizWebServiceState;
import org.lecturestudio.presenter.api.view.MenuView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "main-menu", presenter = org.lecturestudio.presenter.api.presenter.MenuPresenter.class)
public class SwingMenuView extends JMenuBar implements MenuView {

	private final Dictionary dict;

	private ConsumerAction<Bookmark> openBookmarkAction;

	private ConsumerAction<File> openDocumentAction;

	private JMenu fileMenu;

	private JMenu bookmarksMenu;

	private JMenuItem openDocumentMenuItem;

	private JMenuItem closeDocumentMenuItem;

	private JMenuItem saveDocumentsMenuItem;

	private JMenuItem exitMenuItem;

	private JMenuItem undoMenuItem;

	private JMenuItem redoMenuItem;

	private JMenuItem settingsMenuItem;

	private JCheckBoxMenuItem outlineMenuItem;

	private JCheckBoxMenuItem fullscreenMenuItem;

	private JCheckBoxMenuItem advancedSettingsMenuItem;

	private JMenuItem customizeToolbarMenuItem;

	private JMenu externalWindowsMenu;

	private JCheckBoxMenuItem externalMessagesMenuItem;

	private JCheckBoxMenuItem externalSlidePreviewMenuItem;

	private JCheckBoxMenuItem externalSpeechMenuItem;

	private JMenu messagesPositionMenu;

	private JRadioButtonMenuItem messagesPositionLeftMenuItem;

	private JRadioButtonMenuItem messagesPositionBottomMenuItem;

	private JRadioButtonMenuItem messagesPositionRightMenuItem;

	private JMenuItem newWhiteboardMenuItem;

	private JMenuItem newWhiteboardPageMenuItem;

	private JMenuItem deleteWhiteboardPageMenuItem;

	private JCheckBoxMenuItem gridMenuItem;

	private JMenuItem startRecordingMenuItem;

	private JMenuItem stopRecordingMenuItem;

	private JCheckBoxMenuItem enableStreamMenuItem;

	private JCheckBoxMenuItem enableStreamMicrophoneMenuItem;

	private JCheckBoxMenuItem enableStreamCameraMenuItem;

	private JCheckBoxMenuItem enableMessengerMenuItem;

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

	private JMenu speechIndicatorMenu;

	private JMenu streamIndicatorMenu;


	@Inject
	SwingMenuView(Dictionary dict) {
		super();

		this.dict = dict;
	}

	@Override
	public void setDocument(Document doc) {
		final boolean hasDocument = nonNull(doc);
		final boolean isPdf = nonNull(doc) && doc.isPDF();
		final boolean isWhiteboard = nonNull(doc) && doc.isWhiteboard();

		// TODO: Einträge ausgrauen anzeigen
		SwingUtils.invoke(() -> {
			closeDocumentMenuItem.setEnabled(hasDocument);
			saveDocumentsMenuItem.setEnabled(hasDocument);
			customizeToolbarMenuItem.setEnabled(hasDocument);
			outlineMenuItem.setEnabled(isPdf);
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
			enableStreamMenuItem.setEnabled(hasDocument);
			enableMessengerMenuItem.setEnabled(hasDocument);
			externalWindowsMenu.setEnabled(hasDocument);
			messagesPositionMenu.setEnabled(hasDocument);
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

		int offset = List.of(fileMenu.getMenuComponents()).indexOf(saveDocumentsMenuItem) + 1;

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
		openDocumentAction = action;
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
	public void setOnCustomizeToolbar(Action action) {
		SwingUtils.bindAction(customizeToolbarMenuItem, action);
	}

	@Override
	public void setExternalMessages(boolean selected, boolean show) {
		externalMessagesMenuItem.setSelected(selected);
		externalMessagesMenuItem.setText(
				!selected || show ? dict.get("menu.external.messages") :
						dict.get("menu.external.messages.disconnected"));
	}

	@Override
	public void setOnExternalMessages(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(externalMessagesMenuItem, action);
	}

	@Override
	public void setExternalSlidePreview(boolean selected, boolean show) {
		externalSlidePreviewMenuItem.setSelected(selected);
		externalSlidePreviewMenuItem.setText(
				!selected || show ? dict.get("menu.external.slide.preview") :
						dict.get("menu.external.slide.preview.disconnected"));
	}

	@Override
	public void setOnExternalSlidePreview(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(externalSlidePreviewMenuItem, action);
	}

	@Override
	public void setExternalSpeech(boolean selected, boolean show) {
		externalSpeechMenuItem.setSelected(selected);
		externalSpeechMenuItem.setText(
				!selected || show ? dict.get("menu.external.speech") : dict.get("menu.external.speech.disconnected"));
	}

	@Override
	public void setOnExternalSpeech(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(externalSpeechMenuItem, action);
	}

	@Override
	public void setOnMessagesPositionLeft(Action action) {
		SwingUtils.bindAction(messagesPositionLeftMenuItem, action);
	}

	@Override
	public void setMessagesPositionLeft() {
		messagesPositionLeftMenuItem.setSelected(true);
	}

	@Override
	public void setOnMessagesPositionBottom(Action action) {
		SwingUtils.bindAction(messagesPositionBottomMenuItem, action);
	}

	@Override
	public void setMessagesPositionBottom() {
		messagesPositionBottomMenuItem.setSelected(true);
	}

	@Override
	public void setOnMessagesPositionRight(Action action) {
		SwingUtils.bindAction(messagesPositionRightMenuItem, action);
	}

	@Override
	public void setMessagesPositionRight() {
		messagesPositionRightMenuItem.setSelected(true);
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
	public void bindEnableStream(BooleanProperty enable) {
		SwingUtils.bindBidirectional(enableStreamMenuItem, enable);
	}

	@Override
	public void bindEnableStreamingMicrophone(BooleanProperty enable) {
		SwingUtils.bindBidirectional(enableStreamMicrophoneMenuItem, enable);
	}

	@Override
	public void bindEnableStreamingCamera(BooleanProperty enable) {
		SwingUtils.bindBidirectional(enableStreamCameraMenuItem, enable);
	}

	@Override
	public void bindEnableMessenger(BooleanProperty enable) {
		SwingUtils.bindBidirectional(enableMessengerMenuItem, enable);
	}

	@Override
	public void setOnShowMessengerWindow(ConsumerAction<Boolean> action) {
//		SwingUtils.bindAction(showMessengerWindowMenuItem, action);
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
//		showMessengerWindowMenuItem.setSelected(visible);
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
//			showMessengerWindowMenuItem.setEnabled(started);

			setIndicatorState(messengerIndicatorMenu, state);
		});
	}

	@Override
	public void setQuizState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			closeQuizMenuItem.setEnabled(started);

			setIndicatorState(quizIndicatorMenu, state);
		});
	}

	@Override
	public void setRecordingState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			startRecordingMenuItem.setEnabled(!started && closeDocumentMenuItem.isEnabled());
			stopRecordingMenuItem.setEnabled(started);

			updateRecTimeLabel(state);

			setIndicatorState(recordIndicatorMenu, state);
		});
	}

	@Override
	public void setStreamingState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		SwingUtils.invoke(() -> {
			enableStreamMicrophoneMenuItem.setEnabled(started);
			enableStreamCameraMenuItem.setEnabled(started);

			setIndicatorState(streamIndicatorMenu, state);
			setIndicatorState(speechIndicatorMenu, state);
		});
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
	public void bindMessageCount(IntegerProperty count) {
		count.addListener((observable, oldValue, newValue) -> {
			SwingUtils.invoke(() -> {
				messengerIndicatorMenu.setText(Integer.toString(newValue));
			});
		});
	}

	@Override
	public void bindSpeechRequestCount(IntegerProperty count) {
		count.addListener((observable, oldValue, newValue) -> {
			SwingUtils.invoke(() -> {
				speechIndicatorMenu.setText(Integer.toString(newValue));
			});
		});
	}

	@Override
	public void bindAttendeesCount(IntegerProperty count) {
		count.addListener((observable, oldValue, newValue) -> {
			SwingUtils.invoke(() -> {
				streamIndicatorMenu.setText(Integer.toString(newValue));
			});
		});
	}

	@Override
	public void setQuizServiceState(QuizWebServiceState state) {
		SwingUtils.invoke(() -> {
			quizIndicatorMenu.setText(Long.toString(state.answerCount));
		});
	}

	@ViewPostConstruct
	private void initialize() {
		setStateText(enableStreamMenuItem, dict.get("menu.stream.start"),
				dict.get("menu.stream.stop"));
		setStateText(enableMessengerMenuItem, dict.get("menu.messenger.start"),
				dict.get("menu.messenger.stop"));
		setStateText(enableStreamMicrophoneMenuItem, dict.get("menu.stream.microphone.start"),
				dict.get("menu.stream.microphone.stop"));
		setStateText(enableStreamCameraMenuItem, dict.get("menu.stream.camera.start"),
				dict.get("menu.stream.camera.stop"));

		final ButtonGroup messagesPositionButtonGroup = new ButtonGroup();
		messagesPositionButtonGroup.add(messagesPositionLeftMenuItem);
		messagesPositionButtonGroup.add(messagesPositionBottomMenuItem);
		messagesPositionButtonGroup.add(messagesPositionRightMenuItem);
	}

	private void setStateText(AbstractButton button, String start, String stop) {
		button.addItemListener(e -> {
			button.setText(button.isSelected() ? stop : start);
		});
	}

	private void setIndicatorState(JMenuItem styleable, ExecutableState state) {
		if (state == ExecutableState.Started) {
			styleable.setOpaque(true);
			styleable.setBackground(new Color(210, 210, 210));
		} else if (state == ExecutableState.Suspended) {
			styleable.setOpaque(true);
			styleable.setBackground(new Color(245, 138, 0));
		} else if (state == ExecutableState.Stopped || state == ExecutableState.Error) {
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
