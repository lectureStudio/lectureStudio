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

package org.lecturestudio.core.codec;

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * The {@link VideoCodecConfiguration} contains video codec related properties.
 *
 * @author Alex Andres
 */
public class VideoCodecConfiguration extends CodecConfiguration {

	/** The frame rate for a video encoder. */
    private final DoubleProperty frameRate = new DoubleProperty();

	/** The bounding box of the encoded or decoded image. */
	private final ObjectProperty<Rectangle2D> viewRect = new ObjectProperty<>();


	/**
	 * Get the bounding box of the encoded or decoded image.
	 *
	 * @return The image bounding box.
	 */
	public Rectangle2D getViewRect() {
		return viewRect.get();
	}

	/**
	 * Set the bounding box of the encoded or decoded image.
	 *
	 * @param viewRect The bounding box of the image to set.
	 */
	public void setViewRect(Rectangle2D viewRect) {
		this.viewRect.set(viewRect);
	}

	/**
	 * Get the image bounding box property.
	 *
	 * @return The image bounding box property.
	 */
	public ObjectProperty<Rectangle2D> viewRectProperty() {
		return viewRect;
	}

	/**
	 * Get the target framerate for the video encoder.
	 *
	 * @return The target framerate for the video encoder.
	 */
    public double getFrameRate() {
        return frameRate.get();
    }

	/**
	 * Set the target framerate for the video encoder.
	 *
	 * @param frameRate The target framerate to set.
	 */
    public void setFrameRate(double frameRate) {
        this.frameRate.set(frameRate);
    }

	@Override
	public String toString() {
		return "View-Rect: " + getViewRect() + "\n" +
				"Framerate: " + getFrameRate() + "\n" +
				"Bitrate: " + getBitRate();
	}

}
