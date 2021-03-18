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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.model.ZoomConstraints;
import org.lecturestudio.editor.api.presenter.MediaTrackControlsPresenter;
import org.lecturestudio.media.search.SearchState;
import org.lecturestudio.editor.api.view.MediaTrackControlsView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.control.TextSearchField;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "media-track-controls", presenter = MediaTrackControlsPresenter.class)
public class FxMediaTrackControlsView extends HBox implements MediaTrackControlsView {

	@FXML
	private ResourceBundle resources;

	@FXML
	private Button undoButton;

	@FXML
	private Button redoButton;

	@FXML
	private Button cutButton;

	@FXML
	private Button deletePageButton;

	@FXML
	private Button importRecordingButton;

	@FXML
	private Button zoomInButton;

	@FXML
	private Button zoomOutButton;

	@FXML
	private Slider zoomSlider;

	@FXML
	private Slider adjustVolumeSlider;

	@FXML
	private Label searchStateLabel;

	@FXML
	private Button searchPrevButton;

	@FXML
	private Button searchNextButton;

	@FXML
	private TextSearchField<String> searchField;


	public FxMediaTrackControlsView() {
		super();
	}

	@Override
	public void bindCanCut(BooleanProperty property) {
		BooleanBinding lectProperty = new LectBooleanProperty(property).not();

		cutButton.disableProperty().bind(lectProperty);
		adjustVolumeSlider.disableProperty().bind(lectProperty);
	}

	@Override
	public void bindCanDeletePage(BooleanProperty property) {
		deletePageButton.disableProperty().bind(new LectBooleanProperty(property).not());
	}

	@Override
	public void bindCanUndo(BooleanProperty property) {
		undoButton.disableProperty().bind(new LectBooleanProperty(property).not());
	}

	@Override
	public void bindCanRedo(BooleanProperty property) {
		redoButton.disableProperty().bind(new LectBooleanProperty(property).not());
	}

	@Override
	public void bindZoomLevel(ZoomConstraints constraints, DoubleProperty property) {
		zoomSlider.setMin(constraints.getMinZoom());
		zoomSlider.setMax(constraints.getMaxZoom());
		zoomSlider.valueProperty().bindBidirectional(new LectDoubleProperty(property));

		updateZoomButtons(constraints, property.get());

		property.addListener((o, oldValue, newValue) -> {
			updateZoomButtons(constraints, newValue);
		});
	}

	@Override
	public void setOnUndo(Action action) {
		FxUtils.bindAction(undoButton, action);
	}

	@Override
	public void setOnRedo(Action action) {
		FxUtils.bindAction(redoButton, action);
	}

	@Override
	public void setOnCut(Action action) {
		FxUtils.bindAction(cutButton, action);
	}

	@Override
	public void setOnDeletePage(Action action) {
		FxUtils.bindAction(deletePageButton, action);
	}

	@Override
	public void setOnImportRecording(Action action) {
		FxUtils.bindAction(importRecordingButton, action);
	}

	@Override
	public void setOnZoomIn(Action action) {
		FxUtils.bindAction(zoomInButton, action);
	}

	@Override
	public void setOnZoomOut(Action action) {
		FxUtils.bindAction(zoomOutButton, action);
	}

	@Override
	public void setOnAdjustAudio(ConsumerAction<Double> action) {
		adjustVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			action.execute(newValue.doubleValue());
		});
	}

	@Override
	public void setOnSearch(ConsumerAction<String> action) {
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			action.execute(newValue);
		});
	}

	@Override
	public void setOnPreviousFoundPage(Action action) {
		FxUtils.bindAction(searchPrevButton, action);
	}

	@Override
	public void setOnNextFoundPage(Action action) {
		FxUtils.bindAction(searchNextButton, action);
	}

	@Override
	public void setSearchState(SearchState searchState) {
		FxUtils.invoke(() -> {
			boolean showControls = nonNull(searchState);

			if (showControls) {
				int totalHits = searchState.getTotalHits();
				int currentIndex = searchState.getSelectedIndex();

				searchStateLabel.setText(MessageFormat.format(
						resources.getString("media.search.index.of"),
						currentIndex, totalHits));
				searchPrevButton.setDisable(totalHits < 1 || currentIndex <= 1);
				searchNextButton.setDisable(totalHits < 1 || currentIndex == totalHits);

				searchField.getSuggestions().setAll(searchState.getSearchResult().getSuggestions());
			}

			searchStateLabel.setManaged(showControls);
			searchStateLabel.setVisible(showControls);
			searchPrevButton.setManaged(showControls);
			searchPrevButton.setVisible(showControls);
			searchNextButton.setManaged(showControls);
			searchNextButton.setVisible(showControls);
		});
	}

	private void updateZoomButtons(ZoomConstraints constraints, double value) {
		FxUtils.invoke(() -> {
			zoomInButton.setDisable(Double.compare(value, constraints.getMaxZoom()) == 0);
			zoomOutButton.setDisable(Double.compare(value, constraints.getMinZoom()) == 0);
		});
	}
}
