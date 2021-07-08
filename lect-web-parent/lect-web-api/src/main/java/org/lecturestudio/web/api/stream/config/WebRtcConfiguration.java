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

package org.lecturestudio.web.api.stream.config;

import dev.onvoid.webrtc.RTCConfiguration;

import org.lecturestudio.web.api.stream.model.Course;

public class WebRtcConfiguration {

	private final AudioConfiguration audioConfig;

	private final VideoConfiguration videoConfig;

	private final DesktopCaptureConfiguration desktopCaptureConfig;

	private final RTCConfiguration rtcConfig;

	private Course course;


	public WebRtcConfiguration() {
		audioConfig = new AudioConfiguration();
		videoConfig = new VideoConfiguration();
		desktopCaptureConfig = new DesktopCaptureConfiguration();
		rtcConfig = new RTCConfiguration();
	}

	public AudioConfiguration getAudioConfiguration() {
		return audioConfig;
	}

	public VideoConfiguration getVideoConfiguration() {
		return videoConfig;
	}

	public DesktopCaptureConfiguration getDesktopCaptureConfiguration() {
		return desktopCaptureConfig;
	}

	public RTCConfiguration getRTCConfig() {
		return rtcConfig;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}
}
