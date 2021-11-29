/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;
import org.lecturestudio.web.api.stream.model.Course;

public interface StartStreamView extends View {

	void setCourse(ObjectProperty<Course> course);

	void setCourses(List<Course> courses);

	void setAudioCaptureDevice(StringProperty captureDeviceName);

	void setAudioCaptureDevices(AudioDevice[] captureDevices);

	void setAudioPlaybackDevice(StringProperty playbackDeviceName);

	void setAudioPlaybackDevices(AudioDevice[] playbackDevices);

	void setAudioTestCaptureEnabled(BooleanProperty enable);

	void setAudioTestPlaybackEnabled(BooleanProperty enable);

	void setOnAudioTestCapture(BooleanProperty recordProperty);

	void setOnAudioTestCapturePlayback(BooleanProperty playProperty);

	void setCameraName(StringProperty cameraName);

	void setCameraNames(String[] cameraNames);

	void setEnableMicrophone(BooleanProperty enable);

	void setEnableCamera(BooleanProperty enable);

	void setEnableMessenger(BooleanProperty enable);

	void setCamera(Camera camera);

	void setCameraFormat(CameraFormat cameraFormat);

	void setCameraStatus(String statusMessage);

	void startCameraPreview();

	void stopCameraPreview();

	void setError(String message);

	void setOnSettings(Action action);

	void setOnClose(Action action);

	void setOnStart(Action action);

}
