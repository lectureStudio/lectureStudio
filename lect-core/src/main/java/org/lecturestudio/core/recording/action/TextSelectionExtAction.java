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
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.tool.TextSelectionSettings;
import org.lecturestudio.core.tool.TextSelectionTool;
import org.lecturestudio.core.tool.ToolType;

public class TextSelectionExtAction extends PlaybackAction implements LocationModifiable {

	private final List<Rectangle2D> selection = new ArrayList<>();

	private int shapeHandle;

	private Color color;


	public TextSelectionExtAction(int shapeHandle, Color color) {
		super();

		this.shapeHandle = shapeHandle;
		this.color = color;
	}

	public TextSelectionExtAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	public boolean addSelection(Rectangle2D rect) {
		for (Rectangle2D r : selection) {
			if (r.equals(rect)) {
				return false;
			}
		}

		this.selection.add(rect);

		return true;
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		TextSelectionSettings settings = controller.getPaintSettings(ToolType.TEXT_SELECTION);
		settings.setColor(color);

		controller.setTool(new TextSelectionTool(controller, selection, shapeHandle));
		controller.setKeyEvent(getKeyEvent());

		var it = selection.iterator();

		if (it.hasNext()) {
			Rectangle2D rect = it.next();
			PenPoint2D point = new PenPoint2D(rect.getX() + rect.getWidth() / 2,
					rect.getY() + rect.getHeight() / 2);

			controller.beginToolAction(point);
			controller.executeToolAction(point);
			controller.endToolAction(point);
		}
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int length = 12 + selection.size() * 32;

		ByteBuffer buffer = createBuffer(length);

		buffer.putInt(shapeHandle);
		buffer.putInt(color.getRGBA());
		buffer.putInt(selection.size());

		if (!selection.isEmpty()) {
			for (Rectangle2D rect : selection) {
				buffer.putDouble(rect.getX());
				buffer.putDouble(rect.getY());
				buffer.putDouble(rect.getWidth());
				buffer.putDouble(rect.getHeight());
			}
		}

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		if (buffer.remaining() >= 44) {
			// For backwards compatibility.
			shapeHandle = buffer.getInt();
		}

		color = new Color(buffer.getInt());

		int count = buffer.getInt();

		for (int i = 0; i < count; i++) {
			selection.add(new Rectangle2D(buffer.getDouble(), buffer.getDouble(),
										  buffer.getDouble(), buffer.getDouble()));
		}
	}

	@Override
	public ActionType getType() {
		return ActionType.TEXT_SELECTION_EXT;
	}
	
	@Override
	public boolean hasHandle() {
		return true;
	}

	@Override
	public int getHandle() {
		return shapeHandle;
	}

	public void moveByDelta(Point2D delta) {
		selection.forEach((rect) -> rect.setLocation(rect.getX() - delta.getX(), rect.getY() - delta.getY()));
	}
}
