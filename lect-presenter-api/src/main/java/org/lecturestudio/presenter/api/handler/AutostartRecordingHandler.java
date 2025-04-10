/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.listener.DocumentListChangeListener;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.SaveRecordingPresenter;
import org.lecturestudio.presenter.api.service.RecordingService;

/**
 * Handles automatic recording operations based on document events and the application configuration.
 * <p>
 * This handler monitors document events such as insertion and removal to automatically
 * start or stop recording sessions according to the configured the autostart preference.
 * <p>
 * When enabled in the configuration and a document is available, recording will start
 * automatically during initialization or when a new document is inserted. If all documents
 * are removed, the recording will be automatically stopped.
 *
 * @author Alex Andres
 */
public class AutostartRecordingHandler extends PresenterHandler implements DocumentListChangeListener {

	/** Provides access to notification, stream, and audio settings. */
	private final PresenterConfiguration config;

	/** Service that manages documents and provides access to the document collection. */
	private final DocumentService documentService;

	/** Service that manages recording operations such as start, stop, and suspend. */
	private final RecordingService recordingService;


	/**
	 * Initializes a new instance of AutostartRecordingHandler with required services and context.
	 *
	 * @param context    The presenter context containing application configuration.
	 * @param docService Service that manages documents and provides access to the document collection.
	 * @param recService Service that manages recording operations such as start, stop, and suspend.
	 */
	public AutostartRecordingHandler(PresenterContext context, DocumentService docService,
									 RecordingService recService) {
		super(context);

		config = context.getConfiguration();
		documentService = docService;
		recordingService = recService;
	}

	@Override
	public void initialize() {
		// Respond to document events.
		documentService.getDocuments().addListener(this);

		if (isAutostartEnabled()) {
			startRecording();
		}
	}

	@Override
	public void documentInserted(Document doc) {
		// Check if autostart recording is enabled and a document is selected.
		if (isAutostartEnabled()) {
			startRecording();
		}
	}

	@Override
	public void documentRemoved(Document doc) {
		// Check if any document remains selected after removal.
		boolean hasDocument = nonNull(documentService.getDocuments().getSelectedDocument());

		if (!hasDocument) {
			stopRecording();
		}
	}

	@Override
	public void documentSelected(Document prevDoc, Document newDoc) {
		// Ignore document selection events.
	}

	@Override
	public void documentReplaced(Document prevDoc, Document newDoc) {
		// Ignore document replacement events.
	}

	/**
	 * Determines whether automatic recording should start based on the application configuration
	 * and document availability.
	 *
	 * @return {@code true} if autostart recording is enabled in configuration and a document
	 *         is currently selected, {@code false} otherwise.
	 */
	private boolean isAutostartEnabled() {
		boolean autostartSettingEnabled = requireNonNullElse(config.getAutostartRecording(), false);
		boolean hasDocument = nonNull(documentService.getDocuments().getSelectedDocument());
		boolean isRecording = recordingService.started() || recordingService.suspended();

		return autostartSettingEnabled && hasDocument && !isRecording;
	}

	private void startRecording() {
		if (!recordingService.started()) {
			// Enable microphone input before starting the recording to ensure audio is captured.
			config.getStreamConfig().setMicrophoneEnabled(true);

			try {
				recordingService.start();
			}
			catch (ExecutableException e) {
				handleException(e, "Start recording failed", "recording.start.error");
			}
		}
	}

	private void stopRecording() {
		if (recordingService.started() || recordingService.suspended()) {
			try {
				recordingService.stop();

				// Post a command to show the save recording dialog.
				context.getEventBus().post(new ShowPresenterCommand<>(SaveRecordingPresenter.class));
			}
			catch (ExecutableException e) {
				handleException(e, "Stop recording failed", "recording.stop.error");
			}
		}
	}
}
