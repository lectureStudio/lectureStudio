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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Singleton;

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
import org.lecturestudio.media.config.AudioRenderConfiguration;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;
import org.lecturestudio.media.video.AVDefaults;
import org.lecturestudio.media.video.FFmpegProcessMuxer;
import org.lecturestudio.media.video.VideoMuxer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;

@Singleton
public class ScreenRecorderService extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(ScreenRecorderService.class);

	private final DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");

	private final PresenterContext context;

	private final Dimension2D outputSize = new Dimension2D(1280, 960);

	private final AudioSystemProvider audioSystemProvider;

	private AudioRecorder audioRecorder;

	private VideoMuxer muxer;

	private ByteArrayAudioSink audioSink;

	private Path outputPath;

	private Path outputVideoPath;

	private ScreenShareContext shareContext;

	private BufferedImage bufferedImage;

	private int timestampMs;

	private int frames;


	public ScreenRecorderService(PresenterContext context,
			AudioSystemProvider audioSystemProvider) {
		this.context = context;
		this.audioSystemProvider = audioSystemProvider;
	}

	public ScreenShareContext getScreenShareContext() {
		return shareContext;
	}

	public void setScreenShareContext(ScreenShareContext shareContext) {
		this.shareContext = shareContext;
	}

	@Subscribe
	public void onEvent(LocalScreenVideoFrameEvent event) {
		if (!started()) {
			return;
		}

		try {
			addVideoFrame(event.getFrame());
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

		initMuxer();
		initAudioRecorder();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		audioRecorder.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		context.getEventBus().unregister(this);

		audioRecorder.stop();
		audioRecorder.destroy();

		muxer.stop();
		muxer.destroy();

		// Write recorded audio into the video.
		try {
			flushAudio();
		}
		catch (Exception e) {
			LOG.error("Flush recorded audio failed", e);
		}

		// Delete temporary video file.
		try {
			Files.deleteIfExists(outputVideoPath);
		}
		catch (IOException e) {
			LOG.error("Delete temporary capture file failed", e);
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		audioRecorder.suspend();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

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

	private BufferedImage convertVideoFrame(VideoFrame videoFrame) throws Exception {
		int width = (int) outputSize.getWidth();
		int height = (int) outputSize.getHeight();

		// Valid frame sizes are when width and height are divisible by 2.
		bufferedImage = VideoFrameConverter.convertVideoFrame(videoFrame,
				bufferedImage, width, height);

		// Need to perform type (byte to int) conversion and center image
		// vertically and horizontally.
		BufferedImage converted = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

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
		outputVideoPath = Paths.get(context.getConfiguration().getAudioConfig()
				.getRecordingPath(), title + "-temp-" + date + ".mp4");

		VideoRenderConfiguration vRenderConfig = new VideoRenderConfiguration();
		vRenderConfig.setBitrate(shareContext.getProfile().getBitrate());
		vRenderConfig.setFrameRate(shareContext.getProfile().getFramerate());
		vRenderConfig.setDimension(outputSize);
		vRenderConfig.setCodecID(CodecID.H264);

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat("mp4");
		renderConfig.setOutputFile(outputVideoPath.toFile());
		renderConfig.setAudioConfig(null);
		renderConfig.setVideoConfig(vRenderConfig);

		muxer = new FFmpegProcessMuxer(renderConfig);
		muxer.start();
	}

	private void initAudioRecorder() {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		AudioFormat audioFormat = audioConfig.getRecordingFormat();

		String deviceName = audioConfig.getCaptureDeviceName();
		Double deviceVolume = audioConfig.getRecordingVolume(deviceName);
		double masterVolume = audioConfig.getMasterRecordingVolume();
		double volume = nonNull(deviceVolume) ? deviceVolume : masterVolume;

		audioSink = new ByteArrayAudioSink() {

			final float bytesPerSec = AudioUtils.getBytesPerSecond(audioFormat);

			int bytesConsumed = 0;


			@Override
			public int write(byte[] data, int offset, int length) throws IOException {
				bytesConsumed += length;
				timestampMs = (int) (bytesConsumed / bytesPerSec * 1000);

				return super.write(data, offset, length);
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

	private void flushAudio() throws Exception {
		AudioRenderConfiguration aRenderConfig = new AudioRenderConfiguration();
		aRenderConfig.setInputFormat(audioSink.getAudioFormat());
		aRenderConfig.setVideoInputFile(outputVideoPath.toFile());
		aRenderConfig.setBitrate(AVDefaults.AUDIO_BITRATES[3]);
		aRenderConfig.setCodecID(CodecID.AAC);
		aRenderConfig.setOutputFormat(audioSink.getAudioFormat());
		aRenderConfig.setVBR(false);

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat("mp4");
		renderConfig.setOutputFile(outputPath.toFile());
		renderConfig.setAudioConfig(aRenderConfig);
		renderConfig.setVideoConfig(null);

		byte[] stream = audioSink.toByteArray();

		VideoMuxer muxer = new FFmpegProcessMuxer(renderConfig);
		muxer.start();
		muxer.addAudioFrame(stream, 0, stream.length);
		muxer.stop();
		muxer.destroy();
	}
}
