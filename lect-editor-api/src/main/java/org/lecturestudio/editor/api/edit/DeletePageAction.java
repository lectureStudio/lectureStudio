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

package org.lecturestudio.editor.api.edit;

import java.util.List;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * A {@code DeletePageAction} removes a portion of a recording specified by the
 * page number. All recorded parts - audio, events and slides - contained within
 * the duration of the page will be removed from the recording.
 *
 * @author Alex Andres
 */
public class DeletePageAction extends RecordingAction {

	/**
	 * The page number of the recorded page to remove.
	 */
	private final int pageNumber;


	/**
	 * Creates a new {@code DeletePageAction} with the provided parameters.
	 *
	 * @param recording The recording on which to operate.
	 * @param time      The time position that points to a recorded page that
	 *                  should be removed. The value must be within the range
	 *                  [0, 1].
	 */
	public DeletePageAction(Recording recording, double time) {
		super(recording, createActions(recording, time));

		pageNumber = getPageNumber(recording, time);
	}

	@Override
	protected Interval<Double> getEditDuration() {
		long length = recording.getRecordedAudio().getAudioStream().getLengthInMillis();
		Interval<Integer> pageDuration = getPageDuration(recording, pageNumber);

		return new Interval<>(1.0 * pageDuration.getStart() / length,
				1.0 * pageDuration.getEnd() / length);
	}

	private static List<EditAction> createActions(Recording recording, double timeSelection) {
		int pageNumber = getPageNumber(recording, timeSelection);
		Interval<Integer> pageDuration = getPageDuration(recording, timeSelection);

		EditHeaderAction headerAction = new EditHeaderAction(recording.getRecordingHeader(), -pageDuration.lengthLong());
		DeleteDocumentPageAction documentAction = new DeleteDocumentPageAction(recording.getRecordedDocument(), pageNumber);
		DeleteEventsAction eventsAction = new DeleteEventsAction(recording.getRecordedEvents(), pageDuration, recording.getToolDemoRecordingsData());
		DeleteAudioAction audioAction = new DeleteAudioAction(recording.getRecordedAudio(), pageDuration);

		eventsAction.removeRecordedPage(pageNumber);

		return List.of(headerAction, documentAction, eventsAction, audioAction);
	}

	private static int getPageNumber(Recording recording, double timeSelection) {
		long length = recording.getRecordedAudio().getAudioStream().getLengthInMillis();
		int time = (int) (timeSelection * length);

		return recording.getPageIndex(time, 0);
	}

	private static Interval<Integer> getPageDuration(Recording recording, double timeSelection) {
		int pageNumber = getPageNumber(recording, timeSelection);

		return getPageDuration(recording, pageNumber);
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
}
