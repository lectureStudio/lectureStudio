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
import org.lecturestudio.core.geometry.Rectangle2D;

public class ExtendViewAction extends PlaybackAction {

	private Rectangle2D rect;


	public ExtendViewAction(Rectangle2D rect) {
		this.rect = rect;
	}

	public ExtendViewAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.zoom(rect);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteBuffer buffer = createBuffer(32);

		// View rectangle: 32 bytes.
		buffer.putDouble(rect.getX());
		buffer.putDouble(rect.getY());
		buffer.putDouble(rect.getWidth());
		buffer.putDouble(rect.getHeight());

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		double x = buffer.getDouble();
		double y = buffer.getDouble();
		double width = buffer.getDouble();
		double height = buffer.getDouble();

		rect = new Rectangle2D(x, y, width, height);
	}

	@Override
	public ActionType getType() {
		return ActionType.EXTEND_VIEW;
	}

}
