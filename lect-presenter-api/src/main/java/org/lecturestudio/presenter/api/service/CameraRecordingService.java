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
import dev.onvoid.webrtc.media.video.VideoFrame;
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
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;
import org.lecturestudio.media.video.FFmpegProcessMuxer;
import org.lecturestudio.presenter.api.config.CameraRecordingConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.exceptions.CameraRecordingException;
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

	private final PresenterContext context;

	private final CameraRecordingConfiguration cameraRecordingConfiguration;

	private final CameraService camService;

	private final FileLectureRecorder recorder;

	private Camera camera;

	private FFmpegProcessMuxer muxer;

	private WebRtcStreamService streamService;

	private Path outputVideoPath;

	private long frames;

	private boolean isMuxerInit = false;

	private ExecutableState streamingState = ExecutableState.Created;

	private CameraFormat closestFormat;

	private Dimension2D fixedOutputSize;

	private BufferedImage bufferedImage;

	@Inject
	public CameraRecordingService(PresenterContext context, CameraService camService, FileLectureRecorder recorder) {
		this.context = context;
		this.cameraRecordingConfiguration = context.getConfiguration().getCameraRecordingConfig();
		this.camService = camService;
		this.recorder = recorder;
		context.getEventBus().register(this);
	}

	public void setWebRTC(WebRtcStreamService streamService) {
		this.streamService = streamService;
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		streamingState = event.getState();
	}

	/**
	 * Captures the video frames of the camera, when a recording is started during a stream. And adds them to the muxer.
	 * @param event
	 */
	@Subscribe
	public void onEvent(final LocalVideoFrameEvent event) {
		if (!started()) return;
		var frame = event.getFrame();
		try {
			BufferedImage image;
			image = convertVideoFrame(frame);
			manageVideoFrameAddition(image);
		}
		catch (Exception e) {
			LOG.error("Conversion of video frame failed.", e);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
	}

	/**
	 * Starts the image capturing process by adding the converted bufferedImages, which are imported from the camera, to the muxer.
	 * @throws ExecutableException
	 */
	@Override
	protected void startInternal() throws ExecutableException {
		streamService.setCaptureLocalCameraVideo(true);
		if (!isMuxerInit) {
			if(true){
				fixedOutputSize = new Dimension2D(1152, 648);
			}else{
				fixedOutputSize = new Dimension2D(1152, 864);
			}
			this.camera = camService.getCamera(cameraRecordingConfiguration.getCameraName());
			overwriteCameraRecordingConfigurationWithStreamConfiguration(context.getConfiguration().getStreamConfig());
			this.closestFormat = getNearestCameraFormat(camera, cameraRecordingConfiguration.getCameraFormat());
			frames = 0;
			initMuxer();
			isMuxerInit = true;
		}

		if (streamingState != ExecutableState.Started) {
			camera.setFormat(new CameraFormat(closestFormat.getWidth(), closestFormat.getHeight(), camera.getFormat().getFrameRate()));
			camera.setImageSize(new Dimension2D(fixedOutputSize.getWidth(), fixedOutputSize.getHeight()));
			camera.setImageConsumer(bufferedImage -> {
				try {
					manageVideoFrameAddition(bufferedImage);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			try {
				camera.open();
			}
			catch (Exception e) {
				throw new CameraRecordingException("Open camera failed.", e);
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (camera == null) return;
		try {
			camera.close();
		}
		catch (Exception e) {
			throw new CameraRecordingException("Stop camera failed.", e);
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (camera == null) return;
		try {
			camera.close();
		}
		catch (Exception e) {
			throw new CameraRecordingException("Suspend camera failed.", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		context.getEventBus().unregister(this);
	}

	/**
	 * Initializes the muxer and sets the output path to a pre-defined location with a pre-define name.
	 * @throws ExecutableException
	 */
	private void initMuxer() throws ExecutableException {
		String title = "TempWebcamRecording";
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String date = dateFormat.format(new Date());
		outputVideoPath = Paths.get(context.getConfiguration().getAudioConfig().getRecordingPath(), title + "-" + date + ".mp4");

		VideoRenderConfiguration vRenderConfig = new VideoRenderConfiguration();
		vRenderConfig.setBitrate(cameraRecordingConfiguration.getCameraCodecConfig().getBitRate());
		vRenderConfig.setFrameRate((int) closestFormat.getFrameRate());
		vRenderConfig.setDimension(new Dimension2D(fixedOutputSize.getWidth(), fixedOutputSize.getHeight()));
		vRenderConfig.setCodecID(CodecID.H264);

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat("mp4");
		renderConfig.setOutputFile(outputVideoPath.toFile());
		renderConfig.setAudioConfig(null);
		renderConfig.setVideoConfig(vRenderConfig);

		muxer = new FFmpegProcessMuxer(renderConfig);
		muxer.start();
	}

	/**
	 * This function guarantees that the adjustments made in the settings page correctly affects both the stream configuration and the camera recording configuration by overwriting the data from camera recording configuration with the data from stream configuration.
	 *
	 * NOTE As of now changing the dimension and quality of the webcam in the settings page only affects the stream configuration.
	 * The parts of the stream configuration which instantly adopt the adjustments made in the settings page are thus copied over to the camera recording configuration here.
	 * This is to avoid adding redundant configuration data. Streamlining these camera configurations needs to be addressed in the future.
	 */
	private void overwriteCameraRecordingConfigurationWithStreamConfiguration(StreamConfiguration streamConfig) {
		CameraFormat fixedFormat = new CameraFormat((int) streamConfig.getCameraCodecConfig().getViewRect().getWidth(), (int) streamConfig.getCameraCodecConfig().getViewRect().getHeight(), streamConfig.getCameraCodecConfig().getFrameRate());
		cameraRecordingConfiguration.setCameraFormat(fixedFormat);
		cameraRecordingConfiguration.getCameraCodecConfig().setViewRect(streamConfig.getCameraCodecConfig().getViewRect());
		cameraRecordingConfiguration.getCameraCodecConfig().setBitRate(streamConfig.getCameraCodecConfig().getBitRate());
		cameraRecordingConfiguration.getCameraCodecConfig().setFrameRate(streamConfig.getCameraCodecConfig().getFrameRate());
	}

	/***
	 * Gets the nearest supported format from the camera which resembles the desired format from the input.
	 *
	 * NOTE As of now the camera format has to be determined before opening the camera. The reason for this is that the CropAndScale method of VideoFrameBuffer will throw an exception if the format either exactly matches the given camera format or does not match it at all.
	 * @param camera The camera, whose closest supported format needs to be determined.
	 * @param format The desired format.
	 * @return The closest supported format to the desired format from the input.
	 */
	private CameraFormat getNearestCameraFormat(Camera camera, CameraFormat format) {
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
			}
			else {
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

	/***
	 * Converts a video frame into a buffered image.
	 * @param videoFrame The video frame from the camera.
	 * @return The converted video frame as a buffered image.
	 * @throws Exception
	 */
	private BufferedImage convertVideoFrame(VideoFrame videoFrame) throws Exception {
		int width = (int) fixedOutputSize.getWidth();
		int height = (int) fixedOutputSize.getHeight();

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

	/***
	 * Takes in a frame, converts it and adds it to the muxer.
	 * @param bufferedImage The image to be added to the muxer.
	 */
	private void addVideoFrameToMuxer(BufferedImage bufferedImage) {
		try {
			BufferedImage converted = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = converted.createGraphics();
			g2d.drawImage(bufferedImage, 0, 0, null);
			g2d.dispose();
			muxer.addVideoFrame(converted);
			frames++;
		}
		catch (IOException e) {
			LOG.error("Start video image rendering failed", e);
		}
	}

	/***
	 *  Manages how many frames are added to the muxer, by determining the outputted frame rate of the camera and the desired frame rate which has been configured.
	 * @param bufferedImage The image to be added to the muxer.
	 * @throws Exception
	 */
	private void manageVideoFrameAddition(BufferedImage bufferedImage) throws Exception {
		// Check audio/video timestamp drift.
		int audioTimeMs = (int) recorder.getElapsedTime();
		float currentFps = frames / (audioTimeMs / 1000f);

		int frameStepMs = (int) (1000 / closestFormat.getFrameRate());
		int videoTimeMs = (int) (frames / closestFormat.getFrameRate() * 1000);

		// Drop video frame if ahead of audio.
		if (currentFps > closestFormat.getFrameRate()) {
			return;
		}

		// Add or duplicate video frames if video is behind audio.
		while (audioTimeMs - videoTimeMs > frameStepMs) {
			addVideoFrameToMuxer(bufferedImage);

			videoTimeMs += frameStepMs;
		}

		// Compare timestamps again, since video may be behind audio again.
		if ((int) recorder.getElapsedTime() - videoTimeMs > frameStepMs) {
			addVideoFrameToMuxer(bufferedImage);
		}
	}

	/**
	 * The video file, which has been saved to a pre-defined location with a pre-defined name, gets renamed to match the .presenter file name. If this can't be done or the video file is under 1 second long, the video file gets deleted.
	 * @param file The video file, which has been recorded by the camera
	 * @throws ExecutableException
	 */
	public void finishCameraRecordingProcess(File file) throws ExecutableException {
		muxer.stop();
		muxer.destroy();
		isMuxerInit = false;
		if (closestFormat.getFrameRate() == 0 || frames * 1.0 / closestFormat.getFrameRate() < 1.0) {
			try {
				Files.delete(outputVideoPath);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			try {
				Files.move(outputVideoPath, getSafeVideoFileDestinationPath(file));
			}
			catch (IOException e) {
				try {
					Files.delete(outputVideoPath);
				}
				catch (IOException ex) {
					throw new ExecutableException(ex);
				}
				throw new ExecutableException(e);
			}
		}
	}

	/**
	 * Determines a valid name for the camera recording in the root of the given file and converts it into a path. If this path is already occupied, another name is determined. This process repeats a finite amount of times until a valid destination path is found.
	 * @param file The presenter file, which has been saved to a distinct location.
	 * @return A video file target path that is not occupied by another file.
	 */
	private Path getSafeVideoFileDestinationPath(File file) {
		if (!Files.exists(Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam.mp4"))) {
			return Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam.mp4");
		}
		int attemptCounter = 1;
		final int MAX_ATTEMPTS = 100;
		while (attemptCounter < MAX_ATTEMPTS) {
			if (!Files.exists(Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam(" + attemptCounter + ").mp4"))) {
				return Paths.get(FilenameUtils.getFullPath(file.getPath()) + FilenameUtils.getBaseName(file.getPath()) + "-webcam(" + attemptCounter + ").mp4");
			}
			else {
				attemptCounter++;
			}
		}
		return null;
	}
}
