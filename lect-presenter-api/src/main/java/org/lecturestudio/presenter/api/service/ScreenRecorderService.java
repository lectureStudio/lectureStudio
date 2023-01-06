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

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
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
import org.lecturestudio.web.api.event.ScreenVideoFrameEvent;

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


	public ScreenRecorderService(PresenterContext context,
			AudioSystemProvider audioSystemProvider) {
		this.context = context;
		this.audioSystemProvider = audioSystemProvider;
	}

	public void setScreenShareContext(ScreenShareContext shareContext) {
		this.shareContext = shareContext;
	}

	@Subscribe
	public void onEvent(ScreenVideoFrameEvent event) {
		if (!started()) {
			return;
		}

		try {
			writeVideoFrame(event.getFrame());
		}
		catch (Exception e) {
			LOG.error("Mux video frame failed", e);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		context.getEventBus().register(this);

		String date = dateFormat.format(new Date());

		outputPath = Paths.get(context.getConfiguration().getAudioConfig()
				.getRecordingPath(), "screen-" + date + ".mp4");
		outputVideoPath = Paths.get(context.getConfiguration().getAudioConfig()
						.getRecordingPath(), "screen-video-" + date + ".mp4");

		VideoRenderConfiguration vRenderConfig = new VideoRenderConfiguration();
//		vRenderConfig.setBitrate(shareContext.getProfile().getBitrate());
//		vRenderConfig.setFrameRate(shareContext.getProfile().getFramerate());
		vRenderConfig.setBitrate(700);
		vRenderConfig.setFrameRate(30);
		vRenderConfig.setDimension(outputSize);
		vRenderConfig.setCodecID(CodecID.H264);

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat("mp4");
		renderConfig.setOutputFile(outputVideoPath.toFile());
		renderConfig.setAudioConfig(null);
		renderConfig.setVideoConfig(vRenderConfig);

		muxer = new FFmpegProcessMuxer(renderConfig);
		muxer.start();

		initAudioRecorder();
		audioRecorder.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		context.getEventBus().unregister(this);

		audioRecorder.stop();
		audioRecorder.destroy();

		muxer.stop();
		muxer.destroy();

		try {
			flushAudio();

			Files.deleteIfExists(outputVideoPath);
		}
		catch (Exception e) {
			LOG.error("Flush recorded audio failed", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private void writeVideoFrame(VideoFrame videoFrame) throws Exception {
		int width = (int) outputSize.getWidth();
		int height = (int) outputSize.getHeight();

		// Valid frame sizes are when width and height are divisible by 2.
		BufferedImage image = VideoFrameConverter.convertVideoFrame(videoFrame,
				null, width, height);

		BufferedImage converted = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		// Center resized frame.
		int x = 0;
		int y = 0;

		if (image.getWidth() < converted.getWidth()) {
			x = (converted.getWidth() - image.getWidth()) / 2;
		}
		if (image.getHeight() < converted.getHeight()) {
			y = (converted.getHeight() - image.getHeight()) / 2;
		}

		Graphics2D g2d = converted.createGraphics();
		g2d.drawImage(image, x, y, null);
		g2d.dispose();

		muxer.addVideoFrame(converted);
	}

	private void initAudioRecorder() {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();

		String deviceName = audioConfig.getCaptureDeviceName();
		Double deviceVolume = audioConfig.getRecordingVolume(deviceName);
		double masterVolume = audioConfig.getMasterRecordingVolume();
		double volume = nonNull(deviceVolume) ? deviceVolume : masterVolume;

		audioSink = new ByteArrayAudioSink();
		audioSink.setAudioFormat(audioConfig.getRecordingFormat());

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
	}
}
