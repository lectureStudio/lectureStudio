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

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.codec.CodecID;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;

public class VideoRenderConfiguration {

	/** The video codec ID. */
	private final ObjectProperty<CodecID> codecID = new ObjectProperty<>();

	/** The video dimension. */
	private final ObjectProperty<Dimension2D> dimension = new ObjectProperty<>();

	/** The video frame rate. */
	private final IntegerProperty frameRate = new IntegerProperty();

	/** The video bitrate. */
	private final IntegerProperty bitrate = new IntegerProperty();

	/** The two-pass encoding flag. */
	private final BooleanProperty twoPass = new BooleanProperty();

	/** The n-th encoding pass. */
	private final IntegerProperty pass = new IntegerProperty();

	/** The two-pass profile path. */
	private String twoPassProfilePath;

	/** The placement of the camera recording. */
	private ObjectProperty<Position> cameraRecordingPlacement = new ObjectProperty<>();


	public CodecID getCodecID() {
		return codecID.get();
	}

	public void setCodecID(CodecID codecID) {
		this.codecID.set(codecID);
	}

	public ObjectProperty<CodecID> codecIDProperty() {
		return codecID;
	}

	public Dimension2D getDimension() {
		return dimension.get();
	}

	public void setDimension(Dimension2D size) {
		this.dimension.set(size);
	}

	public ObjectProperty<Dimension2D> dimensionProperty() {
		return dimension;
	}

	public int getFrameRate() {
		return frameRate.get();
	}

	public void setFrameRate(int rate) {
		this.frameRate.set(rate);
	}

	public IntegerProperty frameRateProperty() {
		return frameRate;
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

	public boolean getTwoPass() {
		return twoPass.get();
	}

	public void setTwoPass(boolean twoPass) {
		this.twoPass.set(twoPass);
	}

	public BooleanProperty twoPassProperty() {
		return twoPass;
	}

	public int getPass() {
		return pass.get();
	}

	public void setPass(int pass) {
		this.pass.set(pass);
	}

	public IntegerProperty passProperty() {
		return pass;
	}

	public String getTwoPassProfilePath() {
		return twoPassProfilePath;
	}

	public void setTwoPassProfilePath(String profilePath) {
		this.twoPassProfilePath = profilePath;
	}

	public ObjectProperty<Position> getCameraRecordingPlacement() {
		return cameraRecordingPlacement;
	}

	public void setCameraRecordingPlacement(Position cameraRecordingPlacement) {
		this.cameraRecordingPlacement.set(cameraRecordingPlacement);
	}
}
