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
import org.lecturestudio.presenter.api.model.ParticipantVideoLayout;
import org.lecturestudio.presenter.api.net.ScreenShareProfile;

/**
 * Configuration class for stream settings in the presenter application.
 * Contains properties for audio/video streaming, camera settings, and messaging functionality.
 *
 * @author Alex Andres
 */
public class StreamConfiguration {

	/** Property indicating whether the stream should be recorded. */
	private final BooleanProperty recordStream = new BooleanProperty();

	/** Property for server name to connect to. */
	private final StringProperty serverName = new StringProperty();

	/** Property for access token for authentication. */
	private final StringProperty accessToken = new StringProperty();

	/** Property for audio format configuration. */
	private final ObjectProperty<AudioFormat> audioFormat = new ObjectProperty<>();

	/** Property for audio codec name. */
	private final StringProperty audioCodec = new StringProperty();

	/** Property for camera device name. */
	private final StringProperty cameraName = new StringProperty();

	/** Property for camera format configuration. */
	private final ObjectProperty<CameraFormat> cameraFormat = new ObjectProperty<>();

	/** Configuration for camera video codec. */
	private final VideoCodecConfiguration cameraCodecConfig = new VideoCodecConfiguration();

	/** Property for screen sharing profile. */
	private final ObjectProperty<ScreenShareProfile> screenProfile = new ObjectProperty<>();

	/** Configuration for screen sharing video codec. */
	private final VideoCodecConfiguration screenCodecConfig = new VideoCodecConfiguration();

	/** Property indicating whether microphone is enabled. */
	private final BooleanProperty enableMicrophone = new BooleanProperty();

	/** Property indicating whether the camera is enabled. */
	private final BooleanProperty enableCamera = new BooleanProperty();

	/** Property indicating whether messenger is enabled. */
	private final BooleanProperty enableMessenger = new BooleanProperty();

	/** Property for participant video layout configuration. */
	private final ObjectProperty<ParticipantVideoLayout> participantVideoLayoutProperty = new ObjectProperty<>();


	/**
	 * Gets whether stream recording is enabled.
	 *
	 * @return true if stream recording is enabled, false otherwise.
	 */
	public boolean getRecordStream() {
		return recordStream.get();
	}

	/**
	 * Sets whether stream recording is enabled.
	 *
	 * @param record true to enable stream recording, false to disable.
	 */
	public void setRecordStream(boolean record) {
		this.recordStream.set(record);
	}

	/**
	 * Gets the property for stream recording.
	 *
	 * @return the boolean property for the stream recording.
	 */
	public BooleanProperty recordStreamProperty() {
		return recordStream;
	}

	/**
	 * Gets the server name.
	 *
	 * @return the server name.
	 */
	public String getServerName() {
		return serverName.get();
	}

	/**
	 * Sets the server name.
	 *
	 * @param name the server name to set.
	 */
	public void setServerName(String name) {
		this.serverName.set(name);
	}

	/**
	 * Gets the property for server name.
	 *
	 * @return the string property for server name.
	 */
	public StringProperty serverNameProperty() {
		return serverName;
	}

	/**
	 * Gets the access token.
	 *
	 * @return the access token.
	 */
	public String getAccessToken() {
		return accessToken.get();
	}

	/**
	 * Sets the access token.
	 *
	 * @param token the access token to set.
	 */
	public void setAccessToken(String token) {
		this.accessToken.set(token);
	}

	/**
	 * Gets the property for access token.
	 *
	 * @return the string property for the access token.
	 */
	public StringProperty accessTokenProperty() {
		return accessToken;
	}

	/**
	 * Gets the audio format.
	 *
	 * @return the audio format.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat.get();
	}

	/**
	 * Sets the audio format.
	 *
	 * @param audioFormat the audio format to set.
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat.set(audioFormat);
	}
	
	/**
	 * Gets the property for audio format.
	 *
	 * @return the object property for audio format.
	 */
	public ObjectProperty<AudioFormat> audioFormatProperty() {
		return audioFormat;
	}
	
	/**
	 * Gets the audio codec.
	 *
	 * @return the audio codec name.
	 */
	public String getAudioCodec() {
		return audioCodec.get();
	}
	
	/**
	 * Sets the audio codec.
	 *
	 * @param audioCodec the audio codec name to set.
	 */
	public void setAudioCodec(String audioCodec) {
		this.audioCodec.set(audioCodec);
	}
	
	/**
	 * Gets the property for audio codec.
	 *
	 * @return the string property for the audio codec.
	 */
	public StringProperty audioCodecProperty() {
		return audioCodec;
	}
	
	/**
	 * Gets the camera name.
	 *
	 * @return the camera name.
	 */
	public String getCameraName() {
		return cameraName.get();
	}
	
	/**
	 * Sets the camera name.
	 *
	 * @param cameraName the camera name to set.
	 */
	public void setCameraName(String cameraName) {
		this.cameraName.set(cameraName);
	}
	
	/**
	 * Gets the property for camera name.
	 *
	 * @return the string property for camera name.
	 */
	public StringProperty cameraNameProperty() {
		return cameraName;
	}
	
	/**
	 * Gets the camera format.
	 *
	 * @return the camera format.
	 */
	public CameraFormat getCameraFormat() {
		return cameraFormat.get();
	}
	
	/**
	 * Sets the camera format.
	 *
	 * @param format the camera format to set.
	 */
	public void setCameraFormat(CameraFormat format) {
		this.cameraFormat.set(format);
	}
	
	/**
	 * Gets the property for camera format.
	 *
	 * @return the object property for the camera format.
	 */
	public ObjectProperty<CameraFormat> cameraFormatProperty() {
		return cameraFormat;
	}

	/**
	 * Gets the camera codec configuration.
	 *
	 * @return the camera codec configuration.
	 */
	public VideoCodecConfiguration getCameraCodecConfig() {
		return cameraCodecConfig;
	}

	/**
	 * Gets the screen share profile.
	 *
	 * @return the screen share profile.
	 */
	public ScreenShareProfile getScreenShareProfile() {
		return screenProfile.get();
	}

	/**
	 * Sets the screen share profile.
	 *
	 * @param profile the screen share profile to set.
	 */
	public void setScreenShareProfile(ScreenShareProfile profile) {
		this.screenProfile.set(profile);
	}

	/**
	 * Gets the property for screen share profile.
	 *
	 * @return the object property for screen share profile.
	 */
	public ObjectProperty<ScreenShareProfile> screenProfileProperty() {
		return screenProfile;
	}

	/**
	 * Gets the screen codec configuration.
	 *
	 * @return the screen codec configuration.
	 */
	public VideoCodecConfiguration getScreenCodecConfig() {
		return screenCodecConfig;
	}

	/**
	 * Gets the property for microphone enable state.
	 *
	 * @return the boolean property for the microphone enable state.
	 */
	public BooleanProperty enableMicrophoneProperty() {
		return enableMicrophone;
	}

	/**
	 * Gets whether microphone is enabled.
	 *
	 * @return true if microphone is enabled, false otherwise.
	 */
	public boolean getMicrophoneEnabled() {
		return enableMicrophone.get();
	}

	/**
	 * Sets whether microphone is enabled.
	 *
	 * @param enabled true to enable microphone, false to disable.
	 */
	public void setMicrophoneEnabled(boolean enabled) {
		enableMicrophone.set(enabled);
	}

	/**
	 * Gets the property for camera enable state.
	 *
	 * @return the boolean property for the camera enable state.
	 */
	public BooleanProperty enableCameraProperty() {
		return enableCamera;
	}

	/**
	 * Gets whether camera is enabled.
	 *
	 * @return true if camera is enabled, false otherwise.
	 */
	public boolean getCameraEnabled() {
		return enableCamera.get();
	}

	/**
	 * Sets whether camera is enabled.
	 *
	 * @param enabled true to enable camera, false to disable.
	 */
	public void setCameraEnabled(boolean enabled) {
		enableCamera.set(enabled);
	}

	/**
	 * Gets whether messenger is enabled.
	 *
	 * @return true if messenger is enabled, false otherwise.
	 */
	public boolean getMessengerEnabled() {
		return enableMessenger.get();
	}

	/**
	 * Sets whether messenger is enabled.
	 *
	 * @param enabled true to enable messenger, false to disable.
	 */
	public void setMessengerEnabled(boolean enabled) {
		enableMessenger.set(enabled);
	}

	/**
	 * Gets the property for messenger enable state.
	 *
	 * @return the boolean property for the messenger enable state.
	 */
	public BooleanProperty enableMessengerProperty() {
		return enableMessenger;
	}

	/**
	 * Gets the property for participant video layout.
	 *
	 * @return the object property for the participant video layout.
	 */
	public ObjectProperty<ParticipantVideoLayout> participantVideoLayoutProperty() {
		return participantVideoLayoutProperty;
	}

	/**
	 * Gets the participant video layout.
	 *
	 * @return the participant video layout.
	 */
	public ParticipantVideoLayout getParticipantVideoLayout() {
		return participantVideoLayoutProperty.get();
	}

	/**
	 * Sets the participant video layout.
	 *
	 * @param layout the participant video layout to set.
	 */
	public void setParticipantVideoLayout(ParticipantVideoLayout layout) {
		participantVideoLayoutProperty.set(layout);
	}
}
