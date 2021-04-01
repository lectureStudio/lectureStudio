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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concentus.OpusSignal;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.core.util.DirUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.editor.api.video.VideoRenderProgressEvent;
import org.lecturestudio.editor.api.video.VideoRenderStateEvent;
import org.lecturestudio.editor.api.video.VideoRenderStateEvent.State;
import org.lecturestudio.media.audio.opus.OpusAudioFileWriter;
import org.lecturestudio.media.audio.opus.OpusFileFormatType;

public class WebVectorExport extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(WebVectorExport.class);

	private static final String TEMPLATE_FOLDER = "resources/export/web/vector";

	private static final String TEMPLATE_FILE = "index.html";

	private final Map<String, String> data = new HashMap<>();

	private final Recording recording;

	private final File outputFolder;

	private double streamLength;


	public WebVectorExport(Recording recording, File outputFolder) {
		this.recording = recording;
		this.outputFolder = outputFolder;
	}

	public void setTitle(String title) {
		data.put("title", title);
	}

	public void setName(String name) {
		data.put("name", name);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		String indexContent = loadTemplateFile(TEMPLATE_FOLDER + "/" + TEMPLATE_FILE);
		indexContent = processTemplateFile(indexContent, data);

		try {
			copyResourceToFilesystem(TEMPLATE_FOLDER, outputFolder.getAbsolutePath());

			writeTemplateFile(indexContent, new File(outputFolder.getPath() + File.separator + TEMPLATE_FILE));
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		fireRenderState(new VideoRenderStateEvent(State.RENDER_AUDIO));

		RecordedAudio encAudio;

		try {
			encAudio = encodeAudio(recording.getRecordedAudio());
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		Recording encRecording = new Recording();
		encRecording.setRecordingHeader(recording.getRecordingHeader());
		encRecording.setRecordedEvents(recording.getRecordedEvents());
		encRecording.setRecordedAudio(encAudio);
		encRecording.setRecordedDocument(recording.getRecordedDocument());

		File plrFile = Paths.get(outputFolder.getPath(), data.get("name") + ".plr").toFile();

		String bundleContent = loadTemplateFile(TEMPLATE_FOLDER + "/main.bundle.js");
		bundleContent = processTemplateFile(bundleContent, Map.of("recordingFile", plrFile.getName()));

		try {
			writeTemplateFile(bundleContent, new File(outputFolder.getPath() + File.separator + "main.bundle.js"));

			RecordingFileWriter.write(encRecording, plrFile);

			fireRenderState(new VideoRenderStateEvent(State.FINISHED));
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {

	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private RecordedAudio encodeAudio(RecordedAudio recAudio) throws Exception {
		RandomAccessAudioStream stream = recAudio.getAudioStream().clone();

		AudioFormat sourceFormat = AudioUtils.createAudioFormat(stream.getAudioFormat());
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				16000,
				sourceFormat.getSampleSizeInBits(),
				sourceFormat.getChannels(),
				sourceFormat.getFrameSize(),
				16000,
				sourceFormat.isBigEndian());

		streamLength = stream.getAudioInputStream().available() * (16000 / sourceFormat.getSampleRate());

		Time totalTime = new Time(stream.getLengthInMillis());
		Time progressTime = new Time(0);

		VideoRenderProgressEvent progressEvent = new VideoRenderProgressEvent();
		progressEvent.setCurrentTime(progressTime);
		progressEvent.setTotalTime(totalTime);

		AudioInputStream inputStream = AudioSystem.getAudioInputStream(targetFormat, stream.getAudioInputStream());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OpusAudioFileWriter writer = new OpusAudioFileWriter(16000, 10, OpusSignal.OPUS_SIGNAL_VOICE);
		writer.setProgressListener(readTotal -> {
			double progress = readTotal / streamLength;

			progressTime.setMillis((long) (totalTime.getMillis() * progress));

			fireRenderProgress(progressEvent);
		});
		writer.write(inputStream, OpusFileFormatType.OPUS, outputStream);

		ByteArrayInputStream encodedStream = new ByteArrayInputStream(
				outputStream.toByteArray());
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(
				new DynamicInputStream(encodedStream), true);

		return new RecordedAudio(audioStream);
	}

	private String loadTemplateFile(String path) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(path);

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

	private void copyResourceToFilesystem(String resName, String baseDir) throws Exception {
		URL resURL = ResourceLoader.getResourceURL(resName);

		if (ResourceLoader.isJarResource(resURL)) {
			String jarPath = ResourceLoader.getJarPath(this.getClass());
			FileUtils.copyJarResource(jarPath, resName, baseDir);
		}
		else {
			File resFile = new File(resURL.getPath());
			Path sourcePath = resFile.toPath();

			if (resFile.isFile()) {
				Path targetPath = Paths.get(baseDir, resFile.getName());
				Files.copy(sourcePath, targetPath);
			}
			else if (resFile.isDirectory()) {
				Path targetPath = Paths.get(baseDir);
				DirUtils.copy(sourcePath, targetPath);
			}
		}
	}

	private void fireRenderState(VideoRenderStateEvent event) {
		ApplicationBus.post(event);
	}

	private void fireRenderProgress(VideoRenderProgressEvent event) {
		ApplicationBus.post(event);
	}
}
