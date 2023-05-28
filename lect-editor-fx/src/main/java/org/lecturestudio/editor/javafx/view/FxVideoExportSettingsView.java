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

import java.text.DecimalFormat;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.presenter.VideoExportSettingsPresenter;
import org.lecturestudio.editor.api.view.VideoExportSettingsView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectIntegerProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.media.video.VideoFormat;

@FxmlView(name = "video-export-settings", presenter = VideoExportSettingsPresenter.class)
public class FxVideoExportSettingsView extends ContentPane implements VideoExportSettingsView {

	@FXML
	private ComboBox<Dimension2D> dimensionsCombo;

	@FXML
	private ComboBox<VideoFormat> videoFormatCombo;

	@FXML
	private ComboBox<AudioFormat> sampleRateCombo;

	@FXML
	private ComboBox<Integer> frameRateCombo;

	@FXML
	private ComboBox<Integer> audioBitrateCombo;

	@FXML
	private TextField videoBitrateField;

	@FXML
	private CheckBox audioVbrCheckbox;

	@FXML
	private Button createButton;


	public FxVideoExportSettingsView() {
		super();
	}

	@Override
	public void setDimensions(Dimension2D[] dimensions) {
		FxUtils.invoke(() -> dimensionsCombo.getItems().setAll(dimensions));
	}

	@Override
	public void setDimension(Dimension2D dimension) {
		FxUtils.invoke(() -> dimensionsCombo.getSelectionModel().select(dimension));
	}

	@Override
	public void bindDimension(ObjectProperty<Dimension2D> property) {
		dimensionsCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(property));
	}

	@Override
	public void setVideoFormats(VideoFormat[] formats) {
		FxUtils.invoke(() -> videoFormatCombo.getItems().setAll(formats));
	}

	@Override
	public void bindVideoFormat(ObjectProperty<VideoFormat> property) {
		videoFormatCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(property));
	}

	@Override
	public void setFrameRates(Integer[] rates) {
		FxUtils.invoke(() -> frameRateCombo.getItems().setAll(rates));
	}

	@Override
	public void bindFrameRate(IntegerProperty property) {
		FxUtils.invoke(() -> {
			frameRateCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(property));
		});
	}

	@Override
	public void bindVideoBitrate(IntegerProperty property) {
		videoBitrateField.textProperty().bindBidirectional(
				new LectIntegerProperty(property), new DecimalFormat());
	}

	@Override
	public void bindTwoPassEncoding(BooleanProperty enable) {
//		twoPassCheckbox.selectedProperty().bindBidirectional(new LectBooleanProperty(enable));
	}

	@Override
	public void setAudioFormats(AudioFormat[] formats) {
		FxUtils.invoke(() -> sampleRateCombo.getItems().setAll(formats));
	}

	@Override
	public void bindAudioFormat(ObjectProperty<AudioFormat> property) {
		sampleRateCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(property));
	}

	@Override
	public void setAudioBitrates(Integer[] rates) {
		FxUtils.invoke(() -> audioBitrateCombo.getItems().setAll(rates));
	}

	@Override
	public void bindAudioBitrate(IntegerProperty property) {
		audioBitrateCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(property));
	}

	@Override
	public void bindAudioVBR(BooleanProperty property) {
		audioVbrCheckbox.selectedProperty().bindBidirectional(new LectBooleanProperty(property));
	}

	@Override
	public void setOnCreate(Action action) {
		FxUtils.bindAction(createButton, action);
	}
}
