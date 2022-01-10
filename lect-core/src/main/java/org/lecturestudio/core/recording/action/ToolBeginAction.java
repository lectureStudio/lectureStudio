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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;

public class ToolBeginAction extends PlaybackAction {

	private PenPoint2D point;


	public ToolBeginAction(PenPoint2D point) {
		this.point = point;
	}

	public ToolBeginAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.beginToolAction(point);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		if (point == null) {
			return createBuffer(0).array();
		}

		ByteBuffer buffer = createBuffer(12);
		buffer.putFloat((float) point.getX());
		buffer.putFloat((float) point.getY());
		buffer.putFloat((float) point.getPressure());

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		if (buffer.remaining() >= 24) {
			// Backward compatibility.
			point = new PenPoint2D(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
		}
		else if (buffer.remaining() >= 12) {
			point = new PenPoint2D(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
		}
	}

	@Override
	public ActionType getType() {
		return ActionType.TOOL_BEGIN;
	}

	public void setPoint(PenPoint2D point) {
		this.point = point;
	}
}
