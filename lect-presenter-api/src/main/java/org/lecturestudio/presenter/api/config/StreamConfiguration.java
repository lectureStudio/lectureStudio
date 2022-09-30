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

package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.presenter.api.net.ScreenShareProfile;

public class StreamConfiguration {

	private final StringProperty serverName = new StringProperty();

	private final StringProperty accessToken = new StringProperty();

	private final ObjectProperty<AudioFormat> audioFormat = new ObjectProperty<>();

	private final StringProperty audioCodec = new StringProperty();

	private final StringProperty cameraName = new StringProperty();

	private final ObjectProperty<CameraFormat> cameraFormat = new ObjectProperty<>();

	private final VideoCodecConfiguration cameraCodecConfig = new VideoCodecConfiguration();

	private final ObjectProperty<ScreenShareProfile> screenProfile = new ObjectProperty<>();

	private final VideoCodecConfiguration screenCodecConfig = new VideoCodecConfiguration();

	private final BooleanProperty enableMicrophone = new BooleanProperty();

	private final BooleanProperty enableCamera = new BooleanProperty();

	private final BooleanProperty enableMessenger = new BooleanProperty();


	public String getServerName() {
		return serverName.get();
	}

	public void setServerName(String name) {
		this.serverName.set(name);
	}

	public StringProperty serverNameProperty() {
		return serverName;
	}

	public String getAccessToken() {
		return accessToken.get();
	}

	public void setAccessToken(String token) {
		this.accessToken.set(token);
	}

	public StringProperty accessTokenProperty() {
		return accessToken;
	}

	/**
	 * @return the audioFormat
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat.get();
	}

	/**
	 * @param audioFormat the audioFormat to set
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat.set(audioFormat);
	}
	
	public ObjectProperty<AudioFormat> audioFormatProperty() {
		return audioFormat;
	}
	
	/**
	 * @return the audioCodec
	 */
	public String getAudioCodec() {
		return audioCodec.get();
	}
	
	/**
	 * @param audioCodec the audioCodec to set
	 */
	public void setAudioCodec(String audioCodec) {
		this.audioCodec.set(audioCodec);
	}
	
	public StringProperty audioCodecProperty() {
		return audioCodec;
	}
	
	/**
	 * @return the cameraName
	 */
	public String getCameraName() {
		return cameraName.get();
	}
	
	/**
	 * @param cameraName the cameraName to set
	 */
	public void setCameraName(String cameraName) {
		this.cameraName.set(cameraName);
	}
	
	public StringProperty cameraNameProperty() {
		return cameraName;
	}
	
	/**
	 * @return the format
	 */
	public CameraFormat getCameraFormat() {
		return cameraFormat.get();
	}
	
	/**
	 * @param format the format to set
	 */
	public void setCameraFormat(CameraFormat format) {
		this.cameraFormat.set(format);
	}
	
	public ObjectProperty<CameraFormat> cameraFormatProperty() {
		return cameraFormat;
	}

	/**
	 * @return the camCodecConfig
	 */
	public VideoCodecConfiguration getCameraCodecConfig() {
		return cameraCodecConfig;
	}

	public ScreenShareProfile getScreenShareProfile() {
		return screenProfile.get();
	}

	public void setScreenShareProfile(ScreenShareProfile profile) {
		this.screenProfile.set(profile);
	}

	public ObjectProperty<ScreenShareProfile> screenProfileProperty() {
		return screenProfile;
	}

	public VideoCodecConfiguration getScreenCodecConfig() {
		return screenCodecConfig;
	}

	public BooleanProperty enableMicrophoneProperty() {
		return enableMicrophone;
	}

	public boolean getMicrophoneEnabled() {
		return enableMicrophone.get();
	}

	public void setMicrophoneEnabled(boolean enabled) {
		enableMicrophone.set(enabled);
	}

	public BooleanProperty enableCameraProperty() {
		return enableCamera;
	}

	public boolean getCameraEnabled() {
		return enableCamera.get();
	}

	public void setCameraEnabled(boolean enabled) {
		enableCamera.set(enabled);
	}

	public boolean getMessengerEnabled() {
		return enableMessenger.get();
	}

	public void setMessengerEnabled(boolean enabled) {
		enableMessenger.set(enabled);
	}

	public BooleanProperty enableMessengerProperty() {
		return enableMessenger;
	}
}
