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

package org.lecturestudio.presenter.javafx.view;

import static java.util.Objects.nonNull;

import java.io.File;
import java.net.URI;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.MessageWebServiceState;
import org.lecturestudio.presenter.api.service.QuizWebServiceState;
import org.lecturestudio.presenter.api.view.MenuView;
import org.lecturestudio.web.api.model.quiz.Quiz;

@FxmlView(name = "main-menu", presenter = org.lecturestudio.presenter.api.presenter.MenuPresenter.class)
public class FxMenuView extends HBox implements MenuView {

	private ConsumerAction<Bookmark> openBookmarkAction;

	private ConsumerAction<File> openDocumentAction;

	private ConsumerAction<Quiz> openEmbeddedQuizAction;

	private ConsumerAction<URI> openPageUriAction;

	private ConsumerAction<File> openPageFileLinkAction;

	@FXML
	private Menu fileMenu;

	@FXML
	private Menu bookmarksMenu;

	@FXML
	private Menu pageActionsMenu;

	@FXML
	private Menu embeddedQuizMenu;

	@FXML
	private MenuItem openDocumentMenuItem;

	@FXML
	private MenuItem closeDocumentMenuItem;

	@FXML
	private MenuItem saveDocumentsMenuItem;

	@FXML
	private MenuItem saveQuizMenuItem;

	@FXML
	private MenuItem exitMenuItem;

	@FXML
	private MenuItem undoMenuItem;

	@FXML
	private MenuItem redoMenuItem;

	@FXML
	private MenuItem settingsMenuItem;

	@FXML
	private CheckMenuItem fullscreenMenuItem;

	@FXML
	private CheckMenuItem advancedSettingsMenuItem;

	@FXML
	private MenuItem newWhiteboardMenuItem;

	@FXML
	private MenuItem newWhiteboardPageMenuItem;

	@FXML
	private MenuItem deleteWhiteboardPageMenuItem;

	@FXML
	private CheckMenuItem gridMenuItem;

	@FXML
	private MenuItem startRecordingMenuItem;

	@FXML
	private MenuItem stopRecordingMenuItem;

	@FXML
	private MenuItem startStreamingMenuItem;

	@FXML
	private MenuItem stopStreamingMenuItem;

	@FXML
	private MenuItem startMessengerMenuItem;

	@FXML
	private MenuItem stopMessengerMenuItem;

	@FXML
	private CheckMenuItem showMessengerWindowMenuItem;

	@FXML
	private MenuItem selectQuizMenuItem;

	@FXML
	private MenuItem newQuizMenuItem;

	@FXML
	private MenuItem closeQuizMenuItem;

	@FXML
	private MenuItem clearBookmarksMenuItem;

	@FXML
	private MenuItem newBookmarkMenuItem;

	@FXML
	private MenuItem gotoBookmarkMenuItem;

	@FXML
	private MenuItem previousBookmarkMenuItem;

	@FXML
	private MenuItem aboutMenuItem;

	@FXML
	private MenuItem logMenuItem;

	@FXML
	private Menu timeMenu;

	@FXML
	private Menu recordIndicatorMenu;

	@FXML
	private Label recordTimeLabel;

	@FXML
	private Menu quizIndicatorMenu;

	@FXML
	private Menu messengerIndicatorMenu;

	@FXML
	private Menu streamIndicatorMenu;


	public FxMenuView() {
		super();
	}

	@Override
	public void setDocument(Document doc) {
		final boolean hasDocument = nonNull(doc);
		final boolean isWhiteboard = nonNull(doc) && doc.isWhiteboard();

		FxUtils.invoke(() -> {
			closeDocumentMenuItem.setDisable(!hasDocument);
			saveDocumentsMenuItem.setDisable(!hasDocument);
			newWhiteboardPageMenuItem.setDisable(!isWhiteboard);
			deleteWhiteboardPageMenuItem.setDisable(!isWhiteboard);
			gridMenuItem.setDisable(!isWhiteboard);
			selectQuizMenuItem.setDisable(!hasDocument);
			newQuizMenuItem.setDisable(!hasDocument);
			clearBookmarksMenuItem.setDisable(!hasDocument);
			newBookmarkMenuItem.setDisable(!hasDocument);
			gotoBookmarkMenuItem.setDisable(!hasDocument);
			previousBookmarkMenuItem.setDisable(!hasDocument);
			startRecordingMenuItem.setDisable(!hasDocument);
			startStreamingMenuItem.setDisable(!hasDocument);
			startMessengerMenuItem.setDisable(!hasDocument);
		});
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		FxUtils.invoke(() -> {
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

			undoMenuItem.setDisable(!hasUndo);
			redoMenuItem.setDisable(!hasRedo);
			gridMenuItem.setSelected(hasGrid);
		});
	}

	/**
	 * File Menu
	 */

	@Override
	public void setRecentDocuments(List<RecentDocument> recentDocs) {
		ObservableList<MenuItem> menuItems = fileMenu.getItems();

		// Remove recent document items.
		menuItems.removeIf(item -> nonNull(item.getId()) && item.getId().equals("recent-doc"));

		int offset = menuItems.indexOf(saveQuizMenuItem) + 1;

		if (!recentDocs.isEmpty()) {
			MenuItem docItem = new SeparatorMenuItem();
			docItem.setId("recent-doc");

			menuItems.add(offset++, docItem);
		}

		for (RecentDocument recentDoc : recentDocs) {
			File file = new File(recentDoc.getDocumentPath());

			MenuItem docItem = new MenuItem(file.getName());
			docItem.setId("recent-doc");
			docItem.setOnAction(event -> {
				if (nonNull(openDocumentAction)) {
					openDocumentAction.execute(file);
				}
			});

			menuItems.add(offset++, docItem);
		}
	}

	@Override
	public void setOnOpenDocument(Action action) {
		FxUtils.bindAction(openDocumentMenuItem, action);
	}

	@Override
	public void setOnCloseDocument(Action action) {
		FxUtils.bindAction(closeDocumentMenuItem, action);
	}

	@Override
	public void setOnSaveDocuments(Action action) {
		FxUtils.bindAction(saveDocumentsMenuItem, action);
	}

	@Override
	public void setOnSaveQuizResults(Action action) {
		FxUtils.bindAction(saveQuizMenuItem, action);
	}

	@Override
	public void setOnExit(Action action) {
		FxUtils.bindAction(exitMenuItem, action);
	}

	@Override
	public void setOnOpenBookmark(ConsumerAction<Bookmark> action) {
		this.openBookmarkAction = action;
	}

	/**
	 * Edit Menu
	 */

	@Override
	public void setOnUndo(Action action) {
		FxUtils.bindAction(undoMenuItem, action);
	}

	@Override
	public void setOnRedo(Action action) {
		FxUtils.bindAction(redoMenuItem, action);
	}

	@Override
	public void setOnSettings(Action action) {
		FxUtils.bindAction(settingsMenuItem, action);
	}

	@Override
	public void bindShowOutline(BooleanProperty showProperty) {

	}

	/**
	 * View Menu
	 */

	@Override
	public void setAdvancedSettings(boolean selected) {
		advancedSettingsMenuItem.setSelected(selected);
	}

	@Override
	public void bindFullscreen(BooleanProperty fullscreen) {
		fullscreenMenuItem.selectedProperty().bindBidirectional(new LectBooleanProperty(fullscreen));
	}

	@Override
	public void setOnAdvancedSettings(ConsumerAction<Boolean> action) {
		FxUtils.bindAction(advancedSettingsMenuItem, action);
	}

	/**
	 * Whiteboard Menu
	 */

	@Override
	public void setOnNewWhiteboard(Action action) {
		FxUtils.bindAction(newWhiteboardMenuItem, action);
	}

	@Override
	public void setOnNewWhiteboardPage(Action action) {
		FxUtils.bindAction(newWhiteboardPageMenuItem, action);
	}

	@Override
	public void setOnDeleteWhiteboardPage(Action action) {
		FxUtils.bindAction(deleteWhiteboardPageMenuItem, action);
	}

	@Override
	public void setOnShowGrid(ConsumerAction<Boolean> action) {
		FxUtils.bindAction(gridMenuItem, action);
	}

	/**
	 * Presentation Menu
	 */

	@Override
	public void setOnStartRecording(Action action) {
		FxUtils.bindAction(startRecordingMenuItem, action);
	}

	@Override
	public void setOnStopRecording(Action action) {
		FxUtils.bindAction(stopRecordingMenuItem, action);
	}

	@Override
	public void setOnStartStreaming(Action action) {
		FxUtils.bindAction(startStreamingMenuItem, action);
	}

	@Override
	public void setOnStopStreaming(Action action) {
		FxUtils.bindAction(stopStreamingMenuItem, action);
	}

	@Override
	public void bindEnableStreamingCamera(BooleanProperty enable) {

	}

	@Override
	public void setOnStartMessenger(Action action) {
		FxUtils.bindAction(startMessengerMenuItem, action);
	}

	@Override
	public void setOnStopMessenger(Action action) {
		FxUtils.bindAction(stopMessengerMenuItem, action);
	}

	@Override
	public void setOnShowMessengerWindow(ConsumerAction<Boolean> action) {
		FxUtils.bindAction(showMessengerWindowMenuItem, action);
	}

	@Override
	public void setOnShowSelectQuizView(Action action) {
		FxUtils.bindAction(selectQuizMenuItem, action);
	}

	@Override
	public void setOnShowNewQuizView(Action action) {
		FxUtils.bindAction(newQuizMenuItem, action);
	}

	@Override
	public void setOnCloseQuiz(Action action) {
		FxUtils.bindAction(closeQuizMenuItem, action);
	}

	@Override
	public void setMessengerWindowVisible(boolean visible) {
		showMessengerWindowMenuItem.setSelected(visible);
	}

	@Override
	public void setMessengerState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		FxUtils.invoke(() -> {
			startMessengerMenuItem.setDisable(started);
			stopMessengerMenuItem.setDisable(!started);
			showMessengerWindowMenuItem.setDisable(!started);

			setIndicatorState(messengerIndicatorMenu, state);
		});
	}

	@Override
	public void setQuizState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		FxUtils.invoke(() -> {
			saveQuizMenuItem.setDisable(!started);
			closeQuizMenuItem.setDisable(!started);

			setIndicatorState(quizIndicatorMenu, state);
		});
	}

	@Override
	public void setRecordingState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		FxUtils.invoke(() -> {
			startRecordingMenuItem.setDisable(started);
			stopRecordingMenuItem.setDisable(!started);

			updateRecTimeLabel(state);

			setIndicatorState(recordIndicatorMenu, state);
		});
	}

	@Override
	public void setStreamingState(ExecutableState state) {
		final boolean started = state == ExecutableState.Started;

		FxUtils.invoke(() -> {
			startStreamingMenuItem.setDisable(started);
			stopStreamingMenuItem.setDisable(!started);

			setIndicatorState(streamIndicatorMenu, state);
		});
	}

	/**
	 * Services Menu
	 */

	@Override
	public void setOnControlRecording(ConsumerAction<Boolean> action) {
//		serviceControlPane.setOnRecord(action);
	}

	@Override
	public void setOnControlRecordingSettings(Action action) {
//		serviceControlPane.setOnRecordSettings(action);
	}

	@Override
	public void setOnControlStreaming(ConsumerAction<Boolean> action) {
//		serviceControlPane.setOnStream(action);
	}

	@Override
	public void setOnControlStreamingSettings(Action action) {
//		serviceControlPane.setOnStreamSettings(action);
	}

	@Override
	public void setOnControlMessenger(ConsumerAction<Boolean> action) {
//		serviceControlPane.setOnMessenger(action);
	}

	@Override
	public void setOnControlMessengerSettings(Action action) {
//		serviceControlPane.setOnMessengerSettings(action);
	}

	@Override
	public void setOnControlMessengerWindow(ConsumerAction<Boolean> action) {
//		serviceControlPane.setOnMessengerWindow(action);
	}

	@Override
	public void setOnControlCameraSettings(Action action) {
//		serviceControlPane.setOnCameraSettings(action);
	}

	/**
	 * Bookmarks Menu
	 */

	@Override
	public void setBookmarks(Bookmarks bookmarks) {
		FxUtils.invoke(() -> {
			ObservableList<MenuItem> menuItems = bookmarksMenu.getItems();
			List<Bookmark> bookmarkList = bookmarks.getAllBookmarks();
			int fixedMenuItems = 5;

			// Remove all bookmark menu items.
			if (menuItems.size() > fixedMenuItems) {
				int pos = 0;

				while (pos < menuItems.size() - fixedMenuItems) {
					menuItems.remove(pos);
				}
			}

			if (menuItems.size() == fixedMenuItems && bookmarkList.size() > 0) {
				menuItems.add(0, new SeparatorMenuItem());
			}

			int count = 0;

			for (Bookmark bookmark : bookmarkList) {
				Document doc = bookmark.getPage().getDocument();
				String docName = doc.getName();
				String text = docName + ": " + (doc.getPageIndex(bookmark.getPage()) + 1);

				MenuItem bookmarkItem = new MenuItem(text);
				bookmarkItem.setAccelerator(KeyCombination.keyCombination(bookmark.getShortcut()));
				bookmarkItem.setOnAction(event -> {
					if (nonNull(openBookmarkAction)) {
						openBookmarkAction.execute(bookmark);
					}
				});

				menuItems.add(count++, bookmarkItem);
			}
		});
	}

	@Override
	public void setOnClearBookmarks(Action action) {
		FxUtils.bindAction(clearBookmarksMenuItem, action);
	}

	@Override
	public void setOnShowNewBookmarkView(Action action) {
		FxUtils.bindAction(newBookmarkMenuItem, action);
	}

	@Override
	public void setOnShowGotoBookmarkView(Action action) {
		FxUtils.bindAction(gotoBookmarkMenuItem, action);
	}

	@Override
	public void setOnPreviousBookmark(Action action) {
		FxUtils.bindAction(previousBookmarkMenuItem, action);
	}

	@Override
	public void setOnOpenDocument(ConsumerAction<File> action) {
		this.openDocumentAction = action;
	}

	/**
	 * Embedded Actions
	 */

	@Override
	public void setPageURIs(List<URI> uris) {
		FxUtils.invoke(() -> {
			ObservableList<MenuItem> items = pageActionsMenu.getItems();
			items.removeIf(item -> item.getId().equals("pageURI"));

			if (nonNull(uris) && !uris.isEmpty()) {
				for (final URI uri : uris) {
					MenuItem uriItem = new MenuItem(uri.toString());
					uriItem.setId("pageURI");
					uriItem.setOnAction(event -> {
						if (nonNull(openPageUriAction)) {
							openPageUriAction.execute(uri);
						}
					});

					items.add(uriItem);
				}
			}

			pageActionsMenu.setVisible(!items.isEmpty());
		});
	}

	@Override
	public void setPageFileLinks(List<File> fileLinks) {
		FxUtils.invoke(() -> {
			ObservableList<MenuItem> items = pageActionsMenu.getItems();
			items.removeIf(item -> item.getId().equals("pageFileLink"));

			if (nonNull(fileLinks) && !fileLinks.isEmpty()) {
				for (final File file : fileLinks) {
					MenuItem fileItem = new MenuItem(file.getPath());
					fileItem.setId("pageFileLink");
					fileItem.setOnAction(event -> {
						if (nonNull(openPageFileLinkAction)) {
							openPageFileLinkAction.execute(file);
						}
					});

					items.add(fileItem);
				}
			}

			pageActionsMenu.setVisible(!items.isEmpty());
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

	/**
	 * Embedded Quizzes
	 */

	@Override
	public void setPageQuizzes(List<Quiz> quizzes) {
		FxUtils.invoke(() -> {
			embeddedQuizMenu.getItems().clear();

			boolean hasQuizzes = nonNull(quizzes) && !quizzes.isEmpty();

			if (hasQuizzes) {
				for (final Quiz quiz : quizzes) {
					MenuItem quizItem = new MenuItem(quiz.getQuestion());
					quizItem.setOnAction(event -> {
						if (nonNull(openEmbeddedQuizAction)) {
							openEmbeddedQuizAction.execute(quiz);
						}
					});

					embeddedQuizMenu.getItems().add(quizItem);
				}
			}

			embeddedQuizMenu.setVisible(hasQuizzes);
		});
	}

	@Override
	public void setOnOpenPageQuiz(ConsumerAction<Quiz> action) {
		this.openEmbeddedQuizAction = action;
	}

	/**
	 * Info Menu
	 */

	@Override
	public void setOnOpenLog(Action action) {
		FxUtils.bindAction(logMenuItem, action);
	}

	@Override
	public void setOnOpenAbout(Action action) {
		FxUtils.bindAction(aboutMenuItem, action);
	}

	/**
	 * Time Menu
	 */

	@Override
	public void setCurrentTime(String time) {
		FxUtils.invoke(() -> timeMenu.setText(time));
	}

	/**
	 * Recording time Menu
	 */

	@Override
	public void setRecordingTime(Time time) {
		FxUtils.invoke(() -> {
			recordTimeLabel.setText(time.toString());
		});
	}

	/**
	 * Indicators
	 */

	@Override
	public void setMessageServiceState(MessageWebServiceState state) {
		FxUtils.invoke(() -> {
			messengerIndicatorMenu.setText(Long.toString(state.messageCount));
		});
	}

	@Override
	public void setQuizServiceState(QuizWebServiceState state) {
		FxUtils.invoke(() -> {
			quizIndicatorMenu.setText(Long.toString(state.answerCount));
		});
	}

	@FXML
	private void initialize() {
		sceneProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				Scene scene = getScene();

				if (nonNull(scene)) {
					sceneProperty().removeListener(this);

					onSceneSet(scene);
				}
			}
		});
	}

	private void setIndicatorState(MenuItem styleable, ExecutableState state) {
		if (state == ExecutableState.Started) {
			styleable.getStyleClass().add("indicator-on");
		}
		else if (state == ExecutableState.Stopped || state == ExecutableState.Error) {
			styleable.getStyleClass().remove("indicator-on");
			styleable.setText(null);
		}
	}

	private void updateRecTimeLabel(ExecutableState state) {
		if (state == ExecutableState.Started) {
			Text text = new Text("8:88:88");
			text.setFont(recordTimeLabel.getFont());
			text.setWrappingWidth(0);

			Insets padding = recordTimeLabel.getPadding();
			Insets labelPadding = recordTimeLabel.getLabelPadding();

			double widthPadding = padding.getLeft() + labelPadding.getLeft() + padding.getRight() + labelPadding.getRight();
			double width = text.getLayoutBounds().getWidth();
			double gap = recordTimeLabel.getGraphicTextGap();
			double graphicWidth = recordTimeLabel.getGraphic().getLayoutBounds().getWidth();

			recordTimeLabel.setPrefWidth(graphicWidth + gap + width + widthPadding);
		}
		else if (state == ExecutableState.Stopped) {
			recordTimeLabel.setText(null);
			recordTimeLabel.setPrefWidth(-1);
		}
	}

	private void onSceneSet(Scene scene) {
		scene.windowProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Window> observable, Window oldWindow, Window newWindow) {
				if (nonNull(newWindow)) {
					scene.windowProperty().removeListener(this);

					onStageSet((Stage) newWindow);
				}
			}
		});
	}

	private void onStageSet(Stage stage) {
		stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
			fullscreenMenuItem.setSelected(newValue);
		});
	}

}
