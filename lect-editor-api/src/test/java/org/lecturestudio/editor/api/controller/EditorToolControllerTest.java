package org.lecturestudio.editor.api.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Singleton;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.ServiceTest;

public class EditorToolControllerTest extends ServiceTest {
	private RecordingFileService recordingService;
	private EditorToolController toolController;

	@BeforeEach
	@Override
	protected void setupInjector() throws ExecutionException, InterruptedException {
		injector = new GuiceInjector(new AbstractModule() {
			@Provides
			@Singleton
			ApplicationContext provideApplicationContext() {
				return context;
			}

			@Provides
			@Singleton
			AudioSystemProvider provideAudioSystemProvider() {
				return audioSystemProvider;
			}
		});

		recordingService = injector.getInstance(RecordingFileService.class);
		String recordingPath = Objects.requireNonNull(getClass().getClassLoader().getResource("empty_pages_recording.presenter")).getFile();
		recordingService.openRecording(new File(recordingPath)).get();

		toolController = injector.getInstance(EditorToolController.class);
	}

	@Test
	void testSimpleToolAction() throws InterruptedException {
		AtomicInteger counter = new AtomicInteger(0);

		toolController.isEditingProperty().addListener(((observable, oldValue, newValue) -> {
			assertTrue(counter.incrementAndGet() <= 2);
		}));

		toolController.selectDeleteAllTool();

		awaitTrue(() -> counter.get() == 2, 10);
	}
}
