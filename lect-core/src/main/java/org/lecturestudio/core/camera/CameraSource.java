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

package org.lecturestudio.core.camera;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.codec.VideoEncoder;
import org.lecturestudio.core.codec.h264.H264StreamEncoder;
import org.lecturestudio.core.io.VideoSink;
import org.lecturestudio.core.io.VideoSource;

/**
 * The {@link CameraSource} captures video frames with an camera, encodes the captured
 * frames and writes them to a {@link VideoSink}.
 *
 * @author Alex Andres
 */
public class CameraSource implements VideoSource, FrameGrabberCallback {

	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	/** The video sink handler. */
	private Handler handler = new Handler();

	/** The video sink handler. */
	private VideoEncoder videoEncoder;

	/** The video frame captor. */
	private FrameGrabber frameGrabber;

	/** The video sink that receives captured frames. */
	private VideoSink videoSink;


	/**
	 * Create a new {@link CameraSource} with the specified camera, the capturing format
	 * and video codec configuration.
	 *
	 * @param camera      The camera what captures the video frames.
	 * @param format      The camera format that describes the format of the frames.
	 * @param codecConfig The video encoder configuration.
	 */
	public CameraSource(Camera camera, CameraFormat format, VideoCodecConfiguration codecConfig) {
		this.videoEncoder = new H264StreamEncoder(codecConfig);
		this.frameGrabber = new FrameGrabber(this, camera, format);
	}

	@Override
	public void setSink(VideoSink videoSink) {
		this.videoSink = videoSink;
	}

	@Override
	public void open() {

	}

	@Override
	public void start() {
		frameGrabber.start();
	}

	@Override
	public void stop() {
		frameGrabber.stop();
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void onFrame(BufferedImage image) {
		handler.setImage(image);

		executor.execute(handler);
	}



	/**
	 * The video sink handler.
	 */
	private class Handler implements Runnable {

		/** The buffered image of the {@link Handler}. */
		private BufferedImage image;

		/**
		 * Set a new image.
		 *
		 * @param image The new image.
		 */
		public void setImage(BufferedImage image) {
			this.image = image;
		}

		@Override
		public void run() {
			videoSink.onVideoFrame(videoEncoder.encode(image));
		}
	}

}
