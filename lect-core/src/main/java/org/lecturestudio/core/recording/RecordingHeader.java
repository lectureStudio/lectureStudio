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

package org.lecturestudio.core.recording;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.tool.Tool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RecordingHeader extends RecordedObjectBase {

	/**
	 * The file checksum/hash algorithm name.
	 */
	public static final String CHECKSUM_ALGORITHM = "SHA-1";

	/**
	 * The length in bytes of the file checksum/hash. SHA-1 hash length is 20 bytes.
	 */
	private static final int CHECKSUM_LENGTH = 20;

	/**
	 * Content format marker '.PLR' represented as an integer value.
	 */
	private static final int FORMAT_MARKER = 777014354;

	/**
	 * The file version number.
	 */
	private int version;

	/**
	 * The duration in milliseconds of the recording.
	 */
	private long duration;

	/**
	 * The file contents' checksum.
	 */
	private byte[] checksum;

	/**
	 * The length in bytes of the events chunk.
	 */
	private int eventsLength;

	/**
	 * The length in bytes of the document chunk.
	 */
	private int documentLength;

	/**
	 * The length in bytes of the audio chunk.
	 */
	private int audioLength;

	/**
	 * The length in bytes of the camera recording file data.
	 */
	private int cameraRecordingFileNameLength;

	/**
	 * The length in bytes of the tool demo recordings data.
	 */
	private int toolDemoRecordingsLength;

	public RecordingHeader() {
		setVersion(0);
		setDuration(0L);
		setChecksum(new byte[CHECKSUM_LENGTH]);
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDuration() {
		return duration;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setEventsLength(int length) {
		this.eventsLength = length;
	}

	public int getEventsLength() {
		return eventsLength;
	}

	public void setDocumentLength(int length) {
		this.documentLength = length;
	}

	public int getDocumentLength() {
		return documentLength;
	}

	public void setAudioLength(int length) {
		this.audioLength = length;
	}

	public int getAudioLength() {
		return audioLength;
	}

	public int getCameraRecordingFileNameLength() {
		return cameraRecordingFileNameLength;
	}

	public void setCameraRecordingFileNameLength(int cameraRecordingFileNameLength) {
		this.cameraRecordingFileNameLength = cameraRecordingFileNameLength;
	}

	public int getToolDemoRecordingsLength() {
		return toolDemoRecordingsLength;
	}

	public void setToolDemoRecordingsLength(int toolDemoRecordingsLength) {
		this.toolDemoRecordingsLength = toolDemoRecordingsLength;
	}

	public int getHeaderLength() {
		return CHECKSUM_LENGTH + 36;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(getHeaderLength());
		buffer.putInt(FORMAT_MARKER);
		buffer.putInt(getVersion());
		buffer.putLong(getDuration());
		buffer.put(getChecksum());
		buffer.putInt(getEventsLength());
		buffer.putInt(getDocumentLength());
		buffer.putInt(getAudioLength());
		buffer.putInt(getCameraRecordingFileNameLength());
		buffer.putInt(getToolDemoRecordingsLength());

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		int marker = buffer.getInt();
		if (marker != FORMAT_MARKER) {
			throw new IOException("Invalid recording header");
		}

		setVersion(buffer.getInt());
		setDuration(buffer.getLong());

		// Read the file checksum.
		byte[] checksum = new byte[CHECKSUM_LENGTH];
		buffer.get(checksum);
		setChecksum(checksum);

		// Read chunk lengths.
		setEventsLength(buffer.getInt());
		setDocumentLength(buffer.getInt());
		setAudioLength(buffer.getInt());
		setCameraRecordingFileNameLength(buffer.getInt());
		setToolDemoRecordingsLength(buffer.getInt());
	}

	public RecordingHeader clone() {
		RecordingHeader header = new RecordingHeader();
		header.setVersion(getVersion());
		header.setDuration(getDuration());
		header.setChecksum(getChecksum().clone());
		header.setEventsLength(getEventsLength());
		header.setDocumentLength(getDocumentLength());
		header.setAudioLength(getAudioLength());
		header.setCameraRecordingFileNameLength(getCameraRecordingFileNameLength());
		header.setToolDemoRecordingsLength(getToolDemoRecordingsLength());

		return header;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(getClass().getSimpleName()).append("]\n");
		builder.append("Version: ").append(getVersion()).append("\n");
		builder.append("Duration: ").append(getDuration()).append("\n");
		builder.append("Events: ").append(getEventsLength()).append("\n");
		builder.append("Document: ").append(getDocumentLength()).append("\n");
		builder.append("Audio: ").append(getAudioLength()).append("\n");
		builder.append("CameraRecordingFileName: ").append(getCameraRecordingFileNameLength()).append("\n");
		builder.append("ToolDemoRecordings: ").append(getToolDemoRecordingsLength()).append("\n");

		return builder.toString();
	}
}
