package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.codec.VideoCodecConfiguration;

public class CameraRecordingConfiguration {
	private final StringProperty cameraName = new StringProperty();
	private final ObjectProperty<CameraFormat> cameraFormat = new ObjectProperty<>();
	private final BooleanProperty enableCamera = new BooleanProperty();
	private final VideoCodecConfiguration cameraCodecConfig = new VideoCodecConfiguration();

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

	public BooleanProperty enableCameraProperty() {
		return enableCamera;
	}

	public boolean isCameraEnabled() {
		return enableCamera.get();
	}

	/**
	 * @return the camCodecConfig
	 */
	public VideoCodecConfiguration getCameraCodecConfig() {
		return cameraCodecConfig;
	}
}
