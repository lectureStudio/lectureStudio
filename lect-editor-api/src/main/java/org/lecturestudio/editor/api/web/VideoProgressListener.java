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

package org.lecturestudio.editor.api.web;

import java.util.function.Consumer;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;

/**
 * Listener that consumes video frame times (in milliseconds) and forwards
 * rendering progress updates to a {@link RecordingExport} sink.
 *
 * <p>Each accepted frame time updates a shared mutable {@link Time} object
 * representing the current progress and reuses a {@link RecordingRenderProgressEvent}
 * to report progress to the configured export.</p>
 *
 * @author Alex Andres
 */
public class VideoProgressListener implements Consumer<Long> {

	/** Export sink that receives render progress events. */
	private final RecordingExport export;

	/** Mutable time object representing the current render progress time. */
	private final Time progressTime;

	/** Event object re-used to report progress updates to the export. */
	private final RecordingRenderProgressEvent event;


	/**
	 * Initializes the shared mutable time object used to track current render
	 * progress and prepares a reusable {@link RecordingRenderProgressEvent}
	 * that is forwarded to the given {@code export}.
	 *
	 * @param export   sink that receives render progress events.
	 * @param duration total recording duration in milliseconds.
	 */
	VideoProgressListener(RecordingExport export, long duration) {
		this.export = export;

		progressTime = new Time(0);

		event = new RecordingRenderProgressEvent();
		event.setTotalTime(new Time(duration));
		event.setCurrentTime(progressTime);
	}

	@Override
	public void accept(Long frameTimeMs) {
		progressTime.setMillis(frameTimeMs);

		export.onRenderProgress(event);
	}
}
