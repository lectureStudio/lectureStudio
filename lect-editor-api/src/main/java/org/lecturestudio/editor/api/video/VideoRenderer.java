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

import static java.util.Objects.nonNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;
import org.lecturestudio.editor.api.recording.RecordingRenderState;
import org.lecturestudio.media.config.AudioRenderConfiguration;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;
import org.lecturestudio.swing.DefaultRenderContext;

public class VideoRenderer extends RecordingExport {

	private static final Logger LOG = LogManager.getLogger(VideoRenderer.class);

	private final ApplicationContext context;

	private final RenderConfiguration config;

	private final Recording recording;

	private VideoEventExecutor eventExecutor;

	private VideoMuxer muxer;

	private RenderConfiguration runningConfig;

	private Consumer<BufferedImage> videoFrameConsumer;


	public VideoRenderer(ApplicationContext context, Recording recording, RenderConfiguration config) {
		this.context = context;
		this.recording = recording;
		this.config = config;
	}

	public void setOnVideoFrame(Consumer<BufferedImage> frameConsumer) {
		videoFrameConsumer = frameConsumer;
	}

	@Override
	protected void initInternal() {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			if (config.getVideoConfig().getTwoPass()) {
				startPass1();
			}
			else {
				RenderConfiguration renderConfig = new RenderConfiguration();
				renderConfig.setAudioConfig(null);
				renderConfig.setVideoConfig(config.getVideoConfig());
				renderConfig.setFileFormat(config.getFileFormat());
				renderConfig.setOutputFile(createTempFile(config.getOutputFile()));

				renderVideo(renderConfig);
			}
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (nonNull(eventExecutor) && !eventExecutor.stopped()) {
			eventExecutor.stop();
			eventExecutor = null;
		}
		if (nonNull(muxer) && muxer.getState() != ExecutableState.Stopped) {
			muxer.stop();
			muxer = null;
		}

		if (nonNull(runningConfig.getOutputFile())) {
			runningConfig.getOutputFile().delete();
		}

		deleteProfile();
		deleteTempFile();
	}

	@Override
	protected void destroyInternal() {

	}

	private void startPass1() throws Exception {
    	SecureRandom random = new SecureRandom();
		String profileName = String.format("%s-%d", "twopass", random.nextLong());
		String profilePath = context.getDataLocator().toAppDataPath("media" + File.separator + profileName);
		
		RenderConfiguration renderConfig = new RenderConfiguration();
    	renderConfig.setFileFormat(config.getFileFormat());
    	// First pass only outputs to the profile.
    	renderConfig.setOutputFile(null);
    	renderConfig.setVideoConfig(config.getVideoConfig());
    	renderConfig.setAudioConfig(null);
		
		VideoRenderConfiguration videoConfig = renderConfig.getVideoConfig();
		videoConfig.setPass(1);
		videoConfig.setTwoPassProfilePath(profilePath);
    	
		renderVideo(renderConfig);
    }
    
    private void startPass2() throws Exception {
    	RenderConfiguration renderConfig = new RenderConfiguration();
    	renderConfig.setFileFormat(config.getFileFormat());
    	renderConfig.setOutputFile(createTempFile(config.getOutputFile()));
    	renderConfig.setVideoConfig(config.getVideoConfig());
		renderConfig.setAudioConfig(null);
    	
    	VideoRenderConfiguration videoConfig = renderConfig.getVideoConfig();
    	videoConfig.setPass(2);
    	
		renderVideo(renderConfig);
    }

	private void renderVideo(RenderConfiguration config) throws Exception {
		runningConfig = config;

		VideoRenderConfiguration videoConfig = config.getVideoConfig();
		EditorContext renderContext = new EditorContext(null, null,
				context.getConfiguration(), context.getDictionary(),
				new EventBus(), new EventBus());

		if (videoConfig.getTwoPass()) {
			if (videoConfig.getPass() == 1) {
				onRenderState(RecordingRenderState.PASS_1);
			}
			else if (videoConfig.getPass() == 2) {
				onRenderState(RecordingRenderState.PASS_2);
			}
		}
		else {
			onRenderState(RecordingRenderState.RENDER_VIDEO);
		}

		muxer = new FFmpegProcessMuxer(config);
		muxer.start();

		Document recDocument = recording.getRecordedDocument().getDocument();
		Document document = new Document(recDocument.getPdfDocument());

		for (int i = 0; i < recDocument.getPageCount(); i++) {
			Page recPage = recDocument.getPage(i);
			Page page = document.getPage(i);

			page.addShapes(recPage.getShapes());
		}

		DocumentService documentService = new DocumentService(renderContext);
		documentService.getDocuments().add(document);

		ToolController toolController = new ToolController(renderContext, documentService);

		VideoRendererView renderView = new VideoRendererView(renderContext, videoConfig.getDimension());
		renderView.setRenderController(new RenderController(renderContext, new DefaultRenderContext()));

		eventExecutor = new VideoEventExecutor(renderView, toolController, renderContext.getEventBus());
		eventExecutor.setDocument(documentService.getDocuments().getSelectedDocument());
		eventExecutor.setRecordedPages(recording.getRecordedEvents().getRecordedPages());
		eventExecutor.setDuration((int) recording.getRecordedAudio().getAudioStream().getLengthInMillis());
		eventExecutor.setFrameConsumer(this::onVideoFrame);
		eventExecutor.setFrameRate(videoConfig.getFrameRate());
		eventExecutor.start();

		if (eventExecutor.started()) {
			onEventExecutorFinish();
		}
	}

	private void renderAudio() throws Exception {
		onRenderState(RecordingRenderState.RENDER_AUDIO);

		RandomAccessAudioStream stream = recording.getRecordedAudio().getAudioStream().clone();

		RecordedEvents recEvents = recording.getRecordedEvents();
		List<RecordedPage> pageList = recEvents.getRecordedPages();
		Iterator<RecordedPage> pageIter = pageList.iterator();
		RecordedPage recPage = pageIter.next();

		RenderConfiguration renderConfig = new RenderConfiguration();
		renderConfig.setFileFormat(config.getFileFormat());
		renderConfig.setOutputFile(config.getOutputFile());
		renderConfig.setAudioConfig(config.getAudioConfig());
		renderConfig.setVideoConfig(null);

		AudioRenderConfiguration audioConfig = renderConfig.getAudioConfig();
		audioConfig.setInputFormat(stream.getAudioFormat());
		audioConfig.setVideoInputFile(runningConfig.getOutputFile());

		VideoMuxer muxer = new FFmpegProcessMuxer(renderConfig);
		muxer.start();

		Time totalTime = new Time(stream.getLengthInMillis());
		Time progressTime = new Time(0);

		RecordingRenderProgressEvent progressEvent = new RecordingRenderProgressEvent();
		progressEvent.setCurrentTime(progressTime);
		progressEvent.setTotalTime(totalTime);
		progressEvent.setPageCount(pageList.size());
		progressEvent.setPageNumber(recPage.getNumber() + 1);

		if (pageIter.hasNext()) {
			recPage = pageIter.next();
		}

		byte[] buffer = new byte[8192];
		long totalSize = stream.getLength();
		long totalRead = 0;
		long currentMs;
		int read;

		while ((read = stream.read(buffer)) > 0) {
			muxer.addAudioFrame(buffer, 0, read);

			totalRead += read;

			// Progress update.
			currentMs = totalRead * totalTime.getMillis() / totalSize;

			progressTime.setMillis(currentMs);

			if (recPage.getTimestamp() < currentMs) {
				progressEvent.setPageNumber(recPage.getNumber() + 1);

				if (pageIter.hasNext()) {
					recPage = pageIter.next();
				}
			}

			onRenderProgress(progressEvent);
		}

		muxer.stop();
		stream.close();

		deleteProfile();
		deleteTempFile();
	}

	private void onVideoFrame(BufferedImage image, RecordingRenderProgressEvent event) {
		try {
			muxer.addVideoFrame(image);

			if (nonNull(videoFrameConsumer)) {
				videoFrameConsumer.accept(image);
			}

			onRenderProgress(event);
		}
		catch (IOException e) {
			LOG.error("Mux video frame failed", e);
		}
	}

	private void onEventExecutorFinish() {
		try {
			muxer.stop();

			VideoRenderConfiguration videoConfig = config.getVideoConfig();

			if (videoConfig.getTwoPass()) {
				if (videoConfig.getPass() == 1) {
					// Start second pass.
					startPass2();
				}
				else if (videoConfig.getPass() == 2) {
					renderAudio();
				}
			}
			else {
				renderAudio();
			}
		}
		catch (Exception e) {
			LOG.error("Close muxer failed", e);
		}
	}

	private void deleteProfile() {
		VideoRenderConfiguration videoConfig = runningConfig.getVideoConfig();
		String path = videoConfig.getTwoPassProfilePath();

		if (path != null) {
			path += "-0.log";

			try {
				Files.deleteIfExists(Paths.get(path));
				Files.deleteIfExists(Paths.get(path + ".mbtree"));
			}
			catch (IOException e) {
				LOG.error("Delete profile failed.", e);
			}
		}
	}

	private void deleteTempFile() {
		File inputFile = runningConfig.getOutputFile();

		if (inputFile != null) {
			inputFile.delete();
		}
	}

	private File createTempFile(File outputFile) throws IOException {
		String outputPath = outputFile.getAbsolutePath();
		String outputName = outputFile.getName();
		String format = config.getFileFormat();

		if (outputName.lastIndexOf(".") > 0) {
			outputName = outputName.substring(0, outputName.lastIndexOf("."));
		}

		String outputDir = outputPath.substring(0, outputPath.lastIndexOf(File.separator) + 1);

		return File.createTempFile(outputName + "-temp-", "." + format, new File(outputDir));
	}

}
