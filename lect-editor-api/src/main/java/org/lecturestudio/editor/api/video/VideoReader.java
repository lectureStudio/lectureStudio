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
import static java.util.Objects.nonNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.bytedeco.javacv.*;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.geometry.Dimension2D;

public class VideoReader extends ExecutableBase {

	private final Java2DFrameConverter converter = new Java2DFrameConverter();

	private final File workingDir;

	private File videoFile;

	private Dimension2D targetImageSize;

	private int videoOffset;

	private int videoLength;

	private long referenceTimestamp;

	private FFmpegFrameFilter filter;

	private FFmpegFrameGrabber grabber;

	private BufferedImage image;


	public VideoReader(File workingDir) {
		this.workingDir = workingDir;
	}

	public void setVideoFile(String fileName) {
		videoFile = new File(workingDir, fileName);
	}

	public void setVideoOffset(int offset) {
		this.videoOffset = offset;
	}

	public void setVideoLength(int length) {
		videoLength = length;
	}

	public void setTargetImageSize(Dimension2D size) {
		this.targetImageSize = size;
	}

	public void setReferenceTimestamp(long timestamp) {
		this.referenceTimestamp = timestamp;
	}

	public Frame seekToVideoFrame(long timestamp) throws IOException {
		try {
			// Convert milliseconds to microseconds.
			grabber.setVideoTimestamp(((timestamp - referenceTimestamp) + videoOffset) * 1000);

			return grabber.grabImage();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public BufferedImage renderFrame(long timestamp) throws IOException {
		try {
			// Convert milliseconds to microseconds.
			grabber.setVideoTimestamp(((timestamp - referenceTimestamp) + videoOffset) * 1000);

			image = renderCurrentFrame();
		}
		catch (Exception e) {
			throw new IOException(e);
		}

		return image;
	}

	public BufferedImage renderCurrentFrame() throws IOException {
		try {
			Frame frame = grabber.grabImage();

			if (isNull(frame) || (frame.timestamp / 1000) > (videoOffset + videoLength)) {
				stop();
				destroy();
				return image;
			}

			if (nonNull(filter)) {
				filter.push(frame);
				frame = filter.pull();
			}

			BufferedImage converted = converter.convert(frame);

			if (isNull(image) || image.getWidth() != frame.imageWidth || image.getHeight() != frame.imageHeight) {
				image = new BufferedImage(frame.imageWidth, frame.imageHeight, BufferedImage.TYPE_INT_RGB);
			}

			// Convert type byte to type int image.
			var g2d = image.createGraphics();
			g2d.drawImage(converted, null, 0, 0);
			g2d.dispose();
		}
		catch (Exception e) {
			throw new IOException(e);
		}

		return image;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(videoFile) || !videoFile.exists()) {
			throw new ExecutableException("No video file specified");
		}

		try {
			grabber = new FFmpegFrameGrabber(videoFile);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			grabber.start();

			if (nonNull(targetImageSize)) {
				// Initialize the filter after the grabber has been started.
				String scale = String.format("scale=%dx%d", (int) targetImageSize.getWidth(), (int) targetImageSize.getHeight());

				filter = new FFmpegFrameFilter(scale, grabber.getImageWidth(), grabber.getImageHeight());
				filter.setPixelFormat(grabber.getPixelFormat());
				filter.start();
			}
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			if (nonNull(filter)) {
				filter.stop();
				filter.release();
			}

			grabber.stop();
			grabber.release();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() {
		// Nothing to do.
	}
}
