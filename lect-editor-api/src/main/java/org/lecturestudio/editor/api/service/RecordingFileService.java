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

package org.lecturestudio.editor.api.service;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.recording.edit.ReplaceAudioAction;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.RecordingChangeListener;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.file.RecordingFileReader;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.recording.file.RecordingUtils;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.edit.CutAction;
import org.lecturestudio.editor.api.edit.DeletePageAction;
import org.lecturestudio.editor.api.edit.InsertRecordingAction;
import org.lecturestudio.editor.api.edit.ReplacePageAction;
import org.lecturestudio.media.recording.RecordingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class RecordingFileService {

	private final static Logger LOG = LogManager.getLogger(RecordingFileService.class);

	private final RecordingChangeListener recordingChangeListener = this::recordingChanged;

	private final EditorContext context;

	private final RecordingPlaybackService playbackService;

	private final DocumentService documentService;

	private final List<Recording> recordings;

	private final Map<Recording, Integer> recordingStateMap;

	private final EventBus eventBus;


	@Inject
	RecordingFileService(ApplicationContext context,
			RecordingPlaybackService playbackService,
			DocumentService documentService) {
		this.context = (EditorContext) context;
		this.eventBus = context.getEventBus();
		this.playbackService = playbackService;
		this.documentService = documentService;
		this.recordings = new ArrayList<>();
		this.recordingStateMap = new HashMap<>();
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

	public CompletableFuture<Recording> importRecording(File file, double start) {
		return CompletableFuture.supplyAsync(() -> {
			Recording recording = getSelectedRecording();
			Recording imported;

			try {
				imported = RecordingFileReader.read(file);

				addEditAction(recording, new InsertRecordingAction(recording,
						imported, start));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			return imported;
		});
	}

	public void closeSelectedRecording() {
		closeRecording(getSelectedRecording());
	}

	public void closeRecording(Recording recording) {
		if (recordings.remove(recording)) {
			// Release resources.
			recording.close();

			eventBus.post(new RecordingEvent(recording, RecordingEvent.Type.CLOSED));

			documentService.closeDocument(recording.getRecordedDocument().getDocument());
		}
	}

	public void selectRecording(Recording recording) {
		Recording oldRec = getSelectedRecording();

		if (nonNull(oldRec)) {
			oldRec.removeRecordingChangeListener(recordingChangeListener);
		}
		if (nonNull(recording)) {
			recording.addRecordingChangeListener(recordingChangeListener);
		}

		// Move new recording to the tail in the list.
		if (recordings.remove(recording)) {
			recordings.add(recording);

			eventBus.post(new RecordingEvent(oldRec, recording, RecordingEvent.Type.SELECTED));

			Document document = recording.getRecordedDocument().getDocument();

			playbackService.setRecording(recording);
			documentService.selectDocument(document);

			updateEditState(recording);
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

	public CompletableFuture<Void> cut(double start, double end) {
		return cut(start, end, getSelectedRecording());
	}

	public CompletableFuture<Void> cut(double start, double end, Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				addEditAction(recording, new CutAction(recording, start, end));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> deletePage(double timeNorm) {
		return deletePage(timeNorm, getSelectedRecording());
	}

	public CompletableFuture<Void> deletePage(double timeNorm, Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				addEditAction(recording, new DeletePageAction(recording, timeNorm));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> replacePage(Document newDoc) {
		Recording recording = getSelectedRecording();

		return CompletableFuture.runAsync(() -> {
			try {
				addEditAction(recording, new ReplacePageAction(recording, newDoc));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> undoChanges() {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				Recording recording = getSelectedRecording();
				recording.getEditManager().undo();

				updateEditState(recording);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> redoChanges() {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				Recording recording = getSelectedRecording();
				recording.getEditManager().redo();

				updateEditState(recording);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> saveRecording(File file, ProgressCallback callback) {
		return saveRecording(getSelectedRecording(), file, callback);
	}

	public CompletableFuture<Void> saveRecording(Recording recording, File file, ProgressCallback callback) {
		return CompletableFuture.runAsync(() -> {
			RandomAccessAudioStream audioStream = recording.getRecordedAudio().getAudioStream().clone();
			Path tmpPath = context.getTempDirectory().toPath();
			File target = null;

			try {
				target = new File(Files.createTempFile(tmpPath, "export", "wav").toString());

				try (FileOutputStream outStream = new FileOutputStream(target)) {
					audioStream.reset();
					audioStream.write(outStream.getChannel());
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}

				RandomAccessAudioStream newAudioStream = new RandomAccessAudioStream(target);
				newAudioStream.reset();

				Recording rec = new Recording();
				rec.setRecordingHeader(recording.getRecordingHeader());
				rec.setRecordedAudio(new RecordedAudio(newAudioStream));
				rec.setRecordedEvents(recording.getRecordedEvents());
				rec.setRecordedDocument(recording.getRecordedDocument());

				RecordingFileWriter.write(rec, file, callback);

				newAudioStream.close();

				recordingStateMap.put(recording, recording.getStateHash());
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
			finally {
				if (nonNull(target)) {
					target.delete();
				}
			}
		});
	}

	public CompletableFuture<Void> exportAudio(File file, ProgressCallback callback) {
		return exportAudio(getSelectedRecording(), file, callback);
	}

	public CompletableFuture<Void> exportAudio(Recording recording, File file, ProgressCallback callback) {
		return CompletableFuture.runAsync(() -> {
			try {
				RecordingUtils.exportAudio(recording, file, callback);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> importAudio(File file) {
		return importAudio(file, getSelectedRecording());
	}

	public CompletableFuture<Void> importAudio(File file, Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				RandomAccessAudioStream stream = new RandomAccessAudioStream(file);

				RecordedAudio recordedAudio = recording.getRecordedAudio();

				ReplaceAudioAction editAction = new ReplaceAudioAction(recordedAudio, stream);
				editAction.execute();
				recordedAudio.addEditAction(editAction);

				recording.setRecordedAudio(recordedAudio);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public Integer getRecordingSaveHash(Recording recording) {
		return recordingStateMap.get(recording);
	}

	private void recordingChanged(RecordingChangeEvent event) {
		Recording recording = event.getRecording();
		Document document = recording.getRecordedDocument().getDocument();

		double prevDuration = playbackService.getDuration().getMillis();
		double scale = prevDuration / recording.getRecordedAudio().getAudioStream().getLengthInMillis();

		documentService.replace(document);
		playbackService.setRecording(recording);

		updateEditState(recording);

		context.getEventBus().post(event);

		// Adjust the position of the selection.
		double selection;

		if (nonNull(event.getDuration())) {
			selection = Math.max(0, Math.min(1.0, event.getDuration().getStart()));
			selection *= scale;
		}
		else {
			double pos = Math.min(context.getLeftSelection(), context.getRightSelection());
			selection = Math.min(1.0, pos * scale);
		}

		context.setPrimarySelection(selection);

		try {
			playbackService.seek(selection);
		}
		catch (ExecutableException e) {
			LOG.error("Seek failed", e);
		}
	}

	private void suspendPlayback() throws ExecutableException {
		if (playbackService.started()) {
			playbackService.suspend();
		}
	}

	private void addEditAction(Recording recording, EditAction action)
			throws RecordingEditException {
		recording.getEditManager().addEditAction(action);

		updateEditState(recording);
	}

	private void updateEditState(Recording recording) {
		Document document = recording.getRecordedDocument().getDocument();

		context.setCanDeletePage(document.getPageCount() > 1);
		context.setCanRedo(recording.getEditManager().hasRedoActions());
		context.setCanUndo(recording.getEditManager().hasUndoActions());
	}
}
