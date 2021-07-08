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
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.converter.IntegerStringConverter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.StreamSettingsView;
import org.lecturestudio.presenter.swing.view.model.BroadcastProfileTableModel;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.components.IPTextField;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.stream.model.Course;

@SwingView(name = "stream-settings", presenter = org.lecturestudio.presenter.api.presenter.StreamSettingsPresenter.class)
public class SwingStreamSettingsView extends JPanel implements StreamSettingsView {

	private final ResourceBundle resourceBundle;

	private JTextField accessTokenTextField;

	private JComboBox<Course> courseCombo;

	private JComboBox<String> streamAudioCodecCombo;

	private JComboBox<AudioFormat> streamAudioFormatCombo;

	private JTextField cameraBitrateTextField;

	private JTextField broadcastNameTextField;

	private IPTextField broadcastAddressTextField;

	private JTextField broadcastPortTextField;

	private JTextField broadcastTlsPortTextField;

	private JLabel selectedProfileLabel;

	private JTable profileTable;

	private JButton addProfileButton;

	private JButton closeButton;

	private JButton resetButton;

	private ConsumerAction<BroadcastProfile> deleteProfileAction;

	public javax.swing.Action deleteAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			BroadcastProfileTableModel model = (BroadcastProfileTableModel) profileTable.getModel();
			BroadcastProfile profile = model.getItem(row);

			executeAction(deleteProfileAction, profile);
		}
	};


	@Inject
	SwingStreamSettingsView(ResourceBundle resourceBundle) {
		super();

		this.resourceBundle = resourceBundle;
	}

	@Override
	public void setAccessToken(StringProperty accessToken) {
		SwingUtils.bindBidirectional(accessTokenTextField, accessToken);
	}

	@Override
	public void setCourse(ObjectProperty<Course> course) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(courseCombo, course);
		});
	}

	@Override
	public void setCourses(List<Course> courses) {
		SwingUtils.invoke(() -> courseCombo
				.setModel(new DefaultComboBoxModel<>(new Vector<>(courses))));
	}

	@Override
	public void setStreamAudioFormat(ObjectProperty<AudioFormat> audioFormat) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(streamAudioFormatCombo, audioFormat);
		});
	}

	@Override
	public void setStreamAudioFormats(List<AudioFormat> formats) {
		SwingUtils.invoke(() -> streamAudioFormatCombo
				.setModel(new DefaultComboBoxModel<>(new Vector<>(formats))));
	}

	@Override
	public void setStreamAudioCodecName(StringProperty audioCodecName) {
		SwingUtils.bindBidirectional(streamAudioCodecCombo, audioCodecName);
	}

	@Override
	public void setStreamAudioCodecNames(String[] codecNames) {
		SwingUtils.invoke(() -> streamAudioCodecCombo
				.setModel(new DefaultComboBoxModel<>(codecNames)));
	}

	@Override
	public void setStreamCameraBitrate(IntegerProperty bitrate) {
		SwingUtils.bindBidirectional(cameraBitrateTextField,
				new ConvertibleObjectProperty<>(bitrate,
						new IntegerStringConverter("#")));
	}

	@Override
	public void setBroadcastProfile(ObjectProperty<BroadcastProfile> profile) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(profileTable, profile);
		});

		profile.addListener((observable, oldValue, newValue) -> {
			SwingUtils.invoke(() -> {
				if (isNull(newValue)) {
					return;
				}

				String message = resourceBundle.getString("stream.settings.broadcast.profile.selected");
				selectedProfileLabel.setText(MessageFormat.format(message,
						String.format("%s - %s : %d : %d", newValue.getName(),
								newValue.getBroadcastAddress(),
								newValue.getBroadcastPort(),
								newValue.getBroadcastTlsPort())));
			});
		});
	}

	@Override
	public void setBroadcastProfiles(List<BroadcastProfile> profiles) {
		SwingUtils.invoke(() -> {
			BroadcastProfileTableModel model = (BroadcastProfileTableModel) profileTable.getModel();
			model.setItems(profiles);
		});
	}

	@Override
	public void setOnAddBroadcastProfile(Action action) {
		SwingUtils.bindAction(addProfileButton, action);
	}

	@Override
	public void setOnDeleteBroadcastProfile(ConsumerAction<BroadcastProfile> action) {
		this.deleteProfileAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		profileTable.setModel(new BroadcastProfileTableModel(profileTable.getColumnModel()));
	}
}
