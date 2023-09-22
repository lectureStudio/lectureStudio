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

package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;
import org.lecturestudio.media.video.VideoFormat;

import java.awt.image.BufferedImage;
import java.util.List;

public interface VideoExportSettingsView extends View {

	/**
	 * Video settings
	 */

	void setDimensions(Dimension2D[] dimensions);

	void setDimension(Dimension2D dimension);

	void bindDimension(ObjectProperty<Dimension2D> property);

	void setVideoFormats(VideoFormat[] formats);

	void bindVideoFormat(ObjectProperty<VideoFormat> property);

	void setFrameRates(Integer[] rates);

	void bindFrameRate(IntegerProperty property);

	void bindVideoBitrate(IntegerProperty property);

	public void setCameraRecordingPlacements(Position[] formats);

	public void bindCameraRecordingPlacement(ObjectProperty<Position> property);

	public void setCameraPreview(BufferedImage buffImage);

	void bindTwoPassEncoding(BooleanProperty enable);

	/**
	 * Audio settings
	 */

	void setAudioFormats(AudioFormat[] formats);

	void bindAudioFormat(ObjectProperty<AudioFormat> property);

	void setAudioBitrates(Integer[] rates);

	void bindAudioBitrate(IntegerProperty property);

	void bindAudioVBR(BooleanProperty property);

	/**
	 * Buttons
	 */

	void setOnCreate(Action action);

}
