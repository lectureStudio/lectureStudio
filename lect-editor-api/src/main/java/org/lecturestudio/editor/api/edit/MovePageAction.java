package org.lecturestudio.editor.api.edit;

import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * Moves the timestamp of the supplied RecordedPage, without affecting other parts of the recording,
 * like the audio or total duration of the recording.
 */
public class MovePageAction implements EditAction {
	private final Recording recording;
	private final int pageNumber;
	private int previousPosition;
	private int newPosition;

	public MovePageAction(Recording recording, int pageNumber, int newPosition) {

		this.recording = recording;
		this.pageNumber = pageNumber;
		this.newPosition = newPosition;
		this.previousPosition = recording.getRecordedEvents().getRecordedPage(pageNumber).getTimestamp();
	}

	public void switchPositions() {
		int tmp = newPosition;
		newPosition = previousPosition;
		previousPosition = tmp;
	}

	@Override
	public void undo() throws RecordingEditException {
		switchPositions();
		execute();
	}

	@Override
	public void redo() throws RecordingEditException {
		switchPositions();
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		RecordedPage page = recording.getRecordedEvents().getRecordedPage(pageNumber);
		page.setTimestamp(newPosition);

		recording.fireChangeEvent(Recording.Content.EVENTS_CHANGED);
	}
}
