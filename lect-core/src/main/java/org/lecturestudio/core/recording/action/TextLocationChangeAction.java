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
import org.lecturestudio.core.geometry.Point2D;

public class TextLocationChangeAction extends PlaybackAction {

	private int handle;

	private Point2D location;


	public TextLocationChangeAction(int handle, Point2D location) {
		this.handle = handle;
		this.location = location;
	}

	public TextLocationChangeAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.setTextLocation(handle, location);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int payloadBytes = 4 + 16;

		ByteBuffer buffer = createBuffer(payloadBytes);

		// Shape handle.
		buffer.putInt(handle);

		// Location
		buffer.putDouble(location.getX());
		buffer.putDouble(location.getY());

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();

		double x = buffer.getDouble();
		double y = buffer.getDouble();

		location = new Point2D(x, y);
	}

	@Override
	public ActionType getType() {
		return ActionType.TEXT_LOCATION_CHANGE;
	}

}
