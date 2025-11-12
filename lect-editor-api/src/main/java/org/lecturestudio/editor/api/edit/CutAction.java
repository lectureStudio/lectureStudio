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

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingHeader;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * A {@code CutAction} removes a portion of a recording specified by a time
 * interval. All recorded parts - audio, events and slides - contained within
 * the interval will be removed from the recording.
 *
 * @author Alex Andres
 */
public class CutAction extends RecordingAction {

	private static final Logger LOG = LogManager.getLogger(Recording.class);


	/**
	 * Creates a new {@code CutAction} with the provided parameters.
	 *
	 * @param recording The recording on which to operate.
	 * @param start     The start time from where to start removing. The value
	 *                  must be within the range [0, 1].
	 * @param end       The end time when to stop removing. The value must be
	 *                  within the range [0, 1].
	 */
	public CutAction(Recording recording, double start, double end) {
		this(recording, start, end, null);
	}

	/**
	 * Creates a new {@code CutAction} with the provided parameters.
	 *
	 * @param recording The recording on which to operate.
	 * @param start     The start time from where to start removing. The value
	 *                  must be within the range [0, 1].
	 * @param end       The end time when to stop removing. The value must be
	 *                  within the range [0, 1].
	 * @throws IllegalArgumentException If the recording is null or the time values are invalid.
	 */
	public CutAction(Recording recording, double start, double end, DoubleProperty primarySelectionProperty) {
		super(recording, createActions(validateAndPrepare(recording, start, end, primarySelectionProperty)));
	}

	private static CutActionParams validateAndPrepare(Recording recording, double start, double end, DoubleProperty primarySelectionProperty) {
		if (recording == null) {
			throw new IllegalArgumentException("Recording cannot be null");
		}

		// Validate time range
		if (start < 0.0 || start > 1.0) {
			throw new IllegalArgumentException("Start time must be in range [0.0, 1.0], got: " + start);
		}
		if (end < 0.0 || end > 1.0) {
			throw new IllegalArgumentException("End time must be in range [0.0, 1.0], got: " + end);
		}

		// Ensure start is before end
		double validatedStart = Math.min(start, end);
		double validatedEnd = Math.max(start, end);

		// Check for meaningful cut interval
		if (validatedStart >= validatedEnd) {
			throw new IllegalArgumentException("Cut interval must have positive duration");
		}

		return new CutActionParams(recording, validatedStart, validatedEnd, primarySelectionProperty);
	}

	private static class CutActionParams {
		final Recording recording;
		final double start;
		final double end;
		final DoubleProperty primarySelectionProperty;

		CutActionParams(Recording recording, double start, double end, DoubleProperty primarySelectionProperty) {
			this.recording = recording;
			this.start = start;
			this.end = end;
			this.primarySelectionProperty = primarySelectionProperty;
		}
	}

	private static List<EditAction> createActions(CutActionParams params) {
		Recording recording = params.recording;
		double start = params.start;
		double end = params.end;
		DoubleProperty primarySelectionProperty = params.primarySelectionProperty;

		RecordingHeader header = recording.getRecordingHeader();
		RecordedAudio audio = recording.getRecordedAudio();
		RecordedDocument doc = recording.getRecordedDocument();
		RecordedEvents events = recording.getRecordedEvents();

		long duration = audio.getAudioStream().getLengthInMillis();

		long startTime = (long) (start * duration);
		long endTime = (long) (end * duration);

		// Ensure we don't overflow when casting to int
		int startTimeInt = startTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) startTime;
		int endTimeInt = endTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) endTime;

		final List<RecordedPage> recPages = events.getRecordedPages();
		final Interval<Integer> editInterval = new Interval<>(startTimeInt, endTimeInt);
		final EditHeaderAction headerAction = new EditHeaderAction(header, -editInterval.lengthLong());
		final DeleteEventsAction eventsAction = new DeleteEventsAction(events, editInterval);
		final DeleteDocumentAction documentAction = new DeleteDocumentAction(doc);
		final DeleteAudioAction audioAction = new DeleteAudioAction(audio, editInterval);

		MovePrimarySelectionAction selectionAction = null;
		if (primarySelectionProperty != null && primarySelectionProperty.get() > start) {
			selectionAction = new MovePrimarySelectionAction(primarySelectionProperty, primarySelectionProperty.get() - (end - start));
		}

		final Map<Integer, Integer> timetable = getPageChangeEvents(events);

		LOG.debug("Cut recording at: {}-{} ms", startTimeInt, endTimeInt);

		for (Integer number : timetable.keySet()) {
			Integer timestamp = timetable.get(number);
			Integer nextTimestamp = timetable.get(number + 1);
			boolean containsNext = nonNull(nextTimestamp) && editInterval.contains(nextTimestamp);
			boolean isLastPage = number == (recPages.size() - 1);

			// Interval contains page select event
			if (editInterval.contains(timestamp)) {
				boolean multiplePages = recPages.size() > 1;
				boolean reachedEnd = endTime == duration;

				if (containsNext || (isLastPage && multiplePages && reachedEnd)) {
					LOG.debug("Remove page: {}", number);

					/*
					 * Interval encloses the whole page content.
					 * Remove complete page.
					 */
					eventsAction.removeRecordedPage(number);

					// Remove document page.
					documentAction.removePage(number);
				}
				else {
					LOG.debug("Shift page: {}", number);

					/*
					 * Reached last page in the interval.
					 * Remove actions on the left side and move page to
					 * interval-start.
					 */
					Interval<Integer> interval = new Interval<>(timestamp, editInterval.getEnd());
					eventsAction.changeRecordedPage(number, interval);
					eventsAction.setShiftPage(number);

					// Done.
					break;
				}
			}
			else if (containsNext) {
				LOG.debug("Remove actions on the right side of page: {}", number);

				/*
				 * Remove actions on the right side of the page.
				 */
				Interval<Integer> interval = new Interval<>(editInterval.getStart(), nextTimestamp);
				eventsAction.changeRecordedPage(number, interval);
			}
			else if (timestamp < editInterval.getStart() && (isLastPage || editInterval.getEnd() < nextTimestamp)) {
				LOG.debug("Remove actions from the middle of page: {}", number);

				/*
				 * Remove actions from the middle of the page.
				 */
				eventsAction.changeRecordedPage(number, editInterval);

				// Done.
				break;
			}
		}
		List<EditAction> actions = new ArrayList<>();

		if (selectionAction != null) {
			actions.add(selectionAction);
		}

		actions.addAll(List.of(headerAction, documentAction, eventsAction, audioAction));

		return actions;
	}

	private static Map<Integer, Integer> getPageChangeEvents(RecordedEvents events) {
		List<RecordedPage> pages = events.getRecordedPages();
		Iterator<RecordedPage> pageIterator = pages.iterator();

		Map<Integer, Integer> timetable = new LinkedHashMap<>();

		while (pageIterator.hasNext()) {
			RecordedPage recPage = pageIterator.next();
			timetable.put(recPage.getNumber(), recPage.getTimestamp());
		}

		return timetable;
	}
}
