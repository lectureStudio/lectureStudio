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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
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
import org.lecturestudio.core.recording.*;
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

/**
 * Service class for managing recording files within the lecture editor application.
 * <p>
 * This service provides functionality for:
 * - Opening, saving, and closing recordings
 * - Editing recordings (cut, delete pages, replace audio, etc.)
 * - Managing recording selections and state
 * - Importing and exporting recordings and audio
 * - Handling undo/redo operations
 * - Audio processing operations like loudness normalization
 * <p>
 * The service maintains the state of opened recordings and coordinates with other
 * services like document and playback services to provide a cohesive editing experience.
 *
 * @author Alex Andres
 */
@Singleton
public class RecordingFileService {

	private static final Logger LOG = LogManager.getLogger(RecordingFileService.class);

	/** Listener for recording change events that delegates to the recordingChanged method. */
	private final RecordingChangeListener recordingChangeListener = this::recordingChanged;

	/** The editor application context providing access to editor-specific functionality. */
	private final EditorContext context;

	/** Service for controlling playback of recordings. */
	private final RecordingPlaybackService playbackService;

	/** Service for handling document operations. */
	private final DocumentService documentService;

	/** List of all currently opened recordings. */
	private final List<Recording> recordings;

	/** Maps recordings to their state hash values to track modifications. */
	private final Map<Recording, Integer> recordingStateMap;

	/** Event bus for publishing and subscribing to application events. */
	private final EventBus eventBus;

	/** Maps recordings to their audio loudness configurations for normalization operations. */
	private final Map<Recording, LoudnessConfiguration> loudnessConfigurationMap;


	/**
	 * Creates a new RecordingFileService instance.
	 *
	 * @param context         The application context that will be cast to an EditorContext.
	 * @param playbackService The service for playing back recordings.
	 * @param documentService The service for handling documents.
	 */
	@Inject
	RecordingFileService(ApplicationContext context, RecordingPlaybackService playbackService,
						 DocumentService documentService) {
		this.context = (EditorContext) context;
		this.eventBus = context.getEventBus();
		this.playbackService = playbackService;
		this.documentService = documentService;
		this.recordings = new ArrayList<>();
		this.recordingStateMap = new HashMap<>();
		this.loudnessConfigurationMap = new HashMap<>();
	}

	/**
	 * Opens a recording from the specified file asynchronously.
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Reads the recording file using {@link RecordingFileReader}</li>
	 *   <li>Adds the recording to the internal list of active recordings</li>
	 *   <li>Publishes a {@link RecordingEvent} with type CREATED to notify listeners</li>
	 *   <li>Registers the recording's document with the document service</li>
	 *   <li>Makes the opened recording the currently selected recording</li>
	 *   <li>Initiates asynchronous computation of audio loudness configuration</li>
	 * </ul>
	 * <p>
	 * The method executes asynchronously and returns a CompletableFuture that can be used
	 * to track the operation's progress and handle potential errors.
	 *
	 * @param file The file from which to open the recording. Must be a valid recording file.
	 *
	 * @return A CompletableFuture that completes with the opened Recording on success,
	 * or completes exceptionally with a {@link CompletionException} wrapping the
	 * underlying exception if an error occurs during reading or processing.
	 *
	 * @see RecordingFileReader#read(File)
	 * @see RecordingEvent
	 */
	public CompletableFuture<Recording> openRecording(File file) {
		return CompletableFuture.supplyAsync(() -> {
			Recording recording;

			try {
				recording = RecordingFileReader.read(file);

				RecordingUtils.validateScreenActions(recording);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}

			// Add the successfully read recording to the list of active recordings.
			recordings.add(recording);

			// Notify the system via eventBus that a new recording has been created.
			eventBus.post(new RecordingEvent(recording, RecordingEvent.Type.CREATED));

			// Register the recording's document with the document service.
			documentService.addDocument(recording.getRecordedDocument().getDocument());

			// Make this the currently selected/active recording.
			selectRecording(recording);

			// Begin asynchronous computation of audio loudness configuration
			// for potential normalization operations.
			computeLoudnessConfiguration(recording);

			return recording;
		});
	}

	/**
	 * Convenience helper to open a recording and add it to the recent documents list.
	 * This eliminates duplicated code in presenters when opening a recording.
	 *
	 * @param file the recording file to open
	 * @param recentDocumentService the service to update with the opened file
	 * @return a CompletableFuture that completes when the recording is opened and the recent list is updated
	 */
	public CompletableFuture<Void> openRecordingAndAddToRecent(File file, org.lecturestudio.core.service.RecentDocumentService recentDocumentService) {
		return openRecording(file).thenAccept(recording -> {
			org.lecturestudio.core.model.RecentDocument recentDoc = new org.lecturestudio.core.model.RecentDocument();
			recentDoc.setDocumentName(org.lecturestudio.core.util.FileUtils.stripExtension(file.getName()));
			recentDoc.setDocumentPath(file.getAbsolutePath());
			recentDoc.setLastModified(new java.util.Date());
			recentDocumentService.add(recentDoc);
		});
	}

	/**
	 * Unified handler for exceptions occurring while opening a recording file.
	 * This consolidates duplicated exceptionally-block logic across presenters.
	 *
	 * @param throwable The throwable from the async operation.
	 * @param file      The file being opened (used for error context).
	 */
	public void handleOpenRecordingException(Throwable throwable, File file) {
		Throwable cause = throwable != null ? throwable.getCause() : null;
		if (cause instanceof FileNotFoundException) {
			String message = MessageFormat.format(context.getDictionary()
					.get("open.recording.screen.file.error"), cause.getMessage());
			handleException(throwable, "Recorded screen file not found", "open.recording.error", message);
		}
		else {
			String message = file != null ? file.getPath() : null;
			handleException(throwable, "Open recording failed", "open.recording.error", message);
		}
	}

	/**
	 * Computes the loudness configuration for a recording's audio asynchronously.
	 * <p>
	 * This method analyzes the audio stream to determine its loudness characteristics
	 * and stores the configuration in the loudnessConfigurationMap for later use in
	 * normalization operations. The computation is performed on a background thread
	 * to avoid blocking the main thread.
	 * <p>
	 * Any exceptions that occur during the computation are silently caught and ignored.
	 *
	 * @param recording The recording for which to compute the loudness configuration.
	 */
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

	/**
	 * Imports a recording from a file and inserts it into the currently selected recording at the specified position.
	 * <p>
	 * This method asynchronously performs the following operations:
	 * <ul>
	 *   <li>Reads the recording from the specified file</li>
	 *   <li>Inserts the imported recording into the currently selected recording at the specified start time</li>
	 *   <li>Optionally normalizes the audio of the imported recording</li>
	 * </ul>
	 * <p>
	 * The operation is performed on a background thread, and its progress can be monitored through the provided
	 * callback.
	 *
	 * @param file              The file from which to import the recording.
	 * @param start             The position in the currently selected recording where the imported recording should be
	 *                          inserted (normalized time between 0 and 1).
	 * @param normalizeNewAudio Whether the audio of the imported recording should be normalized.
	 * @param callback          The progress callback to monitor the import progress.
	 *
	 * @return A CompletableFuture that completes with the imported Recording on success,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Closes the currently selected recording by delegating to {@link #closeRecording(Recording)}.
	 * This is a convenience method that automatically retrieves the selected recording.
	 */
	public void closeSelectedRecording() {
		closeRecording(getSelectedRecording());
	}

	/**
	 * Closes the specified recording and cleans up related resources.
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Removes the recording from the internal list of recordings</li>
	 *   <li>Posts a CLOSED event to the event bus</li>
	 *   <li>Closes the associated document</li>
	 *   <li>Notifies the playback service</li>
	 *   <li>Releases resources associated with the recording</li>
	 * </ul>
	 *
	 * @param recording The recording to close. If null or not in the recording list, nothing happens.
	 */
	public void closeRecording(Recording recording) {
		if (recordings.remove(recording)) {
			eventBus.post(new RecordingEvent(recording, RecordingEvent.Type.CLOSED));

			documentService.closeDocument(recording.getRecordedDocument().getDocument());

			playbackService.closeRecording();

			// Release resources.
			recording.close();
		}
	}

	/**
	 * Selects the specified recording as the currently active recording.
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Removes the recording change listener from the previously selected recording</li>
	 *   <li>Adds the recording change listener to the newly selected recording</li>
	 *   <li>Moves the selected recording to the end of the recording list</li>
	 *   <li>Posts a SELECTED event to the event bus</li>
	 *   <li>Updates the playback service with the new recording</li>
	 *   <li>Updates the document service with the recording's document</li>
	 *   <li>Updates the edit state flags based on the recording's edit history</li>
	 * </ul>
	 *
	 * @param recording The recording to select. If null, only the listener is removed from the previous recording.
	 */
	public void selectRecording(Recording recording) {
		Recording oldRec = getSelectedRecording();

		if (nonNull(oldRec)) {
			oldRec.removeRecordingChangeListener(recordingChangeListener);
		}
		if (nonNull(recording)) {
			recording.addRecordingChangeListener(recordingChangeListener);

			// Move the new recording to the tail in the list.
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

	/**
	 * Returns the currently selected recording.
	 * <p>
	 * The selected recording is defined as the last recording in the recordings list.
	 * This design allows for a simple implementation of the recording selection history,
	 * where newly selected recordings are moved to the end of the list.
	 *
	 * @return The currently selected recording, or null if no recordings are available.
	 */
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
			// Skip if looking for the opened recording.
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

	/**
	 * Checks whether there are any recordings currently open in the service.
	 *
	 * @return {@code true} if there is at least one recording open, {@code false} otherwise.
	 */
	public boolean hasRecordings() {
		return !recordings.isEmpty();
	}

	/**
	 * Removes a portion of a recording specified by a time interval. All
	 * recorded parts - audio, events, and slides - contained within the interval
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
	 * recorded parts - audio, events, and slides - contained within the interval
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

				if (RecordingUtils.containsScreenSection(start, end, recording)) {
					context.showError("cut.recording.error", "cut.recording.screen.error");
					return;
				}

				double durationOld = recording.getRecordingHeader().getDuration();

				addEditAction(recording, new CutAction(recording, start, end));

				double durationNew = recording.getRecordingHeader().getDuration();
				double scale = durationOld / durationNew;

				// Set the time marker to the cut position.
				context.setPrimarySelection(start * scale);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	/**
	 * Deletes a page at the specified normalized time position in the currently selected recording.
	 * <p>
	 * This is a convenience method that delegates to {@link #deletePage(double, Recording)}
	 * with the currently selected recording.
	 *
	 * @param timeNorm The normalized time position (between 0 and 1) that specifies which page to delete.
	 *
	 * @return A CompletableFuture that completes when the page deletion operation is done,
	 * or completes exceptionally if an error occurs.
	 */
	public CompletableFuture<Void> deletePage(double timeNorm) {
		return deletePage(timeNorm, getSelectedRecording());
	}

	/**
	 * Deletes a page at the specified normalized time position in the given recording.
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Suspends playback if currently active</li>
	 *   <li>Creates and executes a DeletePageAction on the recording</li>
	 *   <li>Updates the recording's edit history</li>
	 * </ul>
	 * <p>
	 * The operation is performed asynchronously on a background thread.
	 *
	 * @param timeNorm  The normalized time position (between 0 and 1) that specifies which page to delete.
	 * @param recording The recording from which to delete the page.
	 *
	 * @return A CompletableFuture that completes when the page deletion operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Replaces all pages in the currently selected recording with the pages from the provided document.
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Gets the currently selected recording</li>
	 *   <li>Creates a new edit action to replace all pages</li>
	 *   <li>Adds this action to the recording's edit history</li>
	 * </ul>
	 * <p>
	 * The operation is performed asynchronously on a background thread. A special
	 * reference to the document is maintained to prevent garbage collection issues
	 * with PDDocument objects.
	 *
	 * @param newDoc The document containing the pages that will replace the current document's pages.
	 *
	 * @return A CompletableFuture that completes when the page replacement operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Inserts a new page from the provided document into the currently selected recording
	 * at the current time position.
	 * <p>
	 * This method asynchronously inserts a page from the provided document at the
	 * current time position indicated by the primary selection in the context.
	 * The operation creates an edit action that can be undone later.
	 * <p>
	 * A special reference to the document is maintained to prevent garbage collection
	 * issues with PDDocument objects.
	 *
	 * @param newDoc The document containing the page to insert into the recording.
	 *
	 * @return A CompletableFuture that completes when the page insertion operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Undoes the last edit action performed on the currently selected recording.
	 * <p>
	 * This method asynchronously performs the following operations:
	 * <ul>
	 *   <li>Suspends playback if it is currently active</li>
	 *   <li>Gets the currently selected recording</li>
	 *   <li>Calls the undo method on the recording's edit manager</li>
	 *   <li>Updates the edit state flags (can undo, can redo, etc.)</li>
	 * </ul>
	 *
	 * @return A CompletableFuture that completes when the undo operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Redoes the last undone edit action performed on the currently selected recording.
	 * <p>
	 * This method asynchronously performs the following operations:
	 * <ul>
	 *   <li>Suspends playback if it is currently active</li>
	 *   <li>Gets the currently selected recording</li>
	 *   <li>Calls the redo method on the recording's edit manager</li>
	 *   <li>Updates the edit state flags (can undo, can redo, etc.)</li>
	 * </ul>
	 *
	 * @return A CompletableFuture that completes when the redo operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Saves the currently selected recording to the specified file.
	 * <p>
	 * This is a convenience method that delegates to {@link #saveRecording(Recording, File, ProgressCallback)}
	 * using the currently selected recording.
	 *
	 * @param file     The file to which the recording will be saved.
	 * @param callback A progress callback to monitor the save operation.
	 *
	 * @return A CompletableFuture that completes when the save operation is done,
	 * or completes exceptionally if an error occurs.
	 */
	public CompletableFuture<Void> saveRecording(File file, ProgressCallback callback) {
		return saveRecording(getSelectedRecording(), file, callback);
	}

	/**
	 * Saves a recording to the specified file.
	 * <p>
	 * This method performs the following operations:
	 * <ul>
	 *   <li>Clones the recording's audio stream</li>
	 *   <li>Creates a temporary file for the audio data</li>
	 *   <li>Writes the audio data to the temporary file</li>
	 *   <li>Creates a new recording object with the same content as the original</li>
	 *   <li>Writes the recording to the destination file using {@link RecordingFileWriter}</li>
	 *   <li>Updates the original recording's audio stream if overwriting its source file</li>
	 *   <li>Updates the recording's state hash in the state map</li>
	 * </ul>
	 * <p>
	 * The operations are performed asynchronously on a background thread.
	 *
	 * @param recording The recording to save.
	 * @param file      The file to which the recording will be saved.
	 * @param callback  A progress callback to monitor the save operation.
	 *
	 * @return A CompletableFuture that completes when the save operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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
	 * @param file     The file to save the recording in.
	 * @param interval The interval of the recording that should be saved in milliseconds.
	 * @param callback Progress callback for the saving progress.
	 *
	 * @return An async future completing the task.
	 */
	public CompletableFuture<Void> savePartialRecording(File file,
			Interval<Long> interval, ProgressCallback callback)
			throws RecordingEditException {
		return savePartialRecording(file, interval, callback, getSelectedRecording());
	}

	/**
	 * Saves the part of the recording that is specified through the interval.
	 * Removes the saved part from the current recording.
	 *
	 * @param file      The file to save the recording in.
	 * @param interval  The interval of the recording that should be saved in milliseconds.
	 * @param callback  Progress callback for the saving progress.
	 * @param recording The recording that should be saved partially.
	 *
	 * @return An async future completing the task.
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
						// Checks whether the interval to get extracted starts at the beginning of the recording.
						// If it does not, we have to cut out the section before the interval.
						if (start != 0) {
							return cut(0, start, partial);
						}
						return CompletableFuture.completedFuture(null);
					})
					.thenCompose(ignored -> {
						// Checks whether the interval to get extracted ends at the end of the recording.
						// If it does not, we have to cut out the section before the interval.
						if (end != 1) {
							return cut(end, 1, partial);
						}
						return CompletableFuture.completedFuture(null);
					})
					// Then we save the extracted part.
					.thenCompose(ignored -> saveRecording(partial, file, callback))
					// And remove the same interval in the selected recording.
					.thenCompose(ignored -> CompletableFuture.runAsync(() -> {
						try {
							addEditAction(getSelectedRecording(),
									new CutAction(getSelectedRecording(), start, end,
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

	/**
	 * Exports audio from the currently selected recording to a file.
	 * <p>
	 * This is a convenience method that delegates to {@link #exportAudio(Recording, File, ProgressCallback)}
	 * with the currently selected recording.
	 *
	 * @param file     The destination file where audio will be saved.
	 * @param callback A callback to track export progress.
	 *
	 * @return A CompletableFuture that completes when the export operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
	public CompletableFuture<Void> exportAudio(File file,
			ProgressCallback callback) {
		return exportAudio(getSelectedRecording(), file, callback);
	}

	/**
	 * Exports audio from a specified recording to a file.
	 * <p>
	 * This method asynchronously extracts audio from the recording and saves it to the specified file.
	 * The operation is performed on a background thread, and progress can be monitored through the
	 * provided callback.
	 *
	 * @param recording The recording from which to export audio.
	 * @param file      The destination file where audio will be saved.
	 * @param callback  A callback to track export progress.
	 *
	 * @return A CompletableFuture that completes when the export operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Imports audio from a file into the currently selected recording.
	 * <p>
	 * This is a convenience method that delegates to {@link #importAudio(File, Recording)}
	 * with the currently selected recording.
	 *
	 * @param file The source file from which to import audio.
	 *
	 * @return A CompletableFuture that completes when the import operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
	public CompletableFuture<Void> importAudio(File file) {
		return importAudio(file, getSelectedRecording());
	}

	/**
	 * Imports audio from a file into a specified recording.
	 * <p>
	 * This method asynchronously replaces the audio in the specified recording with audio
	 * from the provided file. It creates a {@link ReplaceAudioAction} that can be undone later
	 * and updates the recording with the new audio.
	 * <p>
	 * The operation is performed on a background thread.
	 *
	 * @param file      The source file from which to import audio.
	 * @param recording The recording whose audio will be replaced.
	 *
	 * @return A CompletableFuture that completes when the import operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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

	/**
	 * Retrieves the state hash value for a recording that was saved.
	 * <p>
	 * This method returns the hash value stored when the recording
	 * was last saved. This hash can be used to determine if a recording
	 * has been modified since it was last saved.
	 *
	 * @param recording The recording for which to retrieve the save hash.
	 *
	 * @return The hash value from when the recording was last saved, or null if the recording
	 * has never been saved or is not being tracked.
	 */
	public Integer getRecordingSaveHash(Recording recording) {
		return recordingStateMap.get(recording);
	}

	/**
	 * Handles changes in a recording by updating the UI and related services.
	 * <p>
	 * This method is called when a recording change event occurs and performs the following operations:
	 * <ul>
	 *   <li>Updates document service if content related to the document, header, or audio changed</li>
	 *   <li>Updates playback service for major content changes (except when only events are added)</li>
	 *   <li>Forwards the event to the event bus for other listeners</li>
	 *   <li>Adjusts the selection position to maintain a consistent user experience</li>
	 *   <li>Updates the seeking state to reflect changes correctly in the UI</li>
	 * </ul>
	 * <p>
	 * The method applies the appropriate scaling to selections and positions based on
	 * the duration changes in the recording.
	 *
	 * @param event The recording change event containing information about what changed.
	 */
	private void recordingChanged(RecordingChangeEvent event) {
		Recording.Content content = event.getContentType();
		Recording recording = event.getRecording();
		Document document = recording.getRecordedDocument().getDocument();

		context.setSeeking(true);

		double prevDuration = playbackService.getDuration().getMillis();
		double scale = prevDuration / recording.getRecordedAudio().getAudioStream().getLengthInMillis();

		// Do not refresh the recording if new events got added, because the view got already handled by the UI.
		switch (content) {
			case ALL, DOCUMENT, HEADER, AUDIO, EVENTS_REMOVED, EVENTS_CHANGED -> {
				documentService.replaceDocument(document);
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

	/**
	 * Suspends the ongoing playback if it has been started.
	 * <p>
	 * This method is used before performing edit operations to ensure that
	 * playback doesn't conflict with editing actions.
	 *
	 * @throws ExecutableException If an error occurs during playback suspension.
	 */
	private void suspendPlayback() throws ExecutableException {
		if (playbackService.started()) {
			playbackService.suspend();
		}
	}

	/**
	 * Adds an edit action to the recording's edit manager and updates the edit state.
	 * <p>
	 * This method handles the process of adding a new edit action to the recording's
	 * history and updating the UI state to reflect the new capabilities.
	 *
	 * @param recording The recording to which the edit action will be added.
	 * @param action    The edit action to be performed and added to history.
	 *
	 * @throws RecordingEditException If an error occurs during the edit operation.
	 */
	private void addEditAction(Recording recording, EditAction action)
			throws RecordingEditException {
		recording.getEditManager().addEditAction(action);

		updateEditState(recording);
	}

	/**
	 * Updates the UI context with the current edit capabilities based on the recording state.
	 * <p>
	 * This method examines the recording's document and edit history to determine
	 * which edit operations are currently available and updates the context accordingly.
	 *
	 * @param recording The recording whose edit state should be evaluated.
	 */
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
	 * @param timestamp  The new timestamp.
	 * @param pageNumber The number of the page to move the timestamp.
	 *
	 * @return An async future completing the task.
	 */
	public CompletableFuture<Void> movePage(int timestamp, int pageNumber) {
		return movePage(timestamp, pageNumber, getSelectedRecording());
	}

	/**
	 * Sets the timestamp of the page with the selected number, without
	 * affecting other parts of the recording, like the audio or the total
	 * duration of the recording.
	 *
	 * @param timestamp  The new timestamp.
	 * @param pageNumber The number of the page to move the timestamp.
	 * @param recording  The recording this should happen in.
	 *
	 * @return An async future completing the task.
	 */
	public CompletableFuture<Void> movePage(int timestamp, int pageNumber, Recording recording) {
		return CompletableFuture.runAsync(() -> {
			try {
				suspendPlayback();

				addEditAction(recording, new MovePageAction(recording, pageNumber, timestamp));
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
	 * @param pageNumber The number of the page to remove.
	 *
	 * @return An async future completing the task.
	 */
	public CompletableFuture<Void> hidePage(int pageNumber) {
		return hidePage(pageNumber, getSelectedRecording());
	}

	/**
	 * Removes the page with the selected number, without affecting other parts
	 * of the recording, like the audio or the total duration of the recording.
	 *
	 * @param pageNumber The number of the page to remove.
	 * @param recording  The recording this should happen in.
	 *
	 * @return An async future completing the task.
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

	/**
	 * Hides a page and moves the next page to the specified timestamp in the currently selected recording.
	 * <p>
	 * This is a convenience method that delegates to {@link #hideAndMoveNextPage(int, int, Recording)}
	 * with the currently selected recording.
	 *
	 * @param pageNumber The number of the page to hide.
	 * @param timestamp  The timestamp to which the next page should be moved (in milliseconds).
	 *
	 * @return A CompletableFuture that completes when the operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
	public CompletableFuture<Void> hideAndMoveNextPage(int pageNumber, int timestamp) {
		return hideAndMoveNextPage(pageNumber, timestamp, getSelectedRecording());
	}

	/**
	 * Hides a page and moves the next page to the specified timestamp in a recording.
	 * <p>
	 * This method asynchronously performs the following operations:
	 * <ul>
	 *   <li>Suspends playback if it is currently active</li>
	 *   <li>Creates and executes a HideAndMoveNextPageAction on the recording</li>
	 *   <li>Updates the recording's edit history</li>
	 * </ul>
	 *
	 * @param pageNumber The number of the page to hide.
	 * @param timestamp  The timestamp to which the next page should be moved (in milliseconds).
	 * @param recording  The recording on which to perform this operation.
	 *
	 * @return A CompletableFuture that completes when the operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
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
	 * Inserts new actions into a page and optionally removes unused ones.
	 *
	 * @param addActions    The actions to be added.
	 * @param removeActions The actions to be removed.
	 * @param pageNumber    The page number into which the actions should be added.
	 *
	 * @return An async task performing the task.
	 */
	public CompletableFuture<Void> insertPlaybackActions(
			List<PlaybackAction> addActions,
			List<PlaybackAction> removeActions, int pageNumber) {
		return insertPlaybackActions(addActions, removeActions, pageNumber,
				getSelectedRecording());
	}

	/**
	 * Inserts new actions into a page and optionally removes unused ones in the specified recording.
	 * <p>
	 * This method asynchronously modifies the playback actions in the recording by adding the specified
	 * actions and removing others. The changes are applied to the specified page and recorded as a
	 * single undoable edit action.
	 *
	 * @param addActions    The actions to be added to the recording.
	 * @param removeActions The actions to be removed from the recording.
	 * @param pageNumber    The page number into which the actions should be added.
	 * @param recording     The recording in which the actions should be modified.
	 *
	 * @return A CompletableFuture that completes when the operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
	public CompletableFuture<Void> insertPlaybackActions(
			List<PlaybackAction> addActions,
			List<PlaybackAction> removeActions,
			int pageNumber,
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
	 * Modifies positions of playback actions in the currently selected recording.
	 * <p>
	 * This is a convenience method that delegates to the overloaded version
	 * using the currently selected recording.
	 *
	 * @param handle     The handle identifier of the playback action(s) to modify.
	 * @param pageNumber The page number containing the action(s) to modify.
	 * @param delta      The position delta to apply to the action(s).
	 *
	 * @return A CompletableFuture that completes when the operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
	public CompletableFuture<Void> modifyPlaybackActionPositions(int handle,
			int pageNumber, PenPoint2D delta) {
		return modifyPlaybackActionPositions(handle, pageNumber, delta,
				getSelectedRecording());
	}

	/**
	 * Modifies the positions of playback actions in a recording.
	 * <p>
	 * This method asynchronously updates the positions of playback actions that match
	 * the specified handle in the given page. The action is recorded in the edit history
	 * and can be undone. The primary selection position is preserved during this operation.
	 *
	 * @param handle     The handle identifier of the playback action(s) to modify.
	 * @param pageNumber The page number containing the action(s) to modify.
	 * @param delta      The position delta to apply to the action(s).
	 * @param recording  The recording containing the actions to modify.
	 *
	 * @return A CompletableFuture that completes when the operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs.
	 */
	public CompletableFuture<Void> modifyPlaybackActionPositions(int handle,
			int pageNumber, PenPoint2D delta, Recording recording) {
		double timestampBefore = context.getPrimarySelection();
		return CompletableFuture.runAsync(() -> {
				try {
					addEditAction(recording,
							new ModifyPlaybackActionPositionsAction(recording, handle, pageNumber, delta));
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

	/**
	 * Normalizes the audio loudness of the currently selected recording to a specified LUFS value.
	 * <p>
	 * This is a convenience method that delegates to the overloaded version
	 * using the currently selected recording.
	 *
	 * @param lufsValue The target LUFS (Loudness Units relative to Full Scale) value to normalize to.
	 * @param callback  A progress callback to monitor the normalization progress.
	 *
	 * @return A CompletionStage that completes when the normalization operation is done
	 * or completes exceptionally if an error occurs.
	 */
	public CompletionStage<Void> normalizeAudioLoudness(Double lufsValue,
			ProgressCallback callback) {
		return normalizeAudioLoudness(lufsValue, callback,
				getSelectedRecording());
	}

	/**
	 * Normalizes the audio loudness of a specified recording to a target LUFS value.
	 * <p>
	 * This method asynchronously performs audio loudness normalization on the recording
	 * by creating and executing a {@link NormalizeLoudnessAction}. The operation is
	 * recorded in the edit history and can be undone later. After normalization,
	 * the loudness configuration is recomputed for use in subsequent audio edits.
	 * <p>
	 * The method executes on a background thread, and its progress can be monitored
	 * through the provided callback.
	 *
	 * @param lufsValue The target LUFS (Loudness Units relative to Full Scale) value to normalize to
	 * @param callback  A progress callback to monitor the normalization process
	 * @param recording The recording whose audio will be normalized
	 *
	 * @return A CompletionStage that completes when the normalization operation is done,
	 * or completes exceptionally with a {@link CompletionException} if an error occurs
	 */
	public CompletionStage<Void> normalizeAudioLoudness(Double lufsValue,
			ProgressCallback callback, Recording recording) {
		return CompletableFuture.runAsync(() -> {
				try {
					addEditAction(recording, new NormalizeLoudnessAction(recording, lufsValue, callback));
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			})
			.thenCompose((ignored) -> {
				// Recompute Loudness Configuration, to be used for later audio edits.
				computeLoudnessConfiguration(recording);
				return CompletableFuture.completedFuture(null);
			});
	}
}
