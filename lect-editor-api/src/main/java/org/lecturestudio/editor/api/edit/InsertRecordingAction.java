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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.recording.*;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.recording.file.RecordingUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.audio.LoudnessConfiguration;

import static java.util.Objects.nonNull;

/**
 * Inserts a {@code Recording} into another {@code Recording} that is being
 * worked on. The target recording will be extended by the full length of the
 * inserted recording, merging all parts - audio, events and slides.
 *
 * @author Alex Andres
 */
public class InsertRecordingAction extends RecordingAction {

	/**
	 * A callback that is executed when undoing the action if video was inserted.
	 * This runnable is called during the {@link #undo()} method if any
	 * {@link CopyScreenRecordingsAction} is present in the edit actions.
	 * Can be {@code null}.
	 */
	private final Runnable onUndoVideo;


	/**
	 * @param recording         The recording on which to operate.
	 * @param target            The recording to insert.
	 * @param start             The time position in the target recording where to insert the new recording.
	 * @param normalizeNewAudio Whether the new audio track should have the same loudness as the existing audio track.
	 * @param configuration     The loudness configuration to use for normalization.
	 * @param callback          A progress callback to report progress during audio processing.
	 * @param onUndoVideo       A callback that is executed when undoing the action if video was inserted.
	 *                          Can be {@code null}.
	 */
	public InsertRecordingAction(Recording recording, Recording target, double start, boolean normalizeNewAudio,
								 LoudnessConfiguration configuration, ProgressCallback callback, Runnable onUndoVideo) {
		super(recording, createActions(recording, target, start, normalizeNewAudio, configuration, callback));

		this.onUndoVideo = onUndoVideo;
	}

	@Override
	public void undo() throws RecordingEditException {
		if (editActions.stream().anyMatch(CopyScreenRecordingsAction.class::isInstance) && nonNull(onUndoVideo)) {
			try {
				onUndoVideo.run();
			}
			catch (Exception e) {
				throw new RecordingEditException("Failed to execute onUndoVideo callback.", e);
			}
		}

		super.undo();
	}

	private static List<EditAction> createActions(Recording recording, Recording target, double start,
												  boolean normalizeNewAudio, LoudnessConfiguration configuration,
												  ProgressCallback callback) {
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
		InsertAudioAction audioAction = new InsertAudioAction(audio, insAudio, time, normalizeNewAudio, configuration, callback);

		List<EditAction> actions = new ArrayList<>(List.of(headerAction, documentAction, eventsAction, audioAction));
		List<ScreenAction> screenActions = RecordingUtils.getScreenActions(target);

		if (!screenActions.isEmpty()) {
			Path sourcePath = Paths.get(target.getSourceFile().getParent());
			Path targetPath = Paths.get(recording.getSourceFile().getParent());
			actions.add(new CopyScreenRecordingsAction(sourcePath, targetPath, screenActions));
		}

		return actions;
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
