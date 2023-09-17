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

package org.lecturestudio.editor.javafx.view;

import static java.util.Objects.nonNull;

import java.io.File;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.MenuView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "main-menu", presenter = org.lecturestudio.editor.api.presenter.MenuPresenter.class)
public class FxMenuView extends MenuBar implements MenuView {

	private ConsumerAction<File> openDocumentAction;

	@FXML
	private Menu fileMenu;

	@FXML
	private MenuItem openRecordingMenuItem;

	@FXML
	private MenuItem closeRecordingMenuItem;

	@FXML
	private MenuItem saveDocumentMenuItem;

	@FXML
	private MenuItem saveCurrentPageMenuItem;

	@FXML
	private MenuItem saveRecordingAsMenuItem;

	@FXML
	private MenuItem exportAudioMenuItem;

	@FXML
	private MenuItem importAudioMenuItem;

	@FXML
	private MenuItem exitMenuItem;

	@FXML
	private MenuItem undoMenuItem;

	@FXML
	private MenuItem redoMenuItem;

	@FXML
	private MenuItem cutMenuItem;

	@FXML
	private MenuItem deletePageMenuItem;

	@FXML
	private MenuItem normalizeAudioLoudnessMenuItem;

	@FXML
	private MenuItem settingsMenuItem;

	@FXML
	private CheckMenuItem fullscreenMenuItem;

	@FXML
	private MenuItem logMenuItem;

	@FXML
	private MenuItem aboutMenuItem;


	public FxMenuView() {
		super();
	}

	@Override
	public void setDocument(Document doc) {
		final boolean hasDocument = nonNull(doc);

		FxUtils.invoke(() -> {
			closeRecordingMenuItem.setDisable(!hasDocument);
			saveDocumentMenuItem.setDisable(!hasDocument);
			saveCurrentPageMenuItem.setDisable(!hasDocument);
			saveRecordingAsMenuItem.setDisable(!hasDocument);
			exportAudioMenuItem.setDisable(!hasDocument);
			importAudioMenuItem.setDisable(!hasDocument);
			normalizeAudioLoudnessMenuItem.setDisable(!hasDocument);
		});
	}

	/**
	 * File Menu
	 */

	@Override
	public void setRecentDocuments(List<RecentDocument> recentDocs) {
		final ObservableList<MenuItem> menuItems = fileMenu.getItems();

		FxUtils.invoke(() -> {
			// Remove recent document items.
			menuItems.removeIf(item -> nonNull(item.getId()) && item.getId().equals("recent-doc"));

			int offset = menuItems.indexOf(importAudioMenuItem) + 1;

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
		});
	}

	@Override
	public void setOnOpenRecording(Action action) {
		FxUtils.bindAction(openRecordingMenuItem, action);
	}

	@Override
	public void setOnCloseRecording(Action action) {
		FxUtils.bindAction(closeRecordingMenuItem, action);
	}

	@Override
	public void setOnSaveDocument(Action action) {
		FxUtils.bindAction(saveDocumentMenuItem, action);
	}

	@Override
	public void setOnSaveCurrentPage(Action action) {
		FxUtils.bindAction(saveCurrentPageMenuItem, action);
	}

	@Override
	public void setOnSaveRecordingAs(Action action) {
		FxUtils.bindAction(saveRecordingAsMenuItem, action);
	}

	@Override
	public void setOnExportAudio(Action action) {
		FxUtils.bindAction(exportAudioMenuItem, action);
	}

	@Override
	public void setOnImportAudio(Action action) {
		FxUtils.bindAction(importAudioMenuItem, action);
	}

	@Override
	public void setOnExit(Action action) {
		FxUtils.bindAction(exitMenuItem, action);
	}

	@Override
	public void bindCanCut(BooleanProperty property) {
		cutMenuItem.disableProperty().bind(new LectBooleanProperty(property).not());
	}

	@Override
	public void bindCanUndo(BooleanProperty property) {
		undoMenuItem.disableProperty().bind(new LectBooleanProperty(property).not());
	}

	@Override
	public void bindCanRedo(BooleanProperty property) {
		redoMenuItem.disableProperty().bind(new LectBooleanProperty(property).not());
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
	public void setOnCut(Action action) {
		FxUtils.bindAction(cutMenuItem, action);
	}

	@Override
	public void setOnDeletePage(Action action) {
		FxUtils.bindAction(deletePageMenuItem, action);
	}

	@Override
	public void setOnNormalizeAudioLoudness(Action action) {
		FxUtils.bindAction(normalizeAudioLoudnessMenuItem, action);
	}

	@Override
	public void setOnSettings(Action action) {
		FxUtils.bindAction(settingsMenuItem, action);
	}

	/**
	 * View Menu
	 */

	@Override
	public void bindFullscreen(BooleanProperty fullscreen) {
		fullscreenMenuItem.selectedProperty().bindBidirectional(new LectBooleanProperty(fullscreen));
	}

	@Override
	public void setOnOpenRecording(ConsumerAction<File> action) {
		this.openDocumentAction = action;
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
