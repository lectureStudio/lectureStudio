package org.lecturestudio.editor.api.edit;

import java.util.List;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

/**
 * Insert PlaybackActions into the given Page of the recording.
 */
public class InsertPlaybackActionsAction extends RecordedObjectAction<RecordedEvents> {
	private final List<PlaybackAction> addedActions;
	private final int pageNumber;
	private final Recording recording;
	private final Interval<Double> duration;


	public InsertPlaybackActionsAction(List<PlaybackAction> addedActions, int pageNumber, Recording recording) {
		super(recording.getRecordedEvents());

		this.addedActions = addedActions;
		this.pageNumber = pageNumber;
		this.recording = recording;

		long totalLength = recording.getRecordingHeader().getDuration();
		int start = addedActions.get(0).getTimestamp();
		int end = addedActions.get(addedActions.size() - 1).getTimestamp();

		duration = new Interval<>((double) start / totalLength, (double) end / totalLength);
	}

	@Override
	public void undo() throws RecordingEditException {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);

		recordedPage.getPlaybackActions().removeAll(addedActions);

		recording.fireChangeEvent(Recording.Content.EVENTS_REMOVED, duration);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		int firstTimeStamp = addedActions.get(0).getTimestamp();
		PlaybackAction insertAfterAction = actions.stream().reduce(null, (previous, action) -> {
			if (action.getTimestamp() < firstTimeStamp) {
				return action;
			}
			return previous;
		});

		if (insertAfterAction != null) {
			actions.addAll(actions.indexOf(insertAfterAction) + 1, addedActions);
		}
		else {
			actions.addAll(0, addedActions);
		}

		recording.fireChangeEvent(Recording.Content.EVENTS_ADDED, duration);
	}

}
