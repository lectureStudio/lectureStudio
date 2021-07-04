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

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.web.api.stream.model.Lecture;

import java.util.List;

public interface StreamSettingsView extends SettingsBaseView {

	void setAccessToken(StringProperty accessToken);

	void setLecture(ObjectProperty<Lecture> lecture);

	void setLectures(List<Lecture> lectures);

	void setStreamAudioFormat(ObjectProperty<AudioFormat> audioFormat);

	void setStreamAudioFormats(List<AudioFormat> formats);

	void setStreamAudioCodecName(StringProperty audioCodecName);

	void setStreamAudioCodecNames(String[] codecNames);

	void setStreamCameraBitrate(IntegerProperty bitrate);

	void setBroadcastProfile(ObjectProperty<BroadcastProfile> profile);

	void setBroadcastProfiles(List<BroadcastProfile> profiles);

	void setOnAddBroadcastProfile(Action action);

	void setOnDeleteBroadcastProfile(ConsumerAction<BroadcastProfile> action);

}
