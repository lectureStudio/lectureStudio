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

package org.lecturestudio.core.recording.action;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.graphics.StrokeLineCap;
import org.lecturestudio.core.tool.Stroke;

public abstract class BaseStrokeAction extends PlaybackAction {

	protected int shapeHandle;

	protected Stroke stroke;


	public BaseStrokeAction(int shapeHandle, Stroke stroke, KeyEvent keyEvent) {
		super(keyEvent);

		this.shapeHandle = shapeHandle;
		this.stroke = stroke;
	}

	public BaseStrokeAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int length = nonNull(stroke) ? 17 : 4;

		ByteBuffer buffer = createBuffer(length);
		buffer.putInt(shapeHandle);

		if (nonNull(stroke)) {
			// Stroke data: 13 bytes.
			buffer.putInt(stroke.getColor().getRGBA());
			buffer.put((byte) stroke.getStrokeLineCap().ordinal());
			buffer.putDouble(stroke.getWidth());
		}

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		if (buffer.remaining() >= 17) {
			// For backwards compatibility.
			shapeHandle = buffer.getInt();
		}

		if (buffer.remaining() >= 13) {
			// Stroke
			Color color = new Color(buffer.getInt());
			StrokeLineCap lineCap = StrokeLineCap.values()[buffer.get()];
			double penWidth = buffer.getDouble();

			stroke = new Stroke(color, penWidth);
			stroke.setStrokeLineCap(lineCap);
		}
	}

}
