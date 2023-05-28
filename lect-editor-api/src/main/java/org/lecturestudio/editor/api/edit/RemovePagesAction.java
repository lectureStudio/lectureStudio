package org.lecturestudio.editor.api.edit;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

/**
 * Removes the selected pages, without affecting other parts of the recording.
 * The audio and the duration of the recording stay the same.
 */
public class RemovePagesAction extends RecordedObjectAction<RecordedEvents> {
	private final List<RecordedPage> backupPages = new ArrayList<>();
	private final RecordedEvents recordedEvents;
	private final List<Integer> pagesToRemove;

	public RemovePagesAction(RecordedEvents recordedEvents, List<Integer> pagesToRemove) {
		super(recordedEvents);

		this.recordedEvents = recordedEvents;
		this.pagesToRemove = pagesToRemove;
	}

	@Override
	public void undo() throws RecordingEditException {
		List<RecordedPage> recPages = recordedEvents.getRecordedPages();

		recPages.clear();
		recPages.addAll(backupPages);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		List<RecordedPage> pages = recordedEvents.getRecordedPages();

		// Create backup.
		backupPages.clear();
		for (RecordedPage page : pages) {
			backupPages.add(page.clone());
		}

		for (Integer pageToRemove : pagesToRemove) {
			recordedEvents.removePage(pageToRemove);
		}

		int pageShifts = 0;

		for (int i = 0; i < backupPages.size(); i++) {
			if (pagesToRemove.contains(i)) {
				pageShifts++;
			}
			else {
				RecordedPage page = recordedEvents.getRecordedPage(i);
				page.setNumber(page.getNumber() - pageShifts);
			}
		}
	}
}
