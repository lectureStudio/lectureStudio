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

package org.lecturestudio.core.camera.bus.event;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.lecturestudio.core.bus.event.BusEvent;

public class CameraImageEvent extends BusEvent {

	/** The stats. */
	private final Map<String, Number> stats;

	/** The image. */
	private final BufferedImage image;


	/**
	 * Create the {@link CameraImageEvent} with specified image and stats.
	 * @param image
	 * @param stats
	 */
	public CameraImageEvent(BufferedImage image, Map<String, Number> stats) {
		this.image = image;
		this.stats = stats;
	}

	/**
	 * Get the image.
	 *
	 * @return The image.
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Get the stats.
	 *
	 * @return The stats.
	 */
	public Map<String, Number> getStats() {
		return stats;
	}

}
