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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.converter.NumberStringConverter;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.screencapture.ScreenCaptureFormat;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectIntegerProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.RecordSettingsView;

import java.util.List;

@FxmlView(name = "recording-settings", presenter = org.lecturestudio.presenter.api.presenter.RecordSettingsPresenter.class)
public class FxRecordSettingsView extends GridPane implements RecordSettingsView {

	@FXML
	private CheckBox notifyToRecordCheckBox;

	@FXML
	private CheckBox confirmStopRecordingCheckBox;

	@FXML
	private TextField pageTimeoutTextField;

	@FXML
	private ComboBox<AudioFormat> recordingFormatCombo;

	@FXML
	private TextField recordingPathTextField;

	@FXML
	private Button selectRecPathButton;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxRecordSettingsView() {
		super();
	}

	@Override
	public void setNotifyToRecord(BooleanProperty notify) {
		notifyToRecordCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(notify));
	}

	@Override
	public void setConfirmStopRecording(BooleanProperty confirm) {
		confirmStopRecordingCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(confirm));
	}

	@Override
	public void setPageRecordingTimeout(IntegerProperty timeout) {
		pageTimeoutTextField.textProperty().bindBidirectional(new LectIntegerProperty(timeout), new NumberStringConverter("#"));
	}

	@Override
	public void setRecordingAudioFormat(ObjectProperty<AudioFormat> audioFormat) {
		recordingFormatCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(audioFormat));
	}

	@Override
	public void setRecordingAudioFormats(List<AudioFormat> formats) {
		FxUtils.invoke(() -> {
			recordingFormatCombo.getItems().addAll(formats);
		});
	}

	@Override
	public void setRecordingPath(StringProperty path) {
		recordingPathTextField.textProperty().bindBidirectional(new LectStringProperty(path));
	}

	@Override
	public void setOnSelectRecordingPath(Action action) {
		FxUtils.bindAction(selectRecPathButton, action);
	}

	@Override
	public void setRecordingScreenCaptureFormat(ObjectProperty<ScreenCaptureFormat> screenCaptureFormat) {

	}

	@Override
	public void setRecordingScreenCaptureFormats(List<ScreenCaptureFormat> formats) {

	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

}
