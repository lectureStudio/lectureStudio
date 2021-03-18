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

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.codec.CodecID;

public class AudioRenderConfiguration {

	/** The audio codec ID. */
	private final ObjectProperty<CodecID> codecID = new ObjectProperty<>();

	/** The audio source format to be used. */
	private final ObjectProperty<AudioFormat> inputFormat = new ObjectProperty<>();

	/** The audio format to be used. */
	private final ObjectProperty<AudioFormat> outputFormat = new ObjectProperty<>();

	/** The audio bitrate. */
	private final IntegerProperty bitrate = new IntegerProperty();

	/** The flag whether to use variable bitrate. */
	private final BooleanProperty vbr = new BooleanProperty();

	/** The video input file. */
	private File videoInputFile;


	public CodecID getCodecID() {
		return codecID.get();
	}

	public void setCodecID(CodecID codecID) {
		this.codecID.set(codecID);
	}

	public ObjectProperty<CodecID> codecIDProperty() {
		return codecID;
	}

	public AudioFormat getOutputFormat() {
		return outputFormat.get();
	}

	public void setOutputFormat(AudioFormat audioFormat) {
		this.outputFormat.set(audioFormat);
	}

	public ObjectProperty<AudioFormat> outputFormatProperty() {
		return outputFormat;
	}

	public AudioFormat getInputFormat() {
		return inputFormat.get();
	}

	public void setInputFormat(AudioFormat audioFormat) {
		this.inputFormat.set(audioFormat);
	}

	public ObjectProperty<AudioFormat> inputFormatProperty() {
		return inputFormat;
	}

	public int getBitrate() {
		return bitrate.get();
	}

	public void setBitrate(int rate) {
		this.bitrate.set(rate);
	}

	public IntegerProperty bitrateProperty() {
		return bitrate;
	}

	public boolean getVBR() {
		return vbr.get();
	}

	public void setVBR(boolean enable) {
		this.vbr.set(enable);
	}

	public BooleanProperty vbrProperty() {
		return vbr;
	}

	public File getVideoInputFile() {
		return videoInputFile;
	}

	public void setVideoInputFile(File file) {
		this.videoInputFile = file;
	}
}
