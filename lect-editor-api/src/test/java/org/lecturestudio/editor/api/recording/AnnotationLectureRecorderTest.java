package org.lecturestudio.editor.api.recording;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.editor.api.controller.EditorToolController;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.service.ServiceTest;

public class AnnotationLectureRecorderTest extends ServiceTest {
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

		context.getEventBus().register(this);
	}

	@AfterEach
	void tearDown() {
		context.getEventBus().unregister(this);
	}

	@Test
	void testEditingFalse() throws InterruptedException, ExecutableException {
		Recording recording = recordingService.getSelectedRecording();
		List<PlaybackAction> actionsBefore = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());

		toolController.selectPenTool();
		executeTool(createRandomPoints());

		Thread.sleep(2000);
		List<PlaybackAction> actionsAfter = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());

		assertEquals(actionsBefore.size(), actionsAfter.size());
		assertTrue(actionsBefore.containsAll(actionsAfter));
	}

	@Test
	void testAddPlaybackActions() throws InterruptedException {
		Recording recording = recordingService.getSelectedRecording();
		List<PlaybackAction> actionsBefore = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());
		LinkedList<PenPoint2D> points = createRandomPoints();

		toolController.selectPenTool();
		toolController.setIsEditing(true);
		executeTool(points);
		toolController.setIsEditing(false);

		awaitTrue(() -> recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions().size() != actionsBefore.size(), 10);
		List<PlaybackAction> actionsAfter = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());

		assertEquals(actionsBefore.size() + points.size() + 1, actionsAfter.size());
		assertTrue(actionsAfter.containsAll(actionsBefore));
	}

	@Test
	void testPlaybackActionsOverlap() throws InterruptedException, ExecutableException {
		testAddPlaybackActions();

		Recording recording = recordingService.getSelectedRecording();
		List<PlaybackAction> actionsBefore = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());
		RecordingPlaybackService recordingPlaybackService = injector.getInstance(RecordingPlaybackService.class);

		recordingPlaybackService.seek(0.0);

		Thread.sleep(1000);

		LinkedList<PenPoint2D> points = createRandomPoints();
		toolController.setIsEditing(true);
		executeTool(points);
		toolController.setIsEditing(false);

		Thread.sleep(2000);
		List<PlaybackAction> actionsAfter = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());

		assertEquals(actionsBefore.size(), actionsAfter.size());
		assertTrue(actionsAfter.containsAll(actionsBefore));
	}

	@Test
	void testRecordingEnded() throws ExecutableException, InterruptedException {
		testAddPlaybackActions();

		Recording recording = recordingService.getSelectedRecording();
		List<PlaybackAction> actionsBefore = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());
		RecordingPlaybackService recordingPlaybackService = injector.getInstance(RecordingPlaybackService.class);

		recordingPlaybackService.seek(0.999);

		Thread.sleep(1000);

		LinkedList<PenPoint2D> points = createRandomPoints();
		toolController.setIsEditing(true);
		executeTool(points);
		toolController.setIsEditing(false);

		Thread.sleep(2000);
		List<PlaybackAction> actionsAfter = new ArrayList<>(recording.getRecordedEvents().getRecordedPage(0).getPlaybackActions());

		assertEquals(actionsBefore.size(), actionsAfter.size());
		assertTrue(actionsAfter.containsAll(actionsBefore));
	}

	private LinkedList<PenPoint2D> createRandomPoints() {
		LinkedList<PenPoint2D> points = new LinkedList<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		int min = 0;
		int max = 1000;

		for (int i = 0; i < 10; i++) {
			double x = random.nextDouble(min, max);
			double y = random.nextDouble(min, max);

			points.add(new PenPoint2D(x, y));
		}

		return points;
	}


	private void executeTool(LinkedList<PenPoint2D> points) throws InterruptedException {
		toolController.beginToolAction(points.getFirst());

		for (int i = 1; i < points.size() - 1; i++) {
			Thread.sleep(10);
			toolController.executeToolAction(points.get(i));
		}

		toolController.endToolAction(points.getLast());
	}
}
