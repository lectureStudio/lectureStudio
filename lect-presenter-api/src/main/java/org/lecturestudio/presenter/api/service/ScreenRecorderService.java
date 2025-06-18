/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.sink.ByteArrayAudioSink;
import org.lecturestudio.core.codec.CodecID;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.media.config.AudioRenderConfiguration;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;
import org.lecturestudio.media.video.AVDefaults;
import org.lecturestudio.media.video.FFmpegMuxer;
import org.lecturestudio.media.video.VideoMuxer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;

/**
 * Service responsible for recording screen content during presentations.
 * <p>
 * This service handles both audio and video recording, processes WebRTC video frames,
 * and combines them into an MP4 file. It subscribes to screen frame events and
 * manages the recording lifecycle, including starting, stopping, suspending, and
 * destroying recording resources.
 * <p>
 * The service uses FFmpeg for muxing audio and video streams and maintains timing
 * information for synchronization.
 *
 * @author Alex Andres
 */
public class ScreenRecorderService extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(ScreenRecorderService.class);

	/** Date formatter used to generate timestamps for recording file names. */
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");

	/** The presenter context providing access to configuration settings and the application event bus. */
	private final PresenterContext context;

	/** The target resolution for recorded videos (1920x1080). */
	private final Dimension2D outputSize = new Dimension2D(1920, 1080);

	/** Provider for audio system components used in recording. */
	private final AudioSystemProvider audioSystemProvider;

	/** The audio recorder component that captures system audio. */
	private AudioRecorder audioRecorder;

	/** The video muxer that combines audio and video streams into the output file. */
	private VideoMuxer muxer;

	/** The file system path where the recording will be saved. */
	private Path outputPath;

	/** Context containing information about the active screen sharing session. */
	private ScreenShareContext shareContext;

	/** Buffer used for temporary image processing during video frame conversion. */
	private BufferedImage bufferedImage;

	/** Action containing metadata about the screen recording. */
	private ScreenAction screenAction;

	/** Current timestamp in milliseconds for the recording session. */
	private int timestampMs;

	/** Timestamp in milliseconds when recording was last suspended. */
	private int timestampMsSuspend;

	/** The total number of video frames processed during the current recording session. */
	private int frames;


	/**
	 * Creates a new ScreenRecorderService instance.
	 *
	 * @param context             The presenter context providing configuration and event bus.
	 * @param audioSystemProvider The provider for audio system components used for recording.
	 */
	public ScreenRecorderService(PresenterContext context, AudioSystemProvider audioSystemProvider) {
		this.context = context;
		this.audioSystemProvider = audioSystemProvider;
	}

	/**
	 * Gets the current screen sharing context.
	 *
	 * @return The current screen-share context containing information about the active
	 *         screen sharing session.
	 */
	public ScreenShareContext getScreenShareContext() {
		return shareContext;
	}

	/**
	 * Sets the screen-sharing context for the recorder.
	 *
	 * @param shareContext The screen-share context containing information about the active
	 *                     screen sharing session.
	 */
	public void setScreenShareContext(ScreenShareContext shareContext) {
		this.shareContext = shareContext;
	}

	/**
	 * Gets the current screen action for the recording session.
	 *
	 * @return The screen action containing information about the current recording,
	 *         including file name, video offsets, and length.
	 */
	public ScreenAction getScreenAction() {
		return screenAction;
	}

	@Subscribe
	public void onEvent(LocalScreenVideoFrameEvent event) {
		if (!started()) {
			return;
		}

		try {
			final VideoFrame videoFrame = event.getFrame();
			videoFrame.retain();
			addVideoFrame(videoFrame);
			videoFrame.release();
		}
		catch (Exception e) {
			LOG.error("Mux video frame failed", e);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		context.getEventBus().register(this);

		frames = 0;
		timestampMs = 0;
		timestampMsSuspend = 0;

		initMuxer();
		initAudioRecorder();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		screenAction = new ScreenAction(outputPath.getFileName().toString());

		audioRecorder.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		context.getEventBus().unregister(this);

		audioRecorder.stop();
		audioRecorder.destroy();

		muxer.stop();
		muxer.destroy();
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		// Prepare action for the next start.
		screenAction.setVideoOffset(timestampMsSuspend);
		screenAction.setVideoLength(timestampMs - timestampMsSuspend);

		timestampMsSuspend = timestampMs;

		audioRecorder.suspend();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		// No further actions needed here as the resources are cleaned up in stopInternal.
	}

	/**
	 * Processes and adds a video frame to the recording, handling synchronization with audio.
	 * <p>
	 * This method manages the timing between audio and video streams to ensure proper synchronization.
	 * It performs the following operations:
	 * <ul>
	 *   <li>Checks for audio/video timestamp drift</li>
	 *   <li>Drops frames if video is ahead of audio</li>
	 *   <li>Adds or duplicates frames if video is behind audio</li>
	 *   <li>Converts and muxes the video frame</li>
	 * </ul>
	 *
	 * @param videoFrame The WebRTC video frame to be processed and added to the recording.
	 *
	 * @throws Exception If there's an error during frame conversion or muxing.
	 */
	private void addVideoFrame(VideoFrame videoFrame) throws Exception {
		// Check audio/video timestamp drift.
		int audioTimeMs = timestampMs;
		float currentFps = frames / (audioTimeMs / 1000f);

		float frameRate = shareContext.getProfile().getFramerate();
		int frameStepMs = (int) (1000 / frameRate);
		int videoTimeMs = (int) (frames / frameRate * 1000);

		// Drop video frame if ahead of audio.
		if (currentFps > frameRate) {
			return;
		}

		BufferedImage image = null;

		// Add or duplicate video frames if video is behind audio.
		while (audioTimeMs - videoTimeMs > frameStepMs) {
			if (isNull(image)) {
				image = convertVideoFrame(videoFrame);
			}
			muxVideoImage(image);

			videoTimeMs += frameStepMs;
		}

		// Compare timestamps again, since video may be behind audio again.
		if (timestampMs - videoTimeMs > frameStepMs) {
			if (isNull(image)) {
				image = convertVideoFrame(videoFrame);
			}
			muxVideoImage(image);
		}
	}

	/**
	 * Converts a WebRTC video frame to a properly formatted BufferedImage.
	 * <p>
	 * This method performs several transformations on the input video frame:
	 * <ul>
	 *   <li>Resizes the frame to match the configured output dimensions</li>
	 *   <li>Performs byte-to-int type conversion for proper image encoding</li>
	 *   <li>Centers the image horizontally and vertically in the output frame</li>
	 * </ul>
	 *
	 * @param videoFrame The WebRTC video frame to be converted.
	 *
	 * @return A BufferedImage formatted for video recording with proper dimensions and type.
	 *
	 * @throws Exception If an error occurs during the video frame conversion process.
	 */
	private BufferedImage convertVideoFrame(VideoFrame videoFrame) throws Exception {
		int width = (int) outputSize.getWidth();
		int height = (int) outputSize.getHeight();

		// Valid frame sizes are when width and height are divisible by 2.
		bufferedImage = VideoFrameConverter.convertVideoFrame(videoFrame, bufferedImage, width, height);

		// Need to perform type (byte-to-int) conversion and center image
		// vertically and horizontally.
		BufferedImage converted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Center resized frame.
		int x = 0;
		int y = 0;

		if (bufferedImage.getWidth() < converted.getWidth()) {
			x = (converted.getWidth() - bufferedImage.getWidth()) / 2;
		}
		if (bufferedImage.getHeight() < converted.getHeight()) {
			y = (converted.getHeight() - bufferedImage.getHeight()) / 2;
		}

		Graphics2D g2d = converted.createGraphics();
		g2d.drawImage(bufferedImage, x, y, null);
		g2d.dispose();

		return converted;
	}

	/**
	 * Adds a video frame to the recording muxer and updates frame count.
	 * <p>
	 * This method takes a processed BufferedImage, sends it to the video muxer
	 * for encoding, and increments the internal frame counter to maintain
	 * proper frame sequencing and timing.
	 *
	 * @param image The BufferedImage to be added to the video stream.
	 *
	 * @throws IOException If an error occurs while adding the frame to the muxer.
	 */
	private void muxVideoImage(BufferedImage image) throws IOException {
		muxer.addVideoFrame(image);

		frames++;
	}

	private void initMuxer() throws ExecutableException {
		String title = shareContext.getSource().getTitle();
		String date = dateFormat.format(new Date());

		// Sanitize title to be suitable for file names.
		title = FileSystem.getCurrent().toLegalFileName(title, '_');

		outputPath = Paths.get(context.getConfiguration().getAudioConfig()
				.getRecordingPath(), title + "-" + date + ".mp4");

		VideoRenderConfiguration vRenderConfig = new VideoRenderConfiguration();
		vRenderConfig.setBitrate(5000);
		vRenderConfig.setFrameRate(shareContext.getProfile().getFramerate());
		vRenderConfig.setDimension(outputSize);
		vRenderConfig.setCodecID(CodecID.H264);

		AudioRenderConfiguration aRenderConfig = new AudioRenderConfiguration();
		aRenderConfig.setBitrate(AVDefaults.AUDIO_BITRATES[7]);
		aRenderConfig.setCodecID(CodecID.AAC);
		aRenderConfig.setOutputFormat(getAudioFormat());

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat("mp4");
		renderConfig.setOutputFile(outputPath.toFile());
		renderConfig.setAudioConfig(aRenderConfig);
		renderConfig.setVideoConfig(vRenderConfig);

		muxer = new FFmpegMuxer(renderConfig);
		muxer.start();
	}

	private void initAudioRecorder() {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		AudioFormat audioFormat = getAudioFormat();
		String deviceName = audioConfig.getCaptureDeviceName();
		Double deviceVolume = audioConfig.getRecordingVolume(deviceName);
		double masterVolume = audioConfig.getMasterRecordingVolume();
		double volume = nonNull(deviceVolume) ? deviceVolume : masterVolume;

		final ByteArrayAudioSink audioSink = new ByteArrayAudioSink() {

			/**
			 * The number of bytes processed per second based on the audio format.
			 * Used for calculating timestamps from the byte count.
			 */
			final float bytesPerSec = AudioUtils.getBytesPerSecond(audioFormat);

			/**
			 * Counter tracking the total number of bytes processed by the audio sink.
			 * Used to calculate the current timestamp in milliseconds for synchronization.
			 */
			int bytesConsumed = 0;


			@Override
			public int write(byte[] data, int offset, int length) throws IOException {
				// Accumulate the total number of audio bytes processed so far.
				bytesConsumed += length;
				// Convert bytes processed to timestamp in milliseconds for audio/video synchronization.
				timestampMs = (int) (bytesConsumed / bytesPerSec * 1000);

				muxer.addAudioFrame(data, offset, length);

				return length;
			}
		};
		audioSink.setAudioFormat(audioFormat);

		audioRecorder = audioSystemProvider.createAudioRecorder();
		audioRecorder.setAudioProcessingSettings(
				audioConfig.getRecordingProcessingSettings());
		audioRecorder.setAudioDeviceName(deviceName);
		audioRecorder.setAudioVolume(volume);
		audioRecorder.setAudioSink(audioSink);
	}

	/**
	 * Gets the audio format used for recording.
	 * Retrieves the recording format from the audio configuration and
	 * ensures it has 2 channels, while preserving the original encoding and sample rate.
	 *
	 * @return AudioFormat with the configured encoding, sample rate, and 2 channels.
	 */
	private AudioFormat getAudioFormat() {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		AudioFormat configFormat = audioConfig.getRecordingFormat();

		return new AudioFormat(configFormat.getEncoding(), configFormat.getSampleRate(), 2);
	}
}
