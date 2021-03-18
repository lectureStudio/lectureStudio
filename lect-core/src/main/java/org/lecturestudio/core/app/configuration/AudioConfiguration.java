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

package org.lecturestudio.core.app.configuration;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.FloatProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.util.ObservableHashMap;
import org.lecturestudio.core.util.ObservableMap;

/**
 * The AudioConfiguration specifies audio related properties for the
 * application.
 *
 * @author Alex Andres
 */
public class AudioConfiguration {

	/** The capture device name. */
	private final StringProperty inputDeviceName = new StringProperty();

	/** The playback device name. */
	private final StringProperty outputDeviceName = new StringProperty();

	/** The sound system name. */
	private final StringProperty soundSystem = new StringProperty();

	/** The path where the recordings are stored at. */
	private final StringProperty recordingPath = new StringProperty();

	/** The capture device recording volume. */
	private final FloatProperty recordingVolume = new FloatProperty();

	/** The capture device master recording volume. */
	private final FloatProperty recordingMasterVolume = new FloatProperty();

	/** The playback device volume. */
	private final DoubleProperty playbackVolume = new DoubleProperty();

	/** The recording volumes of all used capture devices. */
	private final ObservableHashMap<String, Double> recordingVolumes = new ObservableHashMap<>();

	/** The audio format of the recording. */
	private final ObjectProperty<AudioFormat> recordingFormat = new ObjectProperty<>();


	/**
	 * Obtain the capture device name.
	 *
	 * @return the capture device name.
	 */
	public String getInputDeviceName() {
		return inputDeviceName.get();
	}

	/**
	 * Set the capture device name.
	 *
	 * @param deviceName the capture device name to set.
	 */
	public void setInputDeviceName(String deviceName) {
		this.inputDeviceName.set(deviceName);
	}

	/**
	 * Obtain the capture device name property.
	 *
	 * @return the capture device name property.
	 */
	public StringProperty inputDeviceNameProperty() {
		return inputDeviceName;
	}

	/**
	 * Obtain the playback device name.
	 *
	 * @return the playback device name.
	 */
	public String getOutputDeviceName() {
		return outputDeviceName.get();
	}

	/**
	 * Set the playback device name.
	 *
	 * @param deviceName the playback device name to set.
	 */
	public void setOutputDeviceName(String deviceName) {
		this.outputDeviceName.set(deviceName);
	}

	/**
	 * Obtain the playback device name property.
	 *
	 * @return the playback device name property.
	 */
	public StringProperty outputDeviceNameProperty() {
		return outputDeviceName;
	}

	/**
	 * Obtain the sound system name.
	 *
	 * @return the sound system name.
	 */
	public String getSoundSystem() {
		return soundSystem.get();
	}

	/**
	 * Set the sound system name.
	 *
	 * @param soundSystem sound system name to set.
	 */
	public void setSoundSystem(String soundSystem) {
		this.soundSystem.set(soundSystem);
	}

	/**
	 * Obtain the sound system property.
	 *
	 * @return the sound system property.
	 */
	public StringProperty soundSystemProperty() {
		return soundSystem;
	}

	/**
	 * Obtain the recording path.
	 *
	 * @return the recording path.
	 */
	public String getRecordingPath() {
		return recordingPath.get();
	}

	/**
	 * Set the new path where to store the recordings.
	 *
	 * @param recordingPath the recording path to set.
	 */
	public void setRecordingPath(String recordingPath) {
		this.recordingPath.set(recordingPath);
	}

	/**
	 * Obtain the recording path property.
	 *
	 * @return the recording path property.
	 */
	public StringProperty recordingPathProperty() {
		return recordingPath;
	}

	/**
	 * Obtain the capture device recording volume in the range of [0, 1].
	 *
	 * @return the capture device recording volume.
	 */
	public float getDefaultRecordingVolume() {
		return recordingVolume.get();
	}

	/**
	 * Set the capture device recording volume. The volume value must be in the
	 * range of [0, 1].
	 *
	 * @param volume the recording volume to set.
	 */
	public void setDefaultRecordingVolume(float volume) {
		this.recordingVolume.set(volume);
	}

	/**
	 * Obtain the recording volume property.
	 *
	 * @return the recording volume property.
	 */
	public FloatProperty recordingVolumeProperty() {
		return recordingVolume;
	}

	/**
	 * Obtain the capture device recording volume in the range of [0, 1].
	 *
	 * @return the capture device recording volume.
	 */
	public float getMasterRecordingVolume() {
		return recordingMasterVolume.get();
	}

	/**
	 * Set the capture device recording volume. The volume value must be in the
	 * range of [0, 1].
	 *
	 * @param volume the recording volume to set.
	 */
	public void setMasterRecordingVolume(float volume) {
		this.recordingMasterVolume.set(volume);
	}

	/**
	 * Obtain the master recording volume property.
	 *
	 * @return the recording volume property.
	 */
	public FloatProperty recordingMasterVolumeProperty() {
		return recordingMasterVolume;
	}

	/**
	 * Obtain the playback device volume in the range of [0, 1].
	 *
	 * @return the playback device volume.
	 */
	public double getPlaybackVolume() {
		return playbackVolume.get();
	}

	/**
	 * Set the playback device volume. The volume value must be in the
	 * range of [0, 1].
	 *
	 * @param volume the playback volume to set.
	 */
	public void setPlaybackVolume(double volume) {
		this.playbackVolume.set(volume);
	}

	/**
	 * Obtain the playback volume property.
	 *
	 * @return the playback volume property.
	 */
	public DoubleProperty playbackVolumeProperty() {
		return playbackVolume;
	}

	/**
	 * Obtain the recording volumes of all used capture devices. The key of the
	 * map refers to the capture device name and the value stores the recording
	 * volume configured for this device.
	 *
	 * @return recording volumes of all used capture devices.
	 */
	public ObservableMap<String, Double> getRecordingVolumes() {
		return recordingVolumes;
	}

	/**
	 * Set the recording volume for the specified capture device name. The
	 * volume value must be in the range of [0, 1].
	 *
	 * @param deviceName The capture device name.
	 * @param volume     The recording volume.
	 */
	public void setRecordingVolume(String deviceName, double volume) {
		recordingVolumes.put(deviceName, volume);
	}

	/**
	 * Obtain the recording volume for the specified capture device name.
	 *
	 * @param deviceName The capture device name.
	 *
	 * @return the recording volume in the range of [0, 1].
	 */
	public Double getRecordingVolume(String deviceName) {
		return recordingVolumes.get(deviceName);
	}

	/**
	 * Obtain the audio format of the recording.
	 *
	 * @return the audio format of the recording.
	 */
	public AudioFormat getRecordingFormat() {
		return recordingFormat.get();
	}

	/**
	 * Set the audio format of the recording.
	 *
	 * @param format The new audio format to set.
	 */
	public void setRecordingFormat(AudioFormat format) {
		this.recordingFormat.set(format);
	}

	/**
	 * Obtain the audio format property.
	 *
	 * @return the audio format property.
	 */
	public ObjectProperty<AudioFormat> recordingFormatProperty() {
		return recordingFormat;
	}

}
