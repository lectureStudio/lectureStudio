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

import com.google.common.eventbus.Subscribe;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.codec.CodecID;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;
import org.lecturestudio.media.video.FFmpegProcessMuxer;
import org.lecturestudio.presenter.api.config.CameraRecordingConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.exceptions.CameraRecordingException;
import org.lecturestudio.presenter.api.model.Stopwatch;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.web.api.event.LocalVideoFrameEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class CameraRecordingService extends ExecutableBase {
	private static final Logger LOG = LogManager.getLogger(CameraRecordingService.class);
	private final CameraService camService;
	private final FileLectureRecorder recorder;
	private final PresenterContext context;
	private Camera camera;
	private final CameraRecordingConfiguration cameraRecordingConfiguration;
	private FFmpegProcessMuxer muxer;
	private WebRtcStreamService streamService;
	private final CameraFormat cameraFormat;
	private Path outputPath;
	private long frameCounter;
	private boolean isMuxerInit = false;
	private ExecutableState streamingState = ExecutableState.Created;

	@Inject
	public CameraRecordingService(PresenterContext context, CameraService camService, FileLectureRecorder recorder) {
		this.context = context;
		this.cameraRecordingConfiguration = context.getConfiguration().getCameraRecordingConfig();
		this.camService = camService;
		this.recorder = recorder;
		Rectangle2D streamCodecViewRect = context.getConfiguration().getStreamConfig().getCameraCodecConfig().getViewRect();
		double streamCodecFrameRate = context.getConfiguration().getStreamConfig().getCameraCodecConfig().getFrameRate();
		cameraFormat = new CameraFormat((int) streamCodecViewRect.getWidth(), (int) streamCodecViewRect.getHeight(), streamCodecFrameRate);
		context.getEventBus().register(this);
	}

	public void setWebRTC(WebRtcStreamService streamService) {
		this.streamService = streamService;
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		streamingState = event.getState();
	}

	@Subscribe
	public void onEvent(final LocalVideoFrameEvent event) {
		if (!started()) return;
		var frame = event.getFrame();
		try {
			BufferedImage bufferedImage;
			bufferedImage = VideoFrameConverter.convertVideoFrame(frame, null, cameraFormat.getWidth(), cameraFormat.getHeight());
			writeImageToMuxer(bufferedImage);
		} catch (Exception e) {
			LOG.error("Conversion of video frame failed.", e);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
	}

	/**
	 * Starts the image capturing process by adding the converted bufferedImages, which are imported from the camera, to the muxer.
	 * The muxer only gets initialized once at the start of the process, not on pause.
	 *
	 * @throws ExecutableException
	 */
	@Override
	protected void startInternal() throws ExecutableException {
		streamService.setCaptureLocalCameraVideo(true);
		//TODO Insert here stopwatch to calculate framerate and image dimension
		if (!isMuxerInit) {
			initMuxer();
			frameCounter = 0;
			isMuxerInit = true;
		}

		if (streamingState != ExecutableState.Started) {
			camera = camService.getCamera(cameraRecordingConfiguration.getCameraName());
			CameraFormat tempFormat = getNearestCameraFormatTEMP(camera, cameraRecordingConfiguration.getCameraFormat());
			System.out.println("StartInternal CameraRecordingConfig: " + cameraRecordingConfiguration.getCameraFormat().toString());
			System.out.println("StartInternal NearestFormat: " + tempFormat.toString());
			//camera.setFormat(tempFormat);
			camera.setImageSize(new Dimension2D(tempFormat.getWidth(), tempFormat.getHeight()));
			camera.setImageConsumer(bufferedImage -> {
				//TODO Gibt die aufgezeichnete Tonspur in ms aufnehmen
				//TODO Übernehme die logik der variablen framerate aus screenrecording
				recorder.getElapsedTime();
				writeImageToMuxer(bufferedImage);
			});
			try {
				camera.open();
			} catch (Exception e) {
				throw new CameraRecordingException("Open camera failed.", e);
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (camera == null) return;
		try {
			camera.close();
		} catch (Exception e) {
			throw new CameraRecordingException("Stop camera failed.", e);
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (camera == null) return;
		try {
			camera.close();
		} catch (Exception e) {
			throw new CameraRecordingException("Suspend camera failed.", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		context.getEventBus().unregister(this);
	}

	/**
	 * The video file, which has been saved to a pre-defined location with a pre-defined name, gets renamed to match the .presenter file name. If this can't be done the video file gets deleted.
	 *
	 * @param file The video file, which has been recorded by the camera
	 * @throws ExecutableException
	 */
	public void FinishVideoRecordingProcess(File file) throws ExecutableException {
		muxer.stop();
		muxer.destroy();
		isMuxerInit = false;
		if (cameraFormat.getFrameRate() == 0 || frameCounter * 1.0 / cameraFormat.getFrameRate() < 1.0) {
			try {
				Files.delete(outputPath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				Files.move(outputPath, getSafeVideoFileDestinationPath(file));
			} catch (IOException e) {
				try {
					Files.delete(outputPath);
				} catch (IOException ex) {
					throw new ExecutableException(ex);
				}
				throw new ExecutableException(e);
			}
		}
	}

	/**
	 * Initializes the muxer and sets the output path to a pre-defined location with a pre-define name.
	 *
	 * @throws ExecutableException
	 */
	private void initMuxer() throws ExecutableException {
		CameraFormat muxerFormat;
		String title = "TempWebcamRecording";
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String date = dateFormat.format(new Date());
		outputPath = Paths.get(context.getConfiguration().getAudioConfig().getRecordingPath(), title + "-" + date + ".mp4");
		matchCameraRecordingConfigurationToSettings();

		if (streamingState != ExecutableState.Started) {
			camera = camService.getCamera(cameraRecordingConfiguration.getCameraName());
			CameraFormat tempFormat = getNearestCameraFormatTEMP(camera, cameraRecordingConfiguration.getCameraFormat());
			muxerFormat = tempFormat;
		} else {
			muxerFormat = cameraFormat;
		}

		System.out.println("InitMuxer muxerFormat: " + muxerFormat.toString());

		VideoRenderConfiguration vRenderConfig = new VideoRenderConfiguration();
		vRenderConfig.setBitrate(600 * 1000);
		vRenderConfig.setFrameRate(15);
		vRenderConfig.setDimension(new Dimension2D(cameraRecordingConfiguration.getCameraFormat().getWidth(), cameraRecordingConfiguration.getCameraFormat().getHeight()));
		vRenderConfig.setCodecID(CodecID.H264);

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat("mp4");
		renderConfig.setOutputFile(outputPath.toFile());
		renderConfig.setAudioConfig(null);
		renderConfig.setVideoConfig(vRenderConfig);

		muxer = new FFmpegProcessMuxer(renderConfig);
		muxer.start();
	}

	/**
	 * Searches for a free destination for the video to be written to.
	 *
	 * @param file The video file, which has been recorded by the camera
	 * @return A video file target path that is not occupied by another file.
	 */
	private Path getSafeVideoFileDestinationPath(File file) {
		if (!Files.exists(Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam.mp4"))) {
			return Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam.mp4");
		}
		int counter = 1;
		final int MAX_ITERATION = 100;
		while (counter < MAX_ITERATION) {
			if (!Files.exists(Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam(" + counter + ").mp4"))) {
				return Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam(" + counter + ").mp4");
			} else {
				counter++;
			}
		}
		return null;
	}

	/***
	 * Takes a bufferedImage, converts it and adds it to the muxer.
	 * @param bufferedImage The image from the camera
	 */
	private void writeImageToMuxer(BufferedImage bufferedImage) {
		//TODO This needs to complete in 1/30 secs with puffer.
		try {
			BufferedImage converted = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = converted.createGraphics();
			g2d.drawImage(bufferedImage, 0, 0, null);
			g2d.dispose();
			muxer.addVideoFrame(converted);
			frameCounter++;
		} catch (IOException e) {
			LOG.error("Start video image rendering failed", e);
		}
	}

	/**
	 * Transfers the camera configuration data, configured in the settings page, from the streamConfiguration onto the cameraRecordingConfiguration.
	 */
	private void matchCameraRecordingConfigurationToSettings() {
		var streamConfig = context.getConfiguration().getStreamConfig();
		CameraFormat fixedFormat = new CameraFormat((int) streamConfig.getCameraCodecConfig().getViewRect().getWidth(), (int) streamConfig.getCameraCodecConfig().getViewRect().getHeight(), streamConfig.getCameraCodecConfig().getFrameRate());
		cameraRecordingConfiguration.setCameraFormat(fixedFormat);
		cameraRecordingConfiguration.getCameraCodecConfig().setViewRect(streamConfig.getCameraCodecConfig().getViewRect());
		cameraRecordingConfiguration.getCameraCodecConfig().setBitRate(streamConfig.getCameraCodecConfig().getBitRate());
		cameraRecordingConfiguration.getCameraCodecConfig().setFrameRate(streamConfig.getCameraCodecConfig().getFrameRate());
	}

	private CameraFormat getNearestCameraFormatTEMP(Camera camera, CameraFormat format) {
		CameraFormat[] formats = camera.getSupportedFormats();
		CameraFormat nearest = null;

		Point2D formatPoint = new Point2D(format.getWidth(), format.getHeight());
		Point2D tempPoint = new Point2D();
		Point2D tempPoint2 = new Point2D();

		double formatRatio = format.getHeight() / (double) format.getWidth();

		double pointDistance = Double.MAX_VALUE;
		double ratioDistance = Double.MAX_VALUE;

		for (CameraFormat f : formats) {
			tempPoint.set(f.getWidth(), f.getHeight());

			double d = formatPoint.distance(tempPoint);
			double r = f.getHeight() / (double) f.getWidth();
			double rd = Math.abs(formatRatio - r);

			if (d == 0) {
				// Perfect match.
				nearest = f;
				break;
			}
			if (pointDistance > d && rd == 0) {
				// Best match within the same aspect ratio.
				pointDistance = d;
				nearest = f;
			} else {
				// Compare point-ratio distance.
				tempPoint2.set(d, rd);

				double prd = tempPoint2.distance(new Point2D());

				if (ratioDistance > prd) {
					ratioDistance = prd;
					nearest = f;
				}
			}
		}

		return nearest;
	}
}