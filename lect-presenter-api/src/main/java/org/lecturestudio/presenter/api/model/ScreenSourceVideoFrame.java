/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.model;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.nio.ByteBuffer;

public class ScreenSourceVideoFrame {

	/**
	 * The rectangle in full desktop coordinates.
	 */
	public final Rectangle frameRect;

	/**
	 * The size of the frame in full desktop coordinate space.
	 */
	public final Dimension frameSize;

	/**
	 * Distance in the buffer between two neighboring rows in bytes.
	 */
	public final int stride;

	/**
	 * The underlying frame buffer.
	 */
	public final ByteBuffer buffer;


	public ScreenSourceVideoFrame(Rectangle frameRect, Dimension frameSize,
			int stride, ByteBuffer buffer) {
		this.frameRect = frameRect;
		this.frameSize = frameSize;
		this.stride = stride;
		this.buffer = buffer;
	}
}