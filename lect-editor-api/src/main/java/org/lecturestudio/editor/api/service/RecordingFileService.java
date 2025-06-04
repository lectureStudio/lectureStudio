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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.RecordingChangeListener;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.recording.file.RecordingFileReader;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.recording.file.RecordingUtils;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.edit.*;
import org.lecturestudio.media.audio.FFmpegLoudnessNormalization;
import org.lecturestudio.media.audio.LoudnessConfiguration;
import org.lecturestudio.media.recording.RecordingEvent;

@Singleton
public class RecordingFileService {

	private static final Logger LOG = LogManager.getLogger(RecordingFileService.class);

	private final RecordingChangeListener recordingChangeListener = this::recordingChanged;

	private final EditorContext context;

	private final RecordingPlaybackService playbackService;

	private final DocumentService documentService;

	private final List<Recording> recordings;

	private final Map<Recording, Integer> recordingStateMap;

	private final EventBus eventBus;

	private final Map<Recording, LoudnessConfiguration> loudnessConfigurationMap;


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
		this.loudnessConfigurationMap = new HashMap<>();
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

			computeLoudnessConfiguration(recording);

			return recording;
		});
	}

	private void computeLoudnessConfiguration(Recording recording) {
		CompletableFuture.runAsync(() -> {
			FFmpegLoudnessNormalization normalization = new FFmpegLoudnessNormalization();
			try {
				loudnessConfigurationMap.put(recording,
						normalization.retrieveInformation(recording.getRecordedAudio().getAudioStream(),
								(progress -> {
									// Don't do anything with progress information for now
								})));
			}
			catch (IOException e) {
				// ignore
			}
		});
	}

	public CompletableFuture<Recording> importRecording(File file, double start, boolean normalizeNewAudio,
														ProgressCallback callback) {
		return CompletableFuture.supplyAsync(() -> {
			Recording recording = getSelectedRecording();
			Recording imported;

			try {
				imported = RecordingFileReader.read(file);

				addEditAction(recording, new InsertRecordingAction(recording,
						imported, start, normalizeNewAudio, loudnessConfigurationMap.get(recording), callback));
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
			eventBus.post(new RecordingEvent(recording, RecordingEvent.Type.CLOSED));

			documentService.closeDocument(recording.getRecordedDocument().getDocument());

			playbackService.closeRecording();

			// Release resources.
			recording.close();
		}
	}

	public void selectRecording(Recording recording) {
		Recording oldRec = getSelectedRecording();

		if (nonNull(oldRec)) {
			oldRec.removeRecordingChangeListener(recordingChangeListener);
		}
		if (nonNull(recording)) {
			recording.addRecordingChangeListener(recordingChangeListener);

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

	}

	public Recording getSelectedRecording() {
		if (recordings.isEmpty()) {
			return null;
		}

		return recordings.get(recordings.size() - 1);
	}

	/**
	 * Searches all opened recordings and returns the recording that contains
	 * the specified document.
	 *
	 * @param document The document of an opened recording.
	 *
	 * @return The recording that contains the provided document.
	 */
	public Recording getRecordingWithDocument(Document document) {
		if (recordings.isEmpty() || isNull(document)) {
			return null;
		}

		Recording selectedRecording = getSelectedRecording();

		if (selectedRecording.getRecordedDocument().getDocument().getName()
				.equals(document.getName())) {
			// Skip if looking for opened recording.
			return selectedRecording;
		}

		for (Recording recording : recordings) {
			if (recording.getRecordedDocument().getDocument().getName()
					.equals(document.getName())) {
				return recording;
			}
		}

		return null;
	}

	public boolean hasRecordings() {
		return !recordings.isEmpty();
	}

	/**
	 * Removes a portion of a recording specified by a time interval. All
	 * recorded parts - audio, events and slides - contained within the interval
	 * will be removed from the recording.
	 *
	 * @param start The start time from where to start removing. The value must
	 *              be within the range [0, 1].
	 * @param end   The end time when to stop removing. The value must be within
	 *              the range [0, 1].
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> cut(double start, double end) {
		return cut(start, end, getSelectedRecording());
	}

	/**
	 * Removes a portion of a recording specified by a time interval. All
	 * recorded parts - audio, events and slides - contained within the interval
	 * will be removed from the recording.
	 *
	 * @param start     The start time from where to start removing. The value
	 *                  must be within the range [0, 1].
	 * @param end       The end time when to stop removing. The value must be
	 *                  within the range [0, 1].
	 * @param recording The recording this edit action should happen on
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> cut(double start, double end, Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				double durationOld = recording.getRecordingHeader().getDuration();

				addEditAction(recording, new CutAction(recording, start, end));

				double durationNew = recording.getRecordingHeader().getDuration();
				double scale = durationOld / durationNew;

				// Set time marker to the cut position.
				context.setPrimarySelection(start * scale);
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

	public CompletableFuture<Void> replaceAllPages(Document newDoc) {
		Recording recording = getSelectedRecording();

		// PDDocument is getting garbage collected, when it's not actively referenced,
		// saving the document in a temporary variable might keep the reference alive and make sure it won't get GCed
		Runnable runnable = new Runnable() {
			final Document document = newDoc;

			@Override
			public void run() {
				try {
					addEditAction(recording, new ReplaceAllPagesAction(recording, document));
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			}
		};

		return CompletableFuture.runAsync(runnable);
	}

	public CompletableFuture<Void> insertPage(Document newDoc) {
		Recording recording = getSelectedRecording();
		double timePos = context.getPrimarySelection();

		// PDDocument is getting garbage collected, when it's not actively referenced,
		// saving the document in a temporary variable might keep the reference alive and make sure it won't get GCed
		Runnable runnable = new Runnable() {
			final Document document = newDoc;

			@Override
			public void run() {
				try {
					addEditAction(recording, new InsertPdfPageAction(recording, document, timePos));
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			}
		};

		return CompletableFuture.runAsync(runnable);
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

				try (RandomAccessAudioStream newAudioStream = new RandomAccessAudioStream(target)) {
					newAudioStream.reset();

					Recording rec = new Recording();
					rec.setRecordingHeader(recording.getRecordingHeader());
					rec.setRecordedAudio(new RecordedAudio(newAudioStream));
					rec.setRecordedEvents(recording.getRecordedEvents());
					rec.setRecordedDocument(recording.getRecordedDocument());

					RecordingFileWriter.write(rec, file, callback);
				}

				if (file.equals(recording.getSourceFile())) {
					// File overwritten. Need to update the audio stream.
					recording.setRecordedAudio(RecordingFileReader.getRecordedAudio(file));
				}

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

	/**
	 * Saves the part of the recording that is specified through the interval.
	 * Removes the saved part from the current recording.
	 *
	 * @param file     The file to save the recording in
	 * @param interval The interval of the recording that should be saved, in
	 *                 milliseconds
	 * @param callback Progress callback for the saving progress
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> savePartialRecording(File file,
			Interval<Long> interval, ProgressCallback callback)
			throws RecordingEditException {
		return savePartialRecording(file, interval, callback,
				getSelectedRecording());
	}

	/**
	 * Saves the part of the recording that is specified through the interval.
	 * Removes the saved part from the current recording.
	 *
	 * @param file      The file to save the recording in
	 * @param interval  The interval of the recording that should be saved, in
	 *                  milliseconds
	 * @param callback  Progress callback for the saving progress
	 * @param recording The recording that should be saved partially
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> savePartialRecording(File file,
			Interval<Long> interval, ProgressCallback callback,
			Recording recording) throws RecordingEditException {
		try {
			Recording partial = new Recording(recording);

			long duration = partial.getRecordingHeader().getDuration();
			double start = (double) interval.getStart() / duration;
			double end = (double) interval.getEnd() / duration;
			CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

			return future.thenCompose(ignored -> {
						// Checks whether the interval to get extracted starts in the beginning of the recording
						// if it does not we have to cut out the section before the interval
						if (start != 0) {
							return cut(0, start, partial);
						}
						return CompletableFuture.completedFuture(null);
					})
					.thenCompose(ignored -> {
						// Checks whether the interval to get extracted ends at the end of the recording
						// if it does not we have to cut out the section before the interval
						if (end != 1) {
							return cut(end, 1, partial);
						}
						return CompletableFuture.completedFuture(null);
					})
					// Then we save the extracted part
					.thenCompose(ignored -> saveRecording(partial, file, callback))
					// And remove the same interval in the selected recording
					.thenCompose(ignored -> CompletableFuture.runAsync(() -> {
						try {
							addEditAction(getSelectedRecording(),
									new CutAction(getSelectedRecording(), start,
											end,
											context.primarySelectionProperty()));
						}
						catch (RecordingEditException e) {
							throw new RuntimeException(e);
						}
					}));
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	public CompletableFuture<Void> exportAudio(File file,
			ProgressCallback callback) {
		return exportAudio(getSelectedRecording(), file, callback);
	}

	public CompletableFuture<Void> exportAudio(Recording recording, File file,
			ProgressCallback callback) {
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
		Recording.Content content = event.getContentType();
		Recording recording = event.getRecording();
		Document document = recording.getRecordedDocument().getDocument();

		context.setSeeking(true);

		double prevDuration = playbackService.getDuration().getMillis();
		double scale = prevDuration / recording.getRecordedAudio().getAudioStream().getLengthInMillis();

		switch (content) {
			case ALL, DOCUMENT, HEADER, AUDIO: {
				documentService.replaceDocument(document);
				break;
			}
		}
		// Do not refresh the recording if new events got added, because the view got already handled by the UI.
		switch (content) {
			case ALL, DOCUMENT, HEADER, AUDIO, EVENTS_REMOVED, EVENTS_CHANGED -> {
				playbackService.setRecording(recording);

				updateEditState(recording);
			}
			default -> {
				// Do nothing
			}
		}

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

		try {
			playbackService.seek(selection);
		}
		catch (ExecutableException e) {
			LOG.error("Seek failed", e);
		}

		context.setSeeking(false);
		context.setPrimarySelection(selection);
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

	/**
	 * Sets the timestamp of the page with the selected number, without
	 * affecting other parts of the recording, like the audio or the total
	 * duration of the recording.
	 *
	 * @param timestamp  The new timestamp
	 * @param pageNumber The number of the page to move the timestamp
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> movePage(int timestamp, int pageNumber) {
		return movePage(timestamp, pageNumber, getSelectedRecording());
	}

	/**
	 * Sets the timestamp of the page with the selected number, without
	 * affecting other parts of the recording, like the audio or the total
	 * duration of the recording.
	 *
	 * @param timestamp  The new timestamp
	 * @param pageNumber The number of the page to move the timestamp
	 * @param recording  The recording this should happen in
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> movePage(int timestamp, int pageNumber,
			Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				addEditAction(recording,
						new MovePageAction(recording, pageNumber, timestamp));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	/**
	 * Removes the page with the selected number, without affecting other parts
	 * of the recording, like the audio or the total duration of the recording.
	 *
	 * @param pageNumber The number of the page to remove
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> hidePage(int pageNumber) {
		return hidePage(pageNumber, getSelectedRecording());
	}

	/**
	 * Removes the page with the selected number, without affecting other parts
	 * of the recording, like the audio or the total duration of the recording.
	 *
	 * @param pageNumber The number of the page to remove
	 * @param recording  The recording this should happen in
	 *
	 * @return An async future completing the task
	 */
	public CompletableFuture<Void> hidePage(int pageNumber,
			Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				addEditAction(recording, new HidePageAction(recording, pageNumber));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> hideAndMoveNextPage(int pageNumber, int timestamp) {
		return hideAndMoveNextPage(pageNumber, timestamp, getSelectedRecording());
	}

	public CompletableFuture<Void> hideAndMoveNextPage(int pageNumber,
			int timestamp, Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				addEditAction(recording,
						new HideAndMoveNextPageAction(recording, pageNumber,
								timestamp));
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	/**
	 * Inserts new actions into a page and optionally removes un-used ones.
	 *
	 * @param addActions    The actions to be added.
	 * @param removeActions The actions to be removed.
	 * @param pageNumber    The page number into which the actions should be
	 *                      added.
	 *
	 * @return An async task performing the task.
	 */
	public CompletableFuture<Void> insertPlaybackActions(
			List<PlaybackAction> addActions,
			List<PlaybackAction> removeActions, int pageNumber) {
		return insertPlaybackActions(addActions, removeActions, pageNumber,
				getSelectedRecording());
	}

	public CompletableFuture<Void> insertPlaybackActions(
			List<PlaybackAction> addActions,
			List<PlaybackAction> removeActions, int pageNumber,
			Recording recording) {
		return CompletableFuture.runAsync(() -> {
				// Add and remove actions with a single composite action which
				// can be undone in one step.
				var changeAction = new ReplacePageEventsAction(recording,
						addActions, removeActions, pageNumber);

				try {
					addEditAction(recording, changeAction);
				}
				catch (Throwable e) {
					throw new CompletionException(e);
				}
			})
			.thenCompose((ignored) -> {
				double endTimestamp = (double) (
						addActions.get(addActions.size() - 1).getTimestamp()
								+ 2) / recording.getRecordingHeader().getDuration();
				context.setPrimarySelection(endTimestamp);
				return null;
			});
	}

	/**
	 * {@link ModifyPlaybackActionPositionsAction}
	 *
	 * @param handle     the associated shape handle
	 * @param pageNumber the number of the page the actions should be modified
	 * @param delta      the amount the locations should be changed by
	 *
	 * @return an async task performing the task
	 */
	public CompletableFuture<Void> modifyPlaybackActionPositions(int handle,
			int pageNumber, PenPoint2D delta) {
		return modifyPlaybackActionPositions(handle, pageNumber, delta,
				getSelectedRecording());
	}

	public CompletableFuture<Void> modifyPlaybackActionPositions(int handle,
			int pageNumber, PenPoint2D delta, Recording recording) {
		double timestampBefore = context.getPrimarySelection();
		return CompletableFuture.runAsync(() -> {
				try {
					addEditAction(recording,
							new ModifyPlaybackActionPositionsAction(recording,
									handle, pageNumber, delta));
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			})
			.thenCompose((ignored) -> {
				context.setPrimarySelection(timestampBefore);
				return null;
			});
	}

	public CompletionStage<Void> normalizeAudioLoudness(Double lufsValue,
			ProgressCallback callback) {
		return normalizeAudioLoudness(lufsValue, callback,
				getSelectedRecording());
	}

	public CompletionStage<Void> normalizeAudioLoudness(Double lufsValue,
			ProgressCallback callback, Recording recording) {
		return CompletableFuture.runAsync(() -> {
				try {
					addEditAction(recording,
							new NormalizeLoudnessAction(recording, lufsValue,
									callback));
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			})
			.thenCompose((ignored) -> {
				// Recompute Loudness Configuration, to be used for subsequent audio edits
				computeLoudnessConfiguration(recording);
				return CompletableFuture.completedFuture(null);
			});
	}
}
