/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.service.ScreenShareService;
import org.lecturestudio.presenter.api.service.ScreenSourceService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.util.ScreenDocumentCreator;

public class ScreenShareHandler extends PresenterHandler {

	private final StreamService streamService;

	private final ScreenShareService screenShareService;

	private final ScreenSourceService screenSourceService;

	private final DocumentService documentService;

	private final RecordingService recordingService;


	/**
	 * Create a new {@code StreamHandler} with the given context.
	 *
	 * @param context The presenter application context.
	 */
	public ScreenShareHandler(PresenterContext context,
			StreamService streamService, ScreenShareService screenShareService,
			ScreenSourceService screenSourceService,
			DocumentService documentService, RecordingService recordingService) {
		super(context);

		this.streamService = streamService;
		this.screenShareService = screenShareService;
		this.screenSourceService = screenSourceService;
		this.documentService = documentService;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		context.screenSharingStartedProperty().addListener((o, oldValue, newValue) -> {
			Document selectedDoc = documentService.getDocuments().getSelectedDocument();
			ScreenShareContext shareContext = screenSourceService.getScreenShareContext(selectedDoc);

			if (!newValue) {
				// Only update documents with screen dumps if the service is active.
				if (streamService.getScreenShareState() == ExecutableState.Started
						|| screenShareService.isScreenCaptureActive()) {
					try {
						ScreenDocumentCreator.create(documentService, shareContext.getSource());

						selectedDoc = documentService.getDocuments().getSelectedDocument();

						// Register the newly created document with the source.
						screenSourceService.addScreenShareContext(selectedDoc, shareContext);
					}
					catch (Error e) {
						// Select a screen source failed.
						// Which in this case is not too critical, since the source may be minimized.
					}
					catch (Exception e) {
						logException(e, "Create screen-document failed");
					}
				}

				// Pause screen recording.
				suspendScreenRecording();
				// Stop screen capturing.
				stopLocalScreenCapture();
			}
			else {
				startScreenRecording(shareContext);

				if (!context.getStreamStarted()) {
					// Start local screen capture only if we are not streaming.
					startLocalScreenCapture(shareContext);
				}
				else {
					// Set the screen source related to the selected screen document.
					streamService.setScreenShareContext(shareContext);
				}
			}

			streamService.enableScreenSharing(newValue);
		});
	}

	private void startScreenRecording(ScreenShareContext shareContext) {
		// Record screen only if we are recording a lecture.
		if (!recordingService.started()) {
			return;
		}

		screenShareService.startScreenRecording(shareContext);
	}

	private void suspendScreenRecording() {
		screenShareService.suspendScreenRecording();
	}

	private void startLocalScreenCapture(ScreenShareContext shareContext) {
		try {
			screenShareService.startScreenCapture(shareContext);
		}
		catch (ExecutableException e) {
			handleException(e, "Set screen-source failed",
					"stream.screen.share.error");
		}
	}

	private void stopLocalScreenCapture() {
		screenShareService.stopScreenCapture();
	}
}
