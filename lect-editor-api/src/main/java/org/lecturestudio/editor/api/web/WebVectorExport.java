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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.concentus.OpusSignal;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.core.util.DirUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.media.audio.opus.OpusAudioFileWriter;
import org.lecturestudio.media.audio.opus.OpusFileFormatType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebVectorExport {

	private static final Logger LOG = LogManager.getLogger(WebVectorExport.class);

	private static final String TEMPLATE_FOLDER = "resources/export/web/vector";

	private static final String TEMPLATE_FILE = "index.html";

	private final Map<String, String> data = new HashMap<>();


	public void setTitle(String title) {
		data.put("title", title);
	}

	public void setName(String name) {
		data.put("name", name);
	}

	public void export(Recording recording, File outputFolder) throws Exception {
		String indexContent = loadTemplateFile(TEMPLATE_FOLDER + "/" + TEMPLATE_FILE);
		indexContent = processTemplateFile(indexContent, data);

		copyResourceToFilesystem(TEMPLATE_FOLDER, outputFolder.getAbsolutePath());

		writeTemplateFile(indexContent, new File(outputFolder.getPath() + File.separator + TEMPLATE_FILE));

		Recording encRecording = new Recording();
		encRecording.setRecordingHeader(recording.getRecordingHeader());
		encRecording.setRecordedEvents(recording.getRecordedEvents());
		encRecording.setRecordedAudio(encodeAudio(recording.getRecordedAudio()));
		encRecording.setRecordedDocument(recording.getRecordedDocument());

		File plrFile = Paths.get(outputFolder.getPath(), data.get("name") + ".plr").toFile();

		String bundleContent = loadTemplateFile(TEMPLATE_FOLDER + "/main.bundle.js");
		bundleContent = processTemplateFile(bundleContent, Map.of("recordingFile", plrFile.getName()));

		writeTemplateFile(bundleContent, new File(outputFolder.getPath() + File.separator + "main.bundle.js"));

		RecordingFileWriter.write(encRecording, plrFile);
	}

	private RecordedAudio encodeAudio(RecordedAudio recAudio) throws Exception {
		RandomAccessAudioStream stream = recAudio.getAudioStream().clone();

		AudioFormat sourceFormat = AudioUtils.createAudioFormat(stream.getAudioFormat());
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				16000,
				sourceFormat.getSampleSizeInBits(),
				sourceFormat.getChannels(),
				sourceFormat.getFrameSize(),
				sourceFormat.getFrameRate(),
				sourceFormat.isBigEndian());

		AudioInputStream inputStream = AudioSystem.getAudioInputStream(targetFormat, stream.getAudioInputStream());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OpusAudioFileWriter writer = new OpusAudioFileWriter(16000, 10, OpusSignal.OPUS_SIGNAL_VOICE);
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
}
