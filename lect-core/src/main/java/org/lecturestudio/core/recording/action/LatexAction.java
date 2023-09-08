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

public class LatexAction extends PlaybackAction {

	private int handle;


	public LatexAction(int handle) {
		this.handle = handle;
	}

	public LatexAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.selectLatexTool(handle);
		controller.setKeyEvent(getKeyEvent());
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteBuffer buffer = createBuffer(4);

		// Shape handle.
		buffer.putInt(handle);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		handle = buffer.getInt();
	}

	@Override
	public ActionType getType() {
		return ActionType.LATEX;
	}
	
	@Override
	public boolean hasHandle() {
		return true;
	}

	@Override
	public int getHandle() {
		return handle;
	}
}
