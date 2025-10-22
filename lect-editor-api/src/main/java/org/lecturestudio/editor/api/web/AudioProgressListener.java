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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;

/**
 * Listener that receives incremental audio render progress (in bytes) and converts it
 * to time-based progress updates which are forwarded to the {@link RecordingExport}.
 *
 * <p>It maps the byte count to milliseconds using the target audio format and keeps track
 * of the current recording page to report page-based progress as well.</p>
 *
 * @author Alex Andres
 */
public class AudioProgressListener implements Consumer<Integer> {

	/** Export sink that receives render progress events. */
	private final RecordingExport export;

	/** Mutable time object representing the current render progress time. */
	private final Time progressTime;

	/** Event object re-used to report progress updates to the export. */
	private final RecordingRenderProgressEvent event;

	/** Iterator over the recorded pages to determine the current page for progress updates. */
	private final Iterator<RecordedPage> pageIter;

	/** Currently considered recorded page. */
	private RecordedPage recPage;

	/** Number of audio bytes corresponding to one millisecond of audio in the target format. */
	double bytesPerMs;


	/**
	 * Create a new listener for a render of the given {@code recording} into the provided
	 * {@code targetFormat}. Progress events will be reported to the supplied {@code export}.
	 *
	 * @param recording    the source recording to determine audio length and page timestamps.
	 * @param targetFormat the audio format used for rendering (used to compute bytes-to-ms).
	 * @param export       the export implementation that will receive {@link RecordingRenderProgressEvent}s.
	 */
	AudioProgressListener(Recording recording, AudioFormat targetFormat, RecordingExport export) {
		double bytesPerSecond = Math.round(targetFormat.getSampleRate() *
				targetFormat.getFrameSize() * targetFormat.getChannels());
		this.export = export;
		this.bytesPerMs = bytesPerSecond / 1000;

		RecordedAudio recAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recAudio.getAudioStream();
		RecordedEvents recEvents = recording.getRecordedEvents();
		List<RecordedPage> pageList = recEvents.getRecordedPages();

		pageIter = pageList.iterator();
		recPage = pageIter.next();

		progressTime = new Time(0);

		event = new RecordingRenderProgressEvent();
		event.setTotalTime(new Time(stream.getLengthInMillis()));
		event.setCurrentTime(progressTime);
		event.setPageCount(pageList.size());
		event.setPageNumber(recPage.getNumber() + 1);

		if (pageIter.hasNext()) {
			recPage = pageIter.next();
		}
	}

	@Override
	public void accept(Integer readTotal) {
		long currentMs = (long) (readTotal / bytesPerMs);

		progressTime.setMillis(currentMs);

		if (recPage.getTimestamp() < currentMs) {
			event.setPageNumber(recPage.getNumber() + 1);

			if (pageIter.hasNext()) {
				recPage = pageIter.next();
			}
		}

		export.onRenderProgress(event);
	}
}
