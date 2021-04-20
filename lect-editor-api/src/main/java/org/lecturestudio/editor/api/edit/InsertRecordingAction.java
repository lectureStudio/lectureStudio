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

import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingHeader;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.recording.edit.EditHeaderAction;
import org.lecturestudio.core.recording.edit.InsertAudioAction;
import org.lecturestudio.core.recording.edit.InsertDocumentAction;
import org.lecturestudio.core.recording.edit.InsertEventsAction;

/**
 * Inserts a {@code Recording} into another {@code Recording} that is being
 * worked on. The target recording will be extended by the full length of the
 * inserted recording, merging all parts - audio, events and slides.
 *
 * @author Alex Andres
 */
public class InsertRecordingAction extends RecordingAction {

	/**
	 * @param recording The recording on which to operate.
	 * @param target    The recording to insert.
	 * @param start     The time position in the target recording where to
	 *                  insert the new recording.
	 */
	public InsertRecordingAction(Recording recording, Recording target, double start) {
		super(recording, createActions(recording, target, start));
	}

	private static List<EditAction> createActions(Recording recording, Recording target, double start) {
		// Snap to page margin in milliseconds.
		int snapToPageMargin = 250;

		RecordingHeader header = recording.getRecordingHeader();
		RecordedAudio audio = recording.getRecordedAudio();
		RecordedDocument doc = recording.getRecordedDocument();
		RecordedEvents events = recording.getRecordedEvents();

		RecordedAudio insAudio = target.getRecordedAudio();
		RecordedDocument insDoc = target.getRecordedDocument();
		RecordedEvents insEvents = target.getRecordedEvents();

		long duration = audio.getAudioStream().getLengthInMillis();
		int insertDuration = (int) insAudio.getAudioStream().getLengthInMillis();
		int time = (int) (start * audio.getAudioStream().getLengthInMillis());
		int startIndex = recording.getPageIndex(time, snapToPageMargin);
		time = findInsertTime(recording, startIndex, time, snapToPageMargin);

		RecordedPage startPage = events.getRecordedPages().get(startIndex);
		boolean end = Math.abs(duration - time) < snapToPageMargin;
		boolean split = startPage.getTimestamp() != time && !end;
		startIndex = end ? startIndex + 1 : startIndex;

		EditHeaderAction headerAction = new EditHeaderAction(header, insertDuration);
		InsertEventsAction eventsAction = new InsertEventsAction(events, insEvents, split, time, startIndex, insertDuration);
		InsertDocumentAction documentAction = new InsertDocumentAction(doc, insDoc, split, time, startIndex);
		InsertAudioAction audioAction = new InsertAudioAction(audio, insAudio, time);

		return List.of(headerAction, documentAction, eventsAction, audioAction);
	}

	/**
	 * Find the insertion time with the possibility to snap to pages.
	 *
	 * @return the insertion time.
	 */
	private static int findInsertTime(Recording recording, int startIndex,
			int startTime, int snapToPageMargin) {
		List<RecordedPage> recPages = recording.getRecordedEvents().getRecordedPages();
		RecordedPage startPage = recPages.get(startIndex);

		if (Math.abs(startPage.getTimestamp() - startTime) < snapToPageMargin) {
			return startPage.getTimestamp();
		}

		return startTime;
	}
}
