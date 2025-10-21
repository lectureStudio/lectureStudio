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

package org.lecturestudio.editor.api.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.recording.file.RecordingUtils;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderState;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;

/**
 * Exports a recording to web-based vector format. This exporter creates a standalone
 * HTML/JS-based vector representation of the recording that can be viewed in web browsers.
 *
 * @author Alex Andres
 */
public class WebVectorExport extends RecordingExport {

	/** Path to the HTML template file used for the export. */
	private static final String TEMPLATE_FILE = "resources/export/web/vector/index.html";

	/** Map containing template variables to be replaced in the output HTML. */
	private final Map<String, String> data = new HashMap<>();

	/** The recording to export. */
	private final Recording recording;

	/** Configuration for the rendering process. */
	private final RenderConfiguration config;

	/** Path where the export files will be written. */
	private String outputPath;


	/**
	 * Creates a new web vector export for the specified recording.
	 *
	 * @param recording The recording to export.
	 * @param config The configuration for the rendering process.
	 */
	public WebVectorExport(Recording recording, RenderConfiguration config) {
		this.recording = recording;
		this.config = config;
	}

	/**
	 * Sets the title of the exported presentation.
	 *
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		data.put("title", title);
	}

	/**
	 * Sets the presenter name for the exported presentation.
	 *
	 * @param name The presenter name.
	 */
	public void setName(String name) {
		data.put("name", name);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		File outputFile = config.getOutputFile();
		String outputPathStr = FileUtils.stripExtension(outputFile.getPath());
		Path outputRootPath = Paths.get(outputPathStr).getParent();
		String outputFileName = FileUtils.stripExtension(outputFile.getName());
		File outputFolder = outputRootPath.resolve(outputFileName + "v").toFile();

		if (!outputFolder.exists() && !outputFolder.mkdirs()) {
			throw new ExecutableException("Failed to create output directory: " + outputFolder);
		}

		outputPath = outputFolder.getAbsolutePath();

		setTitle(FileUtils.stripExtension(outputFile.getName()));
	}

	@Override
	protected void startInternal() {
		try {
			encodeVideo();
		}
		catch (IOException e) {
			throw new CompletionException(e);
		}

		CompletableFuture.supplyAsync(() -> {
			onRenderState(RecordingRenderState.RENDER_VECTOR);

			RecordedAudio encAudio;

			try {
				encAudio = encodeAudio();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			return encAudio;
		})
		.thenAccept(encAudio -> {
			if (stopped()) {
				return;
			}

			Recording encRecording = new Recording();
			encRecording.setRecordingHeader(recording.getRecordingHeader());
			encRecording.setRecordedEvents(recording.getRecordedEvents());
			encRecording.setRecordedAudio(encAudio);
			encRecording.setRecordedDocument(recording.getRecordedDocument());

			try {
				byte[] rec = RecordingFileWriter.writeToByteArray(encRecording);
				String recEncoded = Base64.getEncoder().encodeToString(rec);

				data.put("recording", recEncoded);

				String indexContent = loadTemplateFile();
				indexContent = processTemplateFile(indexContent, data);

				try {
					File outputFile = config.getOutputFile();
					String outputFileName = FileUtils.stripExtension(outputFile.getName());

					writeTemplateFile(indexContent, getFile(outputFileName + "." + FileUtils.getExtension(TEMPLATE_FILE)));

					File recFile = getFile(outputFileName + ".plr");
					Files.write(recFile.toPath(), rec);
				}
				catch (Exception e) {
					throw new ExecutableException(e);
				}

				stop();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(throwable -> {
			LOG.error("HTML vector export failed", throwable);
			return null;
		});
	}

	@Override
	protected void stopInternal() {

	}

	@Override
	protected void destroyInternal() {

	}

	private File getFile(String file) {
		return Paths.get(outputPath, file).toFile();
	}

	private RecordedAudio encodeAudio() throws IOException {
		RecordedAudio recAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recAudio.getAudioStream().clone();
		AudioFormat sourceFormat = AudioUtils.createAudioFormat(stream.getAudioFormat());
		int sampleRate = (int) sourceFormat.getSampleRate();
		int channels = sourceFormat.getChannels();

		ProgressListener progressListener = new ProgressListener(recording, sourceFormat);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, channels);
		recorder.setAudioBitrate(16000);
		recorder.setAudioCodec(avcodec.AV_CODEC_ID_OPUS);
		recorder.setSampleRate(48000);
		recorder.setFormat("opus");

		byte[] buffer = new byte[8192];
		int readTotal = 0;
		int read;

		try {
			recorder.start();

			// Write silenced audio at the beginning to avoid eventual clicks.
			byte[] silenced = org.lecturestudio.core.audio.AudioUtils.silenceAudio(stream, stream.getAudioFormat(), 20);
			writeAudioSamples(recorder, silenced, silenced.length, sampleRate, channels);
			readTotal += silenced.length;

			while ((read = stream.read(buffer)) > 0) {
				writeAudioSamples(recorder, buffer, read, sampleRate, channels);

				readTotal += read;
				progressListener.accept(readTotal);
			}

			stream.close();

			recorder.stop();
			recorder.release();
		}
		catch (FFmpegFrameRecorder.Exception e) {
			throw new IOException(e);
		}

		ByteArrayInputStream encodedStream = new ByteArrayInputStream(
				outStream.toByteArray());

		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(
				new DynamicInputStream(encodedStream), true);

		return new RecordedAudio(audioStream);
	}

	private void writeAudioSamples(FFmpegFrameRecorder recorder, byte[] buffer, int read, int sampleRate, int channels)
			throws FFmpegFrameRecorder.Exception {
		int nSamplesRead = read / 2;
		short[] shortSamples = new short[nSamplesRead];

		ByteBuffer.wrap(buffer, 0, read).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortSamples);
		ShortBuffer samplesBuffer = ShortBuffer.wrap(shortSamples, 0, nSamplesRead);

		recorder.recordSamples(sampleRate, channels, samplesBuffer);
	}

	private void encodeVideo() throws IOException {
		List<ScreenAction> screenActions = RecordingUtils.getScreenActions(recording);
		if (screenActions.isEmpty()) {
			return;
		}

		Path sourcePath = Paths.get(recording.getSourceFile().getParent());
		Set<Path> videoFiles = new HashSet<>();

		StringBuilder builder = new StringBuilder();
		builder.append("{");

		for (ScreenAction action : screenActions) {
			Path videoFile = sourcePath.resolve(action.getFileName());
			if (Files.exists(videoFile) && !videoFiles.contains(videoFile)) {
				String base64Video = encodeFileToBase64(videoFile);

				builder.append("\"").append(action.getFileName()).append("\": ");
				builder.append("\"").append("data:video/mp4;base64,").append(base64Video).append("\"");

				videoFiles.add(videoFile);
			}
		}

		builder.append("}");

		data.put("videoMapping", builder.toString());
	}

	private byte[] transcodeVideo(Path videoFile) throws FFmpegFrameRecorder.Exception {
		ByteArrayOutputStream outputVideoFile = new ByteArrayOutputStream();
		VideoRenderConfiguration renderConfig = config.getVideoConfig();

		FFmpegLogCallback.set();

		FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault("dump.mp4", 1920,
				1080);
		recorder.setFrameRate(renderConfig.getFrameRate());
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setVideoBitrate(renderConfig.getBitrate() * 1000);
		recorder.setImageHeight(1080);
		recorder.setImageWidth(1920);
		recorder.setFormat(config.getFileFormat());
//			recorder.setVideoOptions(videoGrabber.getVideoOptions());
//			recorder.setVideoMetadata(videoGrabber.getVideoMetadata());
		recorder.start();

		try (FFmpegFrameGrabber videoGrabber = FFmpegFrameGrabber.createDefault(videoFile.toFile())) {
			videoGrabber.start();

			Frame frame;
			while ((frame = videoGrabber.grabImage()) != null) {
				System.out.println(frame.imageWidth + "x" + frame.imageHeight + " " + frame.timestamp);
				recorder.record(frame);
			}

			recorder.close();
			videoGrabber.close();

			return outputVideoFile.toByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String loadTemplateFile() {
		InputStream is = ResourceLoader.getResourceAsStream(WebVectorExport.TEMPLATE_FILE);

		if (isNull(is)) {
			throw new NullPointerException("Missing web index.html file.");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		return reader.lines().collect(Collectors.joining(System.lineSeparator()));
	}

	private String processTemplateFile(String fileContent, Map<String, String> data) {
		Pattern pattern = Pattern.compile("\"#\\{(.+?)}\"");
		Matcher matcher = pattern.matcher(fileContent);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String match = matcher.group(1);
			String replacement = data.get(match);

			if (nonNull(replacement)) {
				matcher.appendReplacement(sb, "'" + replacement + "'");
			}
			else {
				LOG.warn("Found match '{}' with no replacement.", match);
			}
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private void writeTemplateFile(String fileContent, File outputFile) throws IOException {
		Path path = Paths.get(outputFile.getPath());
		Files.writeString(path, fileContent);
	}

	private String encodeFileToBase64(Path path) throws IOException {
		try {
			byte[] fileContent = Files.readAllBytes(path);
			return Base64.getEncoder().encodeToString(fileContent);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}



	private class ProgressListener implements Consumer<Integer> {

		private final Time progressTime;

		private final RecordingRenderProgressEvent event;

		private final Iterator<RecordedPage> pageIter;

		private RecordedPage recPage;

		double bytesPerMs;


		ProgressListener(Recording recording, AudioFormat targetFormat) {
			double bytesPerSecond = Math.round(targetFormat.getSampleRate() *
					targetFormat.getFrameSize() * targetFormat.getChannels());
			this.bytesPerMs = bytesPerSecond / 1000;

			RecordedAudio recAudio = recording.getRecordedAudio();
			RandomAccessAudioStream stream = recAudio.getAudioStream();
			RecordedEvents recEvents = recording.getRecordedEvents();
			List<RecordedPage> pageList = recEvents.getRecordedPages();

			pageIter = pageList.iterator();
			recPage = pageIter.next();

			progressTime = new Time(0);

			event = new RecordingRenderProgressEvent();
			event.setTotalTime(new Time(stream.getLengthInMillis()));
			event.setCurrentTime(progressTime);
			event.setPageCount(pageList.size());
			event.setPageNumber(recPage.getNumber() + 1);

			if (pageIter.hasNext()) {
				recPage = pageIter.next();
			}
		}

		@Override
		public void accept(Integer readTotal) {
			long currentMs = (long) (readTotal / bytesPerMs);

			progressTime.setMillis(currentMs);

			if (recPage.getTimestamp() < currentMs) {
				event.setPageNumber(recPage.getNumber() + 1);

				if (pageIter.hasNext()) {
					recPage = pageIter.next();
				}
			}

			onRenderProgress(event);
		}
	}
}
