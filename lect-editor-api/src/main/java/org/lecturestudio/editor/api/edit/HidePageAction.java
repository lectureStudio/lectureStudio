package org.lecturestudio.editor.api.edit;

import java.util.List;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * Removes the page from the document and the recording, without affecting other parts of the recording.
 * The audio and duration of the recording stay the same.
 */
public class HidePageAction extends RecordingAction {
	private final int pageNumber;

	public HidePageAction(Recording recording, int pageNumber) {
		super(recording, createActions(recording, pageNumber));
		this.pageNumber = pageNumber;
	}

	public static List<EditAction> createActions(Recording recording, int pageNumber) {
		DeleteDocumentPageAction documentAction = new DeleteDocumentPageAction(recording.getRecordedDocument(), pageNumber);
		RemovePagesAction eventsAction = new RemovePagesAction(recording.getRecordedEvents(), List.of(pageNumber));

		return List.of(documentAction, eventsAction);
	}

	private static Interval<Integer> getPageDuration(Recording recording, int pageNumber) {
		List<RecordedPage> recPages = recording.getRecordedEvents().getRecordedPages();
		RecordedPage page = recPages.get(Math.min(pageNumber, recPages.size() - 1));

		RecordedPage lastPage = new RecordedPage();
		lastPage.setTimestamp((int) recording.getRecordedAudio().getAudioStream().getLengthInMillis());

		RecordedPage nextPage = pageNumber < recPages.size() - 1 ? recPages.get(pageNumber + 1) : lastPage;

		Interval<Integer> pageInterval = new Interval<>();
		pageInterval.set(page.getTimestamp(), nextPage.getTimestamp());

		return pageInterval;
	}

	@Override
	protected Interval<Double> getEditDuration() {
		long length = recording.getRecordedAudio().getAudioStream().getLengthInMillis();
		Interval<Integer> pageDuration = getPageDuration(recording, pageNumber);

		return new Interval<>(1.0 * pageDuration.getStart() / length,
				1.0 * pageDuration.getEnd() / length);
	}

	@Override
	protected void fireChangeEvent(Interval<Double> duration) {
		recording.fireChangeEvent(Recording.Content.ALL, duration);
		recording.fireChangeEvent(Recording.Content.EVENTS_REMOVED, duration);
	}
}
