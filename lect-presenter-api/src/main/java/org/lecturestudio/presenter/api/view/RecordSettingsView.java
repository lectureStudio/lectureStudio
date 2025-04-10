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

package org.lecturestudio.presenter.api.view;

import java.util.List;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;

/**
 * Interface representing the view for recording settings in the presenter application.
 * Provides methods to configure various recording-related options and behaviors.
 *
 * @author Alex Andres
 */
public interface RecordSettingsView extends SettingsBaseView {

	/**
	 * Sets whether recording should start automatically.
	 *
	 * @param autostart The property containing the autostart recording setting.
	 */
	void setAutostartRecording(BooleanProperty autostart);

	/**
	 * Sets whether to notify the user to record.
	 *
	 * @param notify The property containing the notification setting.
	 */
	void setNotifyToRecord(BooleanProperty notify);

	/**
	 * Sets whether to confirm before stopping a recording.
	 *
	 * @param confirm The property containing the confirmation setting.
	 */
	void setConfirmStopRecording(BooleanProperty confirm);

	/**
	 * Sets whether to mix audio streams during recording.
	 *
	 * @param mix The property containing the audio stream mixing setting.
	 */
	void setMixAudioStreams(BooleanProperty mix);

	/**
	 * Sets the timeout duration for page recording.
	 *
	 * @param timeout The property containing the page recording timeout value.
	 */
	void setPageRecordingTimeout(IntegerProperty timeout);

	/**
	 * Sets the audio format to use for recording.
	 *
	 * @param audioFormat The property containing the recording audio format.
	 */
	void setRecordingAudioFormat(ObjectProperty<AudioFormat> audioFormat);

	/**
	 * Sets the list of available audio formats for recording.
	 *
	 * @param formats The list of available audio formats.
	 */
	void setRecordingAudioFormats(List<AudioFormat> formats);

	/**
	 * Sets the directory path where recordings will be saved.
	 *
	 * @param path The property containing the recording path.
	 */
	void setRecordingPath(StringProperty path);

	/**
	 * Sets the action to perform when selecting a recording path.
	 *
	 * @param action The action to execute when selecting a recording path.
	 */
	void setOnSelectRecordingPath(Action action);

}
