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

/**
 * Action that triggers the zoom out tool functionality during recording playback.
 * This class is responsible for encapsulating the zoom out operation for serialization
 * and execution during playback.
 *
 * @author Alex Andres
 */
public class ZoomOutAction extends PlaybackAction {

	/**
	 * Creates a new zoom out action with default parameters.
	 */
	public ZoomOutAction() {
		
	}

	/**
	 * Creates a new zoom-out action from serialized data.
	 *
	 * @param input The byte array containing the serialized action data.
	 *
	 * @throws IOException If an error occurs while parsing the input data.
	 */
	public ZoomOutAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.selectZoomOutTool();
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteBuffer buffer = createBuffer(0);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		createBuffer(input);
	}

	@Override
	public ActionType getType() {
		return ActionType.ZOOM_OUT;
	}

}
