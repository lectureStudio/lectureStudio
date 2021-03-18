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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.nio.file.Path;
import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.StartView;
import org.lecturestudio.javafx.control.DocButton;
import org.lecturestudio.javafx.control.ExtButton;
import org.lecturestudio.javafx.control.SvgIcon;
import org.lecturestudio.javafx.control.TextSearchField;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxView;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "main-start", presenter = org.lecturestudio.editor.api.presenter.StartPresenter.class)
public class FxStartView extends VBox implements StartView, FxView {

	private final EventHandler<ActionEvent> recentButtonListener = event -> {
		DocButton source = (DocButton) event.getSource();
		onOpenRecentDocument(source.getDocument());
	};

	private ConsumerAction<RecentDocument> openRecentDocumentAction;

	@FXML
	private TextSearchField<Path> recordingSearchField;

	@FXML
	private ExtButton openRecordingButton;

	@FXML
	private Pane docContainer;


	public FxStartView() {
		super();

		registerOnSceneSet();
	}

	@Override
	public void setRecentRecording(Collection<RecentDocument> documents) {
		FxUtils.invoke(() -> {
			docContainer.getChildren().clear();

			for (RecentDocument doc : documents) {
				SvgIcon icon = new SvgIcon();
				icon.getStyleClass().add("pdf-icon");

				DocButton button = new DocButton(doc);
				button.setIcon(icon);
				button.setOnAction(recentButtonListener);

				docContainer.getChildren().add(button);
			}
		});
	}

	@Override
	public void setSearchResult(Collection<Path> result) {
		FxUtils.invoke(() -> {
			if (isNull(result)) {
				recordingSearchField.getSuggestions().clear();
			}
			else {
				recordingSearchField.getSuggestions().setAll(result);
			}
		});
	}

	@Override
	public void setOnOpenRecording(Action action) {
		FxUtils.bindAction(openRecordingButton, action);
	}

	@Override
	public void setOnSearch(ConsumerAction<String> action) {
		recordingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
			action.execute(newValue);
		});
	}

	@Override
	public void setOnOpenRecentRecording(ConsumerAction<RecentDocument> action) {
		this.openRecentDocumentAction = action;
	}

	@Override
	public void onSceneSet() {
		openRecordingButton.requestFocus();
	}

	private void onOpenRecentDocument(RecentDocument document) {
		if (nonNull(openRecentDocumentAction)) {
			openRecentDocumentAction.execute(document);
		}
	}
}
