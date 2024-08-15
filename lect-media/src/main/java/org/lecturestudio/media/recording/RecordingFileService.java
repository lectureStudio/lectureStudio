/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.media.recording;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.file.RecordingFileReader;
import org.lecturestudio.core.service.DocumentService;

public class RecordingFileService {

	private final RecordingPlaybackService playbackService;

	private final DocumentService documentService;

	private final List<Recording> recordings;

	private final EventBus eventBus;


	@Inject
	public RecordingFileService(ApplicationContext context,
			RecordingPlaybackService playbackService,
			DocumentService documentService) {
		this.eventBus = context.getEventBus();
		this.playbackService = playbackService;
		this.documentService = documentService;
		this.recordings = new ArrayList<>();
	}

	public CompletableFuture<Recording> openRecording(File file) {
		return CompletableFuture.supplyAsync(() -> {
			Recording recording;

			try {
				recording = RecordingFileReader.read(file);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			recordings.add(recording);

			eventBus.post(new RecordingEvent(recording, RecordingEvent.Type.CREATED));

			documentService.addDocument(recording.getRecordedDocument().getDocument());

			selectRecording(recording);

			return recording;
		});
	}

	public void closeSelectedRecording() {
		closeRecording(getSelectedRecording());
	}

	public void closeRecording(Recording recording) {
		if (recordings.remove(recording)) {
			eventBus.post(new RecordingEvent(recording, RecordingEvent.Type.CLOSED));

			documentService.closeDocument(recording.getRecordedDocument().getDocument());

			// Release resources.
			recording.close();
		}
	}

	public void selectRecording(Recording recording) {
		Recording oldRec = getSelectedRecording();

		// Move new recording to the tail in the list.
		if (recordings.remove(recording)) {
			recordings.add(recording);

			eventBus.post(new RecordingEvent(oldRec, recording, RecordingEvent.Type.SELECTED));

			Document document = recording.getRecordedDocument().getDocument();

			playbackService.setRecording(recording);
			documentService.selectDocument(document);
		}
	}

	public Recording getSelectedRecording() {
		if (recordings.isEmpty()) {
			return null;
		}

		return recordings.get(recordings.size() - 1);
	}

	public boolean hasRecordings() {
		return !recordings.isEmpty();
	}
}
