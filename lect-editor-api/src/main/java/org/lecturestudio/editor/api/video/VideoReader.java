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

package org.lecturestudio.editor.api.video;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacv.*;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

/**
 * Reads videos that have been recorded during a presentation. This video reader returns decoded video frames that can
 * be retrieved sequentially or after jumping to a specific position in the video stream.
 *
 * @author Alex Andres
 */
public class VideoReader extends ExecutableBase {

	private final File workingDir;

	private File videoFile;

	private int videoOffset;

	private int videoLength;

	private long referenceTimestamp;

	private VideoFrameGrabber grabber;


	/**
	 * Creates a new VideoReader with the provided working directory from which the videos are loaded.
	 *
	 * @param workingDir The directory containing the videos to load.
	 */
	public VideoReader(File workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Sets the file name of the video to load.
	 *
	 * @param fileName The video file name.
	 */
	public void setVideoFile(String fileName) {
		videoFile = new File(workingDir, fileName);
	}

	/**
	 * Sets the offset in milliseconds indicating from which position to start reading the video frames.
	 *
	 * @param offset The offset in milliseconds.
	 */
	public void setVideoOffset(int offset) {
		this.videoOffset = offset;
	}

	/**
	 * Sets the length of the video in milliseconds.
	 *
	 * @param length The length of the video in milliseconds.
	 */
	public void setVideoLength(int length) {
		videoLength = length;
	}

	/**
	 * Sets the reference timestamp in milliseconds to adjust the reading position of the video frames.
	 *
	 * @param timestamp The reference timestamp in milliseconds.
	 */
	public void setReferenceTimestamp(long timestamp) {
		this.referenceTimestamp = timestamp;
	}

	/**
	 * Retrieves a video frame at the specified position in the video stream.
	 *
	 * @param timestamp The timestamp in milliseconds.
	 *
	 * @return The video frame at the specified timestamp.
	 *
	 * @throws IOException If the video frame could not be retrieved.
	 */
	public Frame seekToVideoFrame(long timestamp) throws IOException {
		try {
			grabber.setVideoTimestamp((timestamp - referenceTimestamp) + videoOffset, false);

			return readVideoFrame();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Retrieves a video keyframe at the specified position in the video stream. Reading keyframes using this method is
	 * less accurate with reference to timestamps than reading frames using {@link #seekToVideoFrame(long)}.
	 *
	 * @param timestamp The timestamp in milliseconds.
	 *
	 * @return The video keyframe at the specified timestamp.
	 *
	 * @throws IOException If the keyframe could not be retrieved.
	 */
	public Frame seekToVideoKeyFrame(long timestamp) throws IOException {
		try {
			grabber.setVideoTimestamp((timestamp - referenceTimestamp) + videoOffset, true);

			return readVideoFrame();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Reads one video frame at the current position in the video stream. This call will move the reading position to
	 * the next frame in the video stream.
	 *
	 * @return The video frame at the current position in the video stream.
	 *
	 * @throws Exception If the video frame could not be read.
	 */
	public Frame readVideoFrame() throws Exception {
		Frame frame = grabber.grabVideoFrame();

		// Check if we reached the end of the video.
		if (isNull(frame) || (frame.timestamp / 1000) > (videoOffset + videoLength)) {
			return null;
		}

		return frame;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(videoFile) || !videoFile.exists()) {
			throw new ExecutableException("No video file specified to read");
		}

		grabber = new VideoFrameGrabber();
		grabber.setVideoFile(videoFile);
		grabber.init();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		grabber.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		grabber.stop();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		grabber.destroy();
	}
}
