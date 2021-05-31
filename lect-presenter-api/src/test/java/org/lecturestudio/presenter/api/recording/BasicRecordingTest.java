/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.recording;

import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioFormat.Encoding;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;

public class BasicRecordingTest {

	Path testPath;

	PresenterContext context;

	DocumentService documentService;

	DocumentRecorder documentRecorder;

	FileLectureRecorder recorder;

	PageEventSubscriber subscriber;


	class PageEventSubscriber {

		Exception exception;


		@Subscribe
		public void onEvent(PageEvent event) {
			if (event.isSelected()) {
				try {
					documentRecorder.recordPage(event.getPage());
				}
				catch (ExecutableException e) {
					exception = e;
				}
			}
		}
	}


	@BeforeEach
	void setup() throws Exception {
		Path root = getResourcePath(".");
		testPath = root.resolve("AppData");

		Configuration config = new DefaultConfiguration();

		EventBus eventBus = ApplicationBus.get();
		EventBus audioBus = AudioBus.get();

		Dictionary dict = new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return key;
			}

			@Override
			public boolean contains(String key) {
				return true;
			}
		};

		Document document = new Document(new File(getClass().getClassLoader()
				.getResource("ModSim.pdf").getFile()));

		AppDataLocator locator = new AppDataLocator(testPath.toString());

		context = new PresenterContext(locator, null, config, dict, eventBus, audioBus) {

			@Override
			public String getRecordingDirectory() {
				return testPath.resolve("recording").toString();
			}

			@Override
			public void saveConfiguration() {

			}
		};

		int pageRecTimeout = 0;
		String soundSystem = "Dummy";

		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		audioConfig.setSoundSystem(soundSystem);
		audioConfig.setInputDeviceName(AudioUtils.getDefaultAudioCaptureDevice(soundSystem).getName());

		subscriber = new PageEventSubscriber();

		context.getEventBus().register(subscriber);

		documentService = context.getDocumentService();

		recorder = new FileLectureRecorder(documentService, audioConfig, context.getRecordingDirectory());
		recorder.setAudioFormat(new AudioFormat(Encoding.S16LE, 44100, 1));
		recorder.setPageRecordingTimeout(pageRecTimeout);

		documentRecorder = new DocumentRecorder(context);
		documentRecorder.setPageRecordingTimeout(pageRecTimeout);
		documentRecorder.start();

		documentService.addDocument(document);
		documentService.selectDocument(document);
	}

	@AfterEach
	void destroy() throws IOException {
		for (Document doc : context.getDocumentService().getDocuments().getPdfDocuments()) {
			doc.close();
		}

		deletePath(testPath);
	}

	@Test
	void testRecording() throws Exception {
		Document document = documentService.getDocuments().getSelectedDocument();
		CountDownLatch doneLatch = new CountDownLatch(document.getPageCount());
		AtomicReference<Exception> exceptionRef = new AtomicReference<>();

		documentRecorder.recordPage(document.getCurrentPage());

		recorder.start();

		CompletableFuture.runAsync(() -> {
			for (int i = 0; i < document.getPageCount(); i++) {
				try {
					documentService.selectPage(i);

					TimeUnit.MILLISECONDS.sleep(50);
				}
				catch (Exception e) {
					exceptionRef.set(e);
				}
				finally {
					doneLatch.countDown();
				}
			}
		});

		doneLatch.await();

		recorder.stop();

		Assertions.assertNull(exceptionRef.get());
		Assertions.assertNull(subscriber.exception);
		Assertions.assertEquals(document.getPageCount(),
				documentRecorder.getRecordedPages().size());
	}

	Path getResourcePath(String path) throws URISyntaxException {
		return Path.of(Objects.requireNonNull(
				getClass().getClassLoader().getResource(path)).toURI());
	}

	private void deletePath(Path path) throws IOException {
		if (!Files.exists(path)) {
			return;
		}

		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
