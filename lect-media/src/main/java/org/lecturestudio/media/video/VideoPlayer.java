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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.bytedeco.javacv.Frame;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;

/**
 * Plays videos that have been recorded during a presentation.
 *
 * @author Alex Andres
 */
public class VideoPlayer extends ExecutableBase {

	private final File workingDir;

	private File videoFile;

	private int videoOffset;

	private int videoLength;

	private long referenceTimestamp;

	private VideoFrameGrabber grabber;

	private VideoRenderSurface videoRenderSurface;

	private IoThread ioThread;


	/**
	 * Creates a new VideoPlayer with the provided working directory from which the videos are loaded.
	 *
	 * @param workingDir The directory containing the videos to load.
	 */
	public VideoPlayer(File workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Returns the video file.
	 *
	 * @return The video file.
	 */
	public File getVideoFile() {
		return videoFile;
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
	 * Sets the surface where to render the video frames.
	 *
	 * @param renderSurface The surface where to render the video frames.
	 */
	public void setVideoRenderSurface(VideoRenderSurface renderSurface) {
		videoRenderSurface = renderSurface;
	}

	/**
	 * Retrieves a video frame at the specified position in the video stream.
	 *
	 * @param timestamp The timestamp in milliseconds.
	 *
	 * @throws IOException If the video frame could not be retrieved.
	 */
	public void seekToVideoFrame(long timestamp) throws IOException {
		try {
			grabber.setVideoTimestamp((timestamp - referenceTimestamp) + videoOffset, false);

			Frame frame = readVideoFrame();
			videoRenderSurface.renderFrame(frame);
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
	 * @throws IOException If the keyframe could not be retrieved.
	 */
	public void seekToVideoKeyFrame(long timestamp) throws IOException {
		try {
			grabber.setVideoTimestamp((timestamp - referenceTimestamp) + videoOffset, true);

			Frame frame = readVideoFrame();
			videoRenderSurface.renderFrame(frame);
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
		grabber.start();

		ioThread = new IoThread(new IoTask(), "Video IO Thread");
	}

	@Override
	protected void startInternal() throws ExecutableException {
		ExecutableState state = getPreviousState();

		if (state == ExecutableState.Initialized || state == ExecutableState.Stopped) {
			ioThread.start();
		}
		else if (state == ExecutableState.Suspended) {
			// Interrupt the Thread in case it was sleeping to play new frames again.
			if (ioThread.getState() == Thread.State.TIMED_WAITING) {
				ioThread.interrupt();
			}
			ioThread.signal();
		}

		System.out.println("Video started");
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		grabber.stop();

		ioThread.shutdown();

		System.out.println("Video stopped");
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		grabber.destroy();
	}



	class IoTask implements Runnable {


		@Override
		public void run() {
			while (nonNull(ioThread)) {
				if (!ioThread.isRunning()) {
					return;
				}

				ExecutableState state = getState();

				if (state == ExecutableState.Starting || state == ExecutableState.Started) {
					try {
						Frame frame = readVideoFrame();

						System.out.println("- " + (frame.timestamp / 1000) + " " + grabber.getFrameRate());

						videoRenderSurface.renderFrame(frame);

						Thread.sleep(1000 / 24);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}

				}
				else if (state == ExecutableState.Suspended) {
					System.out.println("Video suspended");
					ioThread.await();
				}
			}
		}
	}



	private static class IoThread extends Thread {

		private final ReentrantLock lock;

		private final Condition condition;

		private volatile boolean running;


		IoThread(Runnable runnable, String name) {
			super(runnable, name);

			lock = new ReentrantLock();
			condition = lock.newCondition();
			running = true;
		}

		boolean isRunning() {
			return running;
		}

		void shutdown() {
			running = false;

			lock.lock();

			try {
				if (lock.hasWaiters(condition)) {
					signal();
				}
			}
			finally {
				lock.unlock();
			}
		}

		void await() {
			lock.lock();

			try {
				condition.await();
			}
			catch (InterruptedException e) {
				// Ignore
			}
			finally {
				lock.unlock();
			}
		}

		void signal() {
			lock.lock();

			try {
				condition.signalAll();
			}
			finally {
				lock.unlock();
			}
		}
	}
}
