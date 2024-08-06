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

package org.lecturestudio.media.video;

import static java.util.Objects.nonNull;

import static org.bytedeco.ffmpeg.global.avformat.AVSEEK_FLAG_BACKWARD;
import static org.bytedeco.ffmpeg.global.avformat.avformat_seek_file;
import static org.bytedeco.ffmpeg.global.avutil.AV_NOPTS_VALUE;
import static org.bytedeco.ffmpeg.global.avutil.AV_TIME_BASE;

import java.io.File;

import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

/**
 * Reads audio and video frames using the FFmpeg library.
 *
 * @author Alex Andres
 */
public class VideoFrameGrabber extends ExecutableBase {

    private FFmpegFrameGrabber grabber;

	private File videoFile;


	public double getFrameRate() {
		return grabber.getFrameRate();
	}

	/**
	 * Reads audio and video frames at the current position in the audio/video stream. This call will move the reading
	 * position to the next frame in the audio/video stream.
	 *
	 * @return The decoded audio frame containing audio samples or the decoded video frame containing the picture data.
	 *
	 * @throws Exception If the frame could not be read.
	 */
	public Frame grabFrame() throws Exception {
		return grabber.grabFrame();
	}

	/**
	 * Reads audio frames at the current position in the audio stream. This call will move the reading position to the
	 * next frame in the audio stream.
	 *
	 * @return The decoded audio frame containing audio samples.
	 *
	 * @throws Exception If the audio frame could not be read.
	 */
	public Frame grabAudioFrame() throws Exception {
		return grabber.grabSamples();
	}

	/**
	 * Reads video frames at the current position in the video stream. This call will move the reading position to the
	 * next frame in the video stream.
	 *
	 * @return The decoded audio frame containing audio samples.
	 *
	 * @throws Exception If the audio frame could not be read.
	 */
	public Frame grabVideoFrame() throws Exception {
		return grabber.grabImage();
	}

	/**
	 * Sets the video file from which to read audio/video frames.
	 *
	 * @param videoFile The video file to read.
	 */
	public void setVideoFile(File videoFile) {
		this.videoFile = videoFile;
	}

	/**
	 * Sets the video stream position to the provided timestamp.
	 *
	 * @param timestampMs The timestamp in milliseconds.
	 *
	 * @param keyFrame True to retrieve a keyframe.
	 *
	 * @throws ExecutableException If the timestamp could not be set.
	 */
	public void setVideoTimestamp(long timestampMs, boolean keyFrame) throws ExecutableException {
		try {
			if (keyFrame) {
				var formatContext = grabber.getFormatContext();
				var videoStream = formatContext.streams(grabber.getVideoStream());

				// Convert milliseconds to microseconds and then to video time base.
				long timestamp = timestampMs * 1000 * AV_TIME_BASE / 1000000L;
				long early_ts = timestamp;

				// The stream start time.
				long ts0 = formatContext.start_time() != AV_NOPTS_VALUE ? formatContext.start_time() : 0;

				// Use the start time of the corresponding stream.
				if (nonNull(videoStream) && videoStream.start_time() != AV_NOPTS_VALUE) {
					AVRational time_base = videoStream.time_base();
					ts0 = 1000000L * videoStream.start_time() * time_base.num() / time_base.den();
				}

				// Add the stream start time.
				timestamp += ts0;
				early_ts += ts0;

				int ret;
				if ((ret = avformat_seek_file(formatContext, -1, 0L, early_ts, early_ts, AVSEEK_FLAG_BACKWARD)) < 0) {
					throw new ExecutableException("avformat_seek_file() error [%s]: Could not seek file to timestamp [%s].",
							ret, timestamp);
				}
			}
			else {
				// Convert milliseconds to microseconds.
				grabber.setVideoTimestamp(timestampMs * 1000);
			}
		}
		catch (FFmpegFrameGrabber.Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		grabber = new FFmpegFrameGrabber(videoFile);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			grabber.start();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			grabber.stop();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			grabber.release();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}
}
