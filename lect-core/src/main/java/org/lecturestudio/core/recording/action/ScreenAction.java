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
import java.nio.charset.StandardCharsets;

import org.lecturestudio.core.controller.ToolController;

public class ScreenAction extends PlaybackAction {

	private String fileName;

	private int videoOffset;

	private int videoLength;


	public ScreenAction(String fileName) {
		this.fileName = fileName;
	}

	public ScreenAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	public String getFileName() {
		return fileName;
	}

	public void setVideoLength(int videoLength) {
		this.videoLength = videoLength;
	}

	public int getVideoLength() {
		return videoLength;
	}

	public void setVideoOffset(int offset) {
		this.videoOffset = offset;
	}

	public int getVideoOffset() {
		return videoOffset;
	}

	@Override
	public ActionType getType() {
		return ActionType.SCREEN;
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		// Ignore. Rendering will take place in other components, e.g., VideoReader.
		controller.clearShapes();
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] data = fileName.getBytes(StandardCharsets.UTF_8);
		int dataLength = data.length;

		ByteBuffer buffer = createBuffer(dataLength + 12);
		buffer.putInt(videoOffset);
		buffer.putInt(videoLength);
		buffer.putInt(dataLength);
		buffer.put(data);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		videoOffset = buffer.getInt();
		videoLength = buffer.getInt();

		int nameLength = buffer.getInt();
		byte[] nameBuffer = new byte[nameLength];

		buffer.get(nameBuffer);

		fileName = new String(nameBuffer, StandardCharsets.UTF_8);
	}
}
