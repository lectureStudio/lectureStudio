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

package org.lecturestudio.media.config;

import java.io.File;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;

public class RenderConfiguration {

	private final ObjectProperty<AudioRenderConfiguration> audioConfig = new ObjectProperty<>();

	private final ObjectProperty<VideoRenderConfiguration> videoConfig = new ObjectProperty<>();

	/** The output file. */
	private final ObjectProperty<File> outputFile = new ObjectProperty<>();

	/** The output file format. */
	private final StringProperty fileFormat = new StringProperty();

	private final BooleanProperty videoExport = new BooleanProperty();

	private final BooleanProperty vectorExport = new BooleanProperty();


	public RenderConfiguration() {
		setAudioConfig(new AudioRenderConfiguration());
		setVideoConfig(new VideoRenderConfiguration());
	}

	/**
	 * @return the audioConfig
	 */
	public AudioRenderConfiguration getAudioConfig() {
		return audioConfig.get();
	}

	/**
	 * @param audioConfig the audioConfig to set
	 */
	public void setAudioConfig(AudioRenderConfiguration audioConfig) {
		this.audioConfig.set(audioConfig);
	}

	/**
	 * @return the videoConfig
	 */
	public VideoRenderConfiguration getVideoConfig() {
		return videoConfig.get();
	}

	/**
	 * @param videoConfig the videoConfig to set
	 */
	public void setVideoConfig(VideoRenderConfiguration videoConfig) {
		this.videoConfig.set(videoConfig);
	}

	/**
	 * @return the fileFormat
	 */
	public String getFileFormat() {
		return fileFormat.get();
	}

	/**
	 * @param fileFormat the fileFormat to set
	 */
	public void setFileFormat(String fileFormat) {
		this.fileFormat.set(fileFormat);
	}

	public File getOutputFile() {
		return outputFile.get();
	}

	public void setOutputFile(File file) {
		this.outputFile.set(file);
	}

	public boolean getVideoExport() {
		return videoExport.get();
	}

	public void setVideoExport(boolean export) {
		this.videoExport.set(export);
	}

	public BooleanProperty videoExportProperty() {
		return videoExport;
	}

	public boolean getWebVectorExport() {
		return vectorExport.get();
	}

	public void setWebVectorExport(boolean export) {
		this.vectorExport.set(export);
	}

	public BooleanProperty webVectorExportProperty() {
		return vectorExport;
	}
}
