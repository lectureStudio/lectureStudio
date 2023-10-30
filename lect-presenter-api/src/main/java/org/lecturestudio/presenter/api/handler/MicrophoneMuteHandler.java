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
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.service.RecordingService;

public class MicrophoneMuteHandler extends PresenterHandler {

	private final RecordingService recordingService;


	/**
	 * Create a new {@code MicrophoneMuteHandler} with the given context.
	 *
	 * @param context          The presenter application context.
	 * @param recordingService The application-wide recording service.
	 */
	public MicrophoneMuteHandler(PresenterContext context,
			RecordingService recordingService) {
		super(context);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		context.getConfiguration().getStreamConfig().enableMicrophoneProperty()
				.addListener((o, oldValue, newValue) -> {
					if (!newValue) {
						// Microphone muted.
						suspendRecording();
					}
					else {
						// Microphone un-muted.
						resumeRecording();
					}
				});
	}

	private void suspendRecording() {
		if (recordingService.started()) {
			try {
				recordingService.suspend();
			}
			catch (ExecutableException e) {
				handleException(e, "Pause recording failed",
						"recording.pause.error");
			}
		}
	}

	private void resumeRecording() {
		if (recordingService.suspended()) {
			try {
				recordingService.start();
			}
			catch (ExecutableException e) {
				handleException(e, "Start recording failed",
						"recording.start.error");
			}
		}
	}
}
