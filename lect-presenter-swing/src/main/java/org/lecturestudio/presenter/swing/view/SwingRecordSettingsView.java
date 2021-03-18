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

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.converter.IntegerStringConverter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.RecordSettingsView;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "recording-settings", presenter = org.lecturestudio.presenter.api.presenter.RecordSettingsPresenter.class)
public class SwingRecordSettingsView extends JPanel implements RecordSettingsView {

	private JCheckBox notifyToRecordCheckBox;

	private JCheckBox confirmStopRecordingCheckBox;

	private JTextField pageTimeoutTextField;

	private JComboBox<AudioFormat> recordingFormatCombo;

	private JTextField recordingPathTextField;

	private JButton selectRecPathButton;

	private JButton closeButton;

	private JButton resetButton;


	SwingRecordSettingsView() {
		super();
	}

	@Override
	public void setNotifyToRecord(BooleanProperty notify) {
		SwingUtils.bindBidirectional(notifyToRecordCheckBox, notify);
	}

	@Override
	public void setConfirmStopRecording(BooleanProperty confirm) {
		SwingUtils.bindBidirectional(confirmStopRecordingCheckBox, confirm);
	}

	@Override
	public void setPageRecordingTimeout(IntegerProperty timeout) {
		SwingUtils.bindBidirectional(pageTimeoutTextField,
				new ConvertibleObjectProperty<>(timeout,
						new IntegerStringConverter("#")));
	}

	@Override
	public void setRecordingAudioFormat(ObjectProperty<AudioFormat> audioFormat) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(recordingFormatCombo, audioFormat);
		});
	}

	@Override
	public void setRecordingAudioFormats(List<AudioFormat> formats) {
		SwingUtils.invoke(() -> recordingFormatCombo
				.setModel(new DefaultComboBoxModel<>(new Vector<>(formats))));
	}

	@Override
	public void setRecordingPath(StringProperty path) {
		SwingUtils.bindBidirectional(recordingPathTextField, path);
	}

	@Override
	public void setOnSelectRecordingPath(Action action) {
		SwingUtils.bindAction(selectRecPathButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}
}
