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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.concentus.OpusSignal;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.core.util.DirUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderState;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;
import org.lecturestudio.media.audio.opus.OpusAudioFileWriter;
import org.lecturestudio.media.audio.opus.OpusFileFormatType;
import org.lecturestudio.media.config.RenderConfiguration;

public class WebVectorExport extends RecordingExport {

	private static final String TEMPLATE_FOLDER = "web-player";

	private static final String TEMPLATE_FILE = "index.html";

	private final Map<String, String> data = new HashMap<>();

	private final Recording recording;

	private final RenderConfiguration config;

	private OpusAudioFileWriter writer;

	private String outputPath;


	public WebVectorExport(Recording recording, RenderConfiguration config) {
		this.recording = recording;
		this.config = config;
	}

	public void setTitle(String title) {
		data.put("title", title);
	}

	public void setName(String name) {
		data.put("name", name);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		File outputFile = config.getOutputFile();
		String webExportPath = FileUtils.stripExtension(outputFile.getPath());
		File outputFolder = Paths.get(webExportPath).getParent().resolve("vector").toFile();

		outputPath = outputFolder.getAbsolutePath();

		setName(FileUtils.stripExtension(outputFile.getName()));

		String indexContent = loadTemplateFile(TEMPLATE_FOLDER + "/" + TEMPLATE_FILE);
		indexContent = processTemplateFile(indexContent, data);

		try {
			copyResourceToFilesystem(TEMPLATE_FOLDER, outputPath, List.of("txt"));

			writeTemplateFile(indexContent, getFile(TEMPLATE_FILE));
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() {
		CompletableFuture.supplyAsync(() -> {
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

			File plrFile = getFile(data.get("name") + ".plr");

			String bundleContent = loadTemplateFile(TEMPLATE_FOLDER + "/main.bundle.js");
			bundleContent = processTemplateFile(bundleContent, Map.of("recordingFile", plrFile.getName()));

			try {
				writeTemplateFile(bundleContent, getFile("main.bundle.js"));

				RecordingFileWriter.write(encRecording, plrFile);

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
		if (nonNull(writer)) {
			writer.cancelWriting();
		}
	}

	@Override
	protected void destroyInternal() {

	}

	private File getFile(String file) {
		return Paths.get(outputPath, file).toFile();
	}

	private RecordedAudio encodeAudio() throws Exception {
		onRenderState(RecordingRenderState.RENDER_AUDIO);

		RecordedAudio recAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recAudio.getAudioStream().clone();

		AudioFormat sourceFormat = AudioUtils.createAudioFormat(stream.getAudioFormat());
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				16000,
				sourceFormat.getSampleSizeInBits(),
				sourceFormat.getChannels(),
				sourceFormat.getFrameSize(),
				16000,
				sourceFormat.isBigEndian());

		AudioInputStream inputStream = AudioSystem.getAudioInputStream(targetFormat, stream.getAudioInputStream());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		writer = new OpusAudioFileWriter(16000, 10, OpusSignal.OPUS_SIGNAL_VOICE);
		writer.setProgressListener(new ProgressListener(recording, targetFormat));
		writer.write(inputStream, OpusFileFormatType.OPUS, outputStream);

		ByteArrayInputStream encodedStream = new ByteArrayInputStream(
				outputStream.toByteArray());
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(
				new DynamicInputStream(encodedStream), true);

		return new RecordedAudio(audioStream);
	}

	private String loadTemplateFile(String path) {
		InputStream is = ResourceLoader.getResourceAsStream(path);

		if (isNull(is)) {
			throw new NullPointerException("Missing web index.html file.");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		return reader.lines().collect(Collectors.joining(System.lineSeparator()));
	}

	private String processTemplateFile(String fileContent, Map<String, String> data) {
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher m = p.matcher(fileContent);
		StringBuilder sb = new StringBuilder();

		while (m.find()) {
			String var = m.group(1);
			String replacement = data.get(var);

			if (nonNull(replacement)) {
				m.appendReplacement(sb, replacement);
			}
			else {
				LOG.warn("Found match '{}' with no replacement.", var);
			}
		}

		m.appendTail(sb);

		return sb.toString();
	}

	private void writeTemplateFile(String fileContent, File outputFile) throws Exception {
		Path path = Paths.get(outputFile.getPath());
		Files.writeString(path, fileContent);
	}

	private void copyResourceToFilesystem(String resName, String baseDir,
			List<String> skipList) throws Exception {
		URL resURL = ResourceLoader.getResourceURL(resName);

		if (ResourceLoader.isJarResource(resURL)) {
			String jarStringPath = resURL.toString().replace("jar:", "");
			String jarCleanPath = Paths.get(new URI(jarStringPath)).toString();
			String jarPath = jarCleanPath.substring(0, jarCleanPath.lastIndexOf(".jar") + 4);

			FileUtils.copyJarResource(jarPath, resName, baseDir, skipList);
		}
		else {
			File resFile = new File(resURL.getPath());
			Path sourcePath = resFile.toPath();

			if (resFile.isFile() && !skipList.contains(FileUtils.getExtension(sourcePath.toString()))) {
				Path targetPath = Paths.get(baseDir, resFile.getName());
				Files.copy(sourcePath, targetPath);
			}
			else if (resFile.isDirectory()) {
				Path targetPath = Paths.get(baseDir);
				DirUtils.copy(sourcePath, targetPath, skipList);
			}
		}
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
