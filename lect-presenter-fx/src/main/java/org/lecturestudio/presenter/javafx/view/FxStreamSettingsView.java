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

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.converter.NumberStringConverter;

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.beans.LectIntegerProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.StreamSettingsView;
import org.lecturestudio.web.api.stream.model.Course;

@FxmlView(name = "stream-settings", presenter = org.lecturestudio.presenter.api.presenter.StreamSettingsPresenter.class)
public class FxStreamSettingsView extends GridPane implements StreamSettingsView {

	private ConsumerAction<BroadcastProfile> deleteProfileAction;

	@FXML
	private TextField accessTokenTextField;

	@FXML
	private ComboBox<BroadcastProfile> bcastProfilesCombo;

	@FXML
	private ComboBox<String> streamAudioCodecCombo;

	@FXML
	private ComboBox<AudioFormat> streamAudioFormatCombo;

	@FXML
	private TextField cameraBitrateTextField;

	@FXML
	private TextField broadcastAddressTextField;

	@FXML
	private TextField broadcastPortTextField;

	@FXML
	private TextField broadcastTlsPortTextField;

	@FXML
	private Button addProfileButton;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxStreamSettingsView() {
		super();
	}

	@Override
	public void setAccessToken(StringProperty accessToken) {
		accessTokenTextField.textProperty().bindBidirectional(new LectStringProperty(accessToken));
	}

	@Override
	public void setCourse(ObjectProperty<Course> course) {

	}

	@Override
	public void setCourses(List<Course> courses) {

	}

	@Override
	public void setStreamAudioFormat(ObjectProperty<AudioFormat> audioFormat) {
		streamAudioFormatCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(audioFormat));
	}

	@Override
	public void setStreamAudioFormats(List<AudioFormat> formats) {
		FxUtils.invoke(() -> {
			streamAudioFormatCombo.getItems().setAll(formats);
		});
	}

	@Override
	public void setStreamAudioCodecName(StringProperty audioCodecName) {
		streamAudioCodecCombo.valueProperty().bindBidirectional(new LectStringProperty(audioCodecName));
	}

	@Override
	public void setStreamAudioCodecNames(String[] codecNames) {
		FxUtils.invoke(() -> {
			streamAudioCodecCombo.getItems().addAll(codecNames);
		});
	}

	@Override
	public void setStreamCameraBitrate(IntegerProperty bitrate) {
		cameraBitrateTextField.textProperty().bindBidirectional(new LectIntegerProperty(bitrate), new NumberStringConverter("#"));
	}

	@Override
	public void setBroadcastProfile(ObjectProperty<BroadcastProfile> profile) {
		bcastProfilesCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(profile));
	}

	@Override
	public void setBroadcastProfiles(List<BroadcastProfile> profiles) {
		FxUtils.invoke(() -> {
			bcastProfilesCombo.getItems().setAll(profiles);
		});
	}

	@Override
	public void setOnAddBroadcastProfile(Action action) {
		FxUtils.bindAction(addProfileButton, action);
	}

	@Override
	public void setOnDeleteBroadcastProfile(ConsumerAction<BroadcastProfile> action) {
		this.deleteProfileAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@FXML
	private void initialize() {
		broadcastTlsPortTextField.prefWidthProperty().bind(broadcastPortTextField.widthProperty());
	}

}
