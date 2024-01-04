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

import java.awt.Color;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.converter.RegexConverter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.net.ScreenShareProfile;
import org.lecturestudio.presenter.api.view.StreamSettingsView;
import org.lecturestudio.presenter.swing.combobox.ScreenShareProfileRenderer;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "stream-settings", presenter = org.lecturestudio.presenter.api.presenter.StreamSettingsPresenter.class)
public class SwingStreamSettingsView extends JPanel implements StreamSettingsView {

	private final ResourceBundle resources;

	private JCheckBox recordStreamCheckBox;

	private JTextField serverNameTextField;

	private JTextField accessTokenTextField;

	private JComboBox<ScreenShareProfile> screenProfileCombo;

	private JButton updateCoursesButton;

	private JButton closeButton;

	private JButton resetButton;


	@Inject
	SwingStreamSettingsView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void setRecordStream(BooleanProperty record) {
		SwingUtils.bindBidirectional(recordStreamCheckBox, record);
	}

	@Override
	public void setAccessToken(StringProperty accessToken) {
		SwingUtils.bindBidirectional(accessTokenTextField, accessToken);
	}

	@Override
	public void setAccessTokenValid(boolean valid) {
		if (valid) {
			accessTokenTextField.setBackground(Color.decode("#D1FAE5"));
		}
		else {
			accessTokenTextField.setBackground(Color.decode("#FEE2E2"));
		}
	}

	@Override
	public void setServerName(StringProperty serverName) {
		SwingUtils.bindBidirectional(serverNameTextField,
				new ConvertibleObjectProperty<>(serverName,
						new RegexConverter("^(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/?\\n]+)")));
	}

	@Override
	public void setOnCheckAccessToken(Action action) {
		SwingUtils.bindAction(updateCoursesButton, action);
	}

	@Override
	public void setScreenShareProfile(ObjectProperty<ScreenShareProfile> profile) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(screenProfileCombo, profile);
		});
	}

	@Override
	public void setScreenShareProfiles(ScreenShareProfile[] profiles) {
		SwingUtils.invoke(() -> screenProfileCombo
				.setModel(new DefaultComboBoxModel<>(profiles)));
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
		screenProfileCombo.setRenderer(new ScreenShareProfileRenderer(resources,
				"stream.settings.screen.share.profile."));
	}
}
