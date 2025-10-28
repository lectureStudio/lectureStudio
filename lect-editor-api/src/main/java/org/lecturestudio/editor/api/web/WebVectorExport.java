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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.recording.file.RecordingUtils;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderState;
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
		File outputFolder = Paths.get(outputPathStr).getParent().toFile();

		if (!outputFolder.exists() && !outputFolder.mkdirs()) {
			throw new ExecutableException("Failed to create output directory: " + outputFolder);
		}

		outputPath = outputFolder.getAbsolutePath();

		setTitle(FileUtils.stripExtension(outputFile.getName()));
	}

	@Override
	protected void startInternal() {
		CompletableFuture.supplyAsync(() -> {
			List<ScreenAction> screenActions = RecordingUtils.getScreenActions(recording);
			if (!screenActions.isEmpty()) {
				onRenderState(RecordingRenderState.RENDER_VECTOR_VIDEO);

				try {
					encodeVideo(screenActions);
				}
				catch (IOException e) {
					throw new CompletionException(e);
				}
			}

			onRenderState(RecordingRenderState.RENDER_VECTOR_AUDIO);

			RecordedAudio encAudio;

			try {
				encAudio = encodeAudio();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			Recording encRecording = new Recording();
			encRecording.setRecordingHeader(recording.getRecordingHeader());
			encRecording.setRecordedEvents(recording.getRecordedEvents());
			encRecording.setRecordedAudio(encAudio);
			encRecording.setRecordedDocument(recording.getRecordedDocument());

			try {
				encodeRecording(encRecording);

				stop();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			return null;
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

	private void encodeRecording(Recording recording) throws Exception {
		byte[] rec = RecordingFileWriter.writeToByteArray(recording);
		String recEncoded = Base64.getEncoder().encodeToString(rec);

		data.put("recording", recEncoded);

		String indexContent = loadTemplateFile();
		indexContent = processTemplateFile(indexContent, data);

		File outputFile = config.getOutputFile();
		String outputFileName = FileUtils.stripExtension(outputFile.getName()) + "-Vector";

		writeTemplateFile(indexContent, getFile(outputFileName + "." + FileUtils.getExtension(TEMPLATE_FILE)));

		// For debugging only.
		//File recFile = getFile(outputFileName + ".plr");
		//Files.write(recFile.toPath(), rec);
	}

	private RecordedAudio encodeAudio() throws IOException {
		RecordedAudio recAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recAudio.getAudioStream().clone();
		AudioFormat sourceFormat = AudioUtils.createAudioFormat(stream.getAudioFormat());
		int sampleRate = (int) sourceFormat.getSampleRate();
		int channels = sourceFormat.getChannels();

		AudioProgressListener audioProgressListener = new AudioProgressListener(recording, sourceFormat, this);

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
				audioProgressListener.accept(readTotal);
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

	private void encodeVideo(List<ScreenAction> screenActions) throws IOException {
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
				String base64Video = encodeVideoFileToBase64(videoFile);

				builder.append("\"").append(action.getFileName()).append("\": ");
				builder.append("\"").append("data:video/mp4;base64,").append(base64Video).append("\"");

				videoFiles.add(videoFile);
			}
		}

		builder.append("}");

		data.put("videoMapping", builder.toString());
	}

	private void transcodeVideo(Path videoFile, ByteArrayOutputStream outStream) throws FFmpegFrameRecorder.Exception {
		try (FFmpegFrameGrabber videoGrabber = FFmpegFrameGrabber.createDefault(videoFile.toFile())) {
			videoGrabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
			videoGrabber.start();

			// Microseconds to milliseconds
			long duration = videoGrabber.getLengthInTime() / 1000L;
			VideoProgressListener videoProgressListener = new VideoProgressListener(this, duration);

			FFmpegFrameRecorder recorder = createVideoRecorder(videoGrabber, outStream);
			recorder.start();

			Frame frame;
			while ((frame = videoGrabber.grabImage()) != null) {
				recorder.record(frame, avutil.AV_PIX_FMT_YUV420P);

				// Microseconds to milliseconds
				videoProgressListener.accept(frame.timestamp / 1000L);
			}

			recorder.close();
		}
		catch (Exception e) {
			throw new FFmpegFrameRecorder.Exception("Could not transcode video", e);
		}
	}

	private FFmpegFrameRecorder createVideoRecorder(FFmpegFrameGrabber videoGrabber, ByteArrayOutputStream outStream) {
		FFmpegLogCallback.set();

		VideoRenderConfiguration renderConfig = config.getVideoConfig();

		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, (int) renderConfig.getDimension().getWidth(),
				(int) renderConfig.getDimension().getHeight());
		recorder.setFrameRate(videoGrabber.getFrameRate());
		recorder.setVideoBitrate(500 * 1000);
		recorder.setVideoCodec(videoGrabber.getVideoCodec());
		recorder.setFormat(config.getFileFormat());
		recorder.setVideoQuality(31);

		return recorder;
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

	private String encodeVideoFileToBase64(Path path) throws IOException {
		ByteArrayOutputStream outStream = new SeekableByteArrayOutputStream();

		transcodeVideo(path, outStream);

		return Base64.getEncoder().encodeToString(outStream.toByteArray());
	}
}
