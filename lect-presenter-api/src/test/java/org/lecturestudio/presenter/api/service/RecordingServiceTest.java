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

package org.lecturestudio.presenter.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.checkerframework.checker.optional.qual.Present;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.RandomAccessStream;
import org.lecturestudio.core.io.WaveHeader;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.recording.RecordingBackup;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordingServiceTest extends ServiceTest {

	private static Path recPath;

	private RecordingService recordingService;

	private CameraRecordingService cameraRecordingService;

	private CameraService cameraService;

	private FileLectureRecorder recorder;


	@BeforeAll
	static void init() throws URISyntaxException {
		Path testPath = Path.of(Objects.requireNonNull(RecordingServiceTest.class.getClassLoader().getResource(".")).toURI());

		recPath = testPath.resolve("recording");
	}

	@AfterAll
	static void destroy() throws IOException {
		Files.walkFileTree(recPath, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@BeforeEach
	void setUp() throws IOException {
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setCaptureDeviceName("dummy");

		recorder = new FileLectureRecorder(audioSystemProvider, documentService, audioConfig, recPath.toFile().getPath());

		cameraRecordingService = new CameraRecordingService((PresenterContext) context, cameraService, recorder);

		recordingService = new RecordingService((PresenterContext) context, recorder, cameraRecordingService);
		recordingService.setAudioFormat(new AudioFormat(AudioFormat.Encoding.S16BE, 24000, 1));
	}

	@AfterEach
	void tearDown() throws ExecutableException {
		try {
			recorder.stop();
		}
		catch (Exception ignored) {
		}
		
		if (!recordingService.destroyed()) {
			recordingService.destroy();
		}
	}

	@Test
	void testInit() throws ExecutableException {
		recordingService.init();

		assertEquals(ExecutableState.Initialized, recordingService.getState());
	}

	@Test
	void testStart() throws ExecutableException {
		recordingService.start();

		assertEquals(ExecutableState.Started, recordingService.getState());
	}

	@Test
	void testSuspend() throws ExecutableException {
		recordingService.start();
		recordingService.suspend();

		assertEquals(ExecutableState.Suspended, recordingService.getState());
	}

	@Test
	void testStop() throws ExecutableException {
		recordingService.start();
		recordingService.stop();

		assertEquals(ExecutableState.Stopped, recordingService.getState());

		recordingService.start();
		recordingService.suspend();
		recordingService.stop();

		assertEquals(ExecutableState.Stopped, recordingService.getState());
	}

	@Test
	void testDestroy() throws ExecutableException {
		recordingService.destroy();

		assertEquals(ExecutableState.Destroyed, recordingService.getState());

		recordingService.start();
		recordingService.destroy();

		assertEquals(ExecutableState.Destroyed, recordingService.getState());

		recordingService.start();
		recordingService.suspend();
		recordingService.destroy();

		assertEquals(ExecutableState.Destroyed, recordingService.getState());
	}

	@Test
	void testSetAudioFormat() throws ExecutableException, IOException {
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S32LE, 96000, 2);

		recordingService.setAudioFormat(audioFormat);
		recordingService.start();
		recordingService.stop();

		RecordingBackup backup = new RecordingBackup(recPath.toFile().getPath());

		File audioFile = recPath.resolve(backup.getCheckpoint() + ".wav").toFile();

		try (InputStream stream = new RandomAccessStream(audioFile)) {
			WaveHeader waveHeader = new WaveHeader(stream);
			AudioFormat wavFormat = waveHeader.getAudioFormat();

			assertEquals(audioFormat.getBitsPerSample(), wavFormat.getBitsPerSample());
			assertEquals(audioFormat.getBytesPerSample(), wavFormat.getBytesPerSample());
			assertEquals(audioFormat.getChannels(), wavFormat.getChannels());
			assertEquals(audioFormat.getEncoding(), wavFormat.getEncoding());
			assertEquals(audioFormat.getSampleRate(), wavFormat.getSampleRate());
		}
	}

	@Test
	void testWriteRecording() throws Exception {
		Path filePath = recPath.resolve("test-rec.presenter");

		recordingService.start();
		recordingService.stop();
		recordingService.writeRecording(filePath.toFile(), progress -> {}).get();

		try (FileChannel fileChannel = FileChannel.open(filePath)) {
			assertEquals(788, fileChannel.size());
		}
	}

	@Test
	void testDiscardRecording() throws ExecutableException, IOException {
		recordingService.start();
		recordingService.stop();

		RecordingBackup backup = new RecordingBackup(recPath.toFile().getPath());

		assertNotNull(backup.getCheckpoint());

		recordingService.discardRecording();

		assertNull(backup.getCheckpoint());
	}
}