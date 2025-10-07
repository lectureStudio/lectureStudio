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
import java.nio.charset.StandardCharsets;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.Dimension2D;

/**
 * Represents a screen action in a recording playback.
 * This action manages video file information including file name, dimensions, offset, and length.
 *
 * @author Alex Andres
 */
public class ScreenAction extends PlaybackAction {

    /** The file name of the video associated with this screen action. */
	private String fileName;

    /** The dimensions of the video (width and height). */
	private Dimension2D videoDimension;

    /** The offset position of the video in bytes or frames. */
	private int videoOffset;

    /** The length of the video in bytes or frames. */
	private int videoLength;


    /**
     * Constructs a new ScreenAction with the specified file name.
     *
     * @param fileName the name of the video file.
     */
	public ScreenAction(String fileName) {
		this.fileName = fileName;
	}

    /**
     * Constructs a ScreenAction by parsing the provided byte array.
     *
     * @param input the byte array containing serialized ScreenAction data.
	 *
     * @throws IOException if an error occurs during parsing.
     */
	public ScreenAction(byte[] input) throws IOException {
		parseFrom(input);
	}

    /**
     * Returns the file name of the video.
     *
     * @return the file name.
     */
	public String getFileName() {
		return fileName;
	}

    /**
     * Returns the dimensions of the video.
     *
     * @return the video dimensions.
     */
	public Dimension2D getVideoDimension() {
		return videoDimension;
	}

    /**
     * Sets the dimensions of the video.
     *
     * @param dimension the dimensions to set
     */
	public void setVideoDimension(Dimension2D dimension) {
		this.videoDimension = dimension;
	}

    /**
     * Sets the length of the video.
     *
     * @param videoLength the video length to set
     */
	public void setVideoLength(int videoLength) {
		this.videoLength = videoLength;
	}

    /**
     * Returns the length of the video.
     *
     * @return the video length.
     */
	public int getVideoLength() {
		return videoLength;
	}

    /**
     * Sets the offset position of the video.
     *
     * @param offset the video offset to set.
     */
	public void setVideoOffset(int offset) {
		this.videoOffset = offset;
	}

    /**
     * Returns the offset position of the video.
     *
     * @return the video offset.
     */
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

		ByteBuffer buffer = createBuffer(dataLength + 20);
		buffer.putInt(videoOffset);
		buffer.putInt(videoLength);
		buffer.putInt(nonNull(videoDimension) ? (int) videoDimension.getWidth() : 0);
		buffer.putInt(nonNull(videoDimension) ? (int) videoDimension.getHeight() : 0);
		buffer.putInt(dataLength);
		buffer.put(data);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		videoOffset = buffer.getInt();
		videoLength = buffer.getInt();
		videoDimension = new Dimension2D(buffer.getInt(), buffer.getInt());

		int nameLength = buffer.getInt();
		byte[] nameBuffer = new byte[nameLength];

		buffer.get(nameBuffer);

		fileName = new String(nameBuffer, StandardCharsets.UTF_8);
	}
}
