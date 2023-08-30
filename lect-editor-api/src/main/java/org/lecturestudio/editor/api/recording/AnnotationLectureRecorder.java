package org.lecturestudio.editor.api.recording;

import static java.util.Objects.isNull;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.editor.api.bus.event.EditorRecordActionEvent;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;

@Singleton
public class AnnotationLectureRecorder extends LectureRecorder {

	private final DocumentService documentService;
	private final RecordingFileService recordingService;
	private final EditorContext context;
	private final RecordingPlaybackService playbackService;
	private ArrayList<PlaybackAction> addedActions;
	private Integer pageNumber;
	private String errorDuringRecording;
	private ExecutableState previousPlaybackState;

	@Inject
	public AnnotationLectureRecorder(DocumentService documentService,
	                                 RecordingFileService recordingFileService,
	                                 ApplicationContext context,
	                                 RecordingPlaybackService playbackService) {
		this.documentService = documentService;
		this.recordingService = recordingFileService;
		this.context = (EditorContext) context;
		this.playbackService = playbackService;

		reset();
	}


	@Subscribe
	public void onEvent(final EditorRecordActionEvent event) {
		PlaybackAction action = event.getAction();

		if (action != null && action.getTimestamp() == 0) {
			addPlaybackAction(action);
		}
	}

	private void addPlaybackAction(PlaybackAction action) {
		if (isNull(action)) {
			return;
		}

		if (!playbackService.started()) {
			errorDuringRecording = "annotationrecorder.recording.stopped.message";
		}

		if (errorDuringRecording == null) {
			if (pageNumber == null) {
				pageNumber = documentService.getDocuments().getSelectedDocument().getCurrentPageNumber();
			}
			else if (pageNumber != documentService.getDocuments().getSelectedDocument().getCurrentPageNumber()) {
				errorDuringRecording = "annotationrecorder.page.flipped.message";
			}

			action.setTimestamp((int) getElapsedTime());

			// Add action to the current page.
			addedActions.add(action);
		}
	}

	public synchronized CompletableFuture<Void> persistPlaybackActions() throws IllegalStateException {
		if (errorDuringRecording != null) {
			handleException(errorDuringRecording);
		}

		if (addedActions.isEmpty() || pageNumber == null) {
			// No actions were recorded
			return CompletableFuture.completedFuture(null);
		}

		RecordedPage page = recordingService.getSelectedRecording().getRecordedEvents().getRecordedPage(pageNumber);
		List<PlaybackAction> actions = page.getPlaybackActions();

		String error = checkAnnotationBounds(actions);

		if (error != null) {
			handleException(error);
		}

		CompletableFuture<Void> task = recordingService.insertPlaybackActions(new ArrayList<>(addedActions), pageNumber);
		reset();
		return task;
	}

	private String checkAnnotationBounds(List<PlaybackAction> actions) {
		// New PlaybackActions that should be added to the page
		int newActionStartTime = addedActions.get(0).getTimestamp();
		int newActionEndTime = addedActions.get(addedActions.size() - 1).getTimestamp();

		// Already existing PlaybackActions on the page
		Integer existingActionStartTime = null;
		Integer existingActionEndTime = null;

		// Using a queue to get the event before the TOOL_BEGIN in case an event did send a TOOL_END event,
		// but a new one already started
		Queue<PlaybackAction> actionQueue = new ArrayDeque<>(3);

		for (PlaybackAction action : actions) {
			// Only saving 2 Events in the Queue, as the event before the TOOL_BEGIN event describes the current one
			// and we need the event before that one
			if (actionQueue.size() >= 3) {
				actionQueue.poll();
			}
			actionQueue.offer(action);

			if (action.getType() != ActionType.TOOL_BEGIN && action.getType() != ActionType.TOOL_END &&
					existingActionStartTime == null) {
				// This is to capture atomic actions without start and end times, such as SimpleToolActions
				existingActionStartTime = action.getTimestamp();
				existingActionEndTime = action.getTimestamp();
			}
			else if (action.getType() == ActionType.TOOL_BEGIN) {
				existingActionStartTime = action.getTimestamp();
			}
			else if (action.getType() == ActionType.TOOL_END) {
				existingActionEndTime = action.getTimestamp();
			}

			if (existingActionStartTime != null && existingActionEndTime != null) {
				if (existingActionStartTime < newActionEndTime && newActionStartTime < existingActionEndTime) {
					return "annotationrecorder.overlap.message";
				}
				else {
					// No overlap detected
					existingActionStartTime = null;
					existingActionEndTime = null;
				}
			}
		}
		return null;
	}

	@Override
	protected void initInternal() throws ExecutableException {
	}

	@Override
	protected void startInternal() throws ExecutableException {
		context.getEventBus().register(this);
		previousPlaybackState = playbackService.getState();
		if (!playbackService.started()) {
			playbackService.start();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		context.getEventBus().unregister(this);

		if (previousPlaybackState != ExecutableState.Started && !playbackService.suspended()) {
			playbackService.suspend();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	@Override
	public long getElapsedTime() {
		return playbackService.getElapsedTime();
	}

	private void handleException(String message) throws IllegalStateException {
		// New PlaybackActions that should be added to the page
		Interval<Double> eventDuration;

		if (!addedActions.isEmpty()) {
			int newActionStartTime = addedActions.get(0).getTimestamp();
			int newActionEndTime = addedActions.get(addedActions.size() - 1).getTimestamp();
			long duration = recordingService.getSelectedRecording().getRecordingHeader().getDuration();
			eventDuration = new Interval<>((double) newActionStartTime / duration, (double) newActionEndTime / duration);
		}
		else {
			eventDuration = null;
		}

		CompletableFuture.runAsync(() -> {
			recordingService.getSelectedRecording().fireChangeEvent(Recording.Content.EVENTS_REMOVED, eventDuration);
		});

		reset();
		throw new IllegalStateException(message);
	}

	public void reset() {
		addedActions = new ArrayList<>();
		pageNumber = null;
		errorDuringRecording = null;
	}
}
