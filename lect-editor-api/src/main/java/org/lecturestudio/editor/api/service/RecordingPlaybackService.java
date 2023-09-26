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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.filter.AudioFilter;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.media.playback.RecordingPlayer;

@Singleton
public class RecordingPlaybackService extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RecordingPlaybackService.class);

	private final AudioSystemProvider audioSystemProvider;

	private final EditorContext context;

	private final ExecutableStateListener playbackStateListener = (oldState, newState) -> {
		if (newState == ExecutableState.Stopped && getState() != ExecutableState.Stopping) {
			try {
				stop();
			}
			catch (ExecutableException e) {
				LOG.error("Stop playback failed", e);
			}
		}
	};

	private RecordingPlayer recordingPlayer;


	@Inject
	RecordingPlaybackService(ApplicationContext context, AudioSystemProvider audioSystemProvider) {
		this.audioSystemProvider = audioSystemProvider;
		this.context = (EditorContext) context;
		this.context.primarySelectionProperty().addListener((o, oldValue, newValue) -> {
			if (initialized() || suspended()) {
				try {
					seek(newValue);
				}
				catch (ExecutableException e) {
					LOG.error("Seek recording failed", e);
				}
			}
		});
	}

	public Time getDuration() {
		return nonNull(recordingPlayer) ? recordingPlayer.getDuration() : null;
	}

	public Long getElapsedTime() {
		return nonNull(recordingPlayer) ? recordingPlayer.getElapsedTime() : null;
	}

	public void setAudioFilter(AudioFilter filter, Interval<Long> interval) {
		recordingPlayer.getAudioStream().setAudioFilter(filter, interval);
	}

	public void removeAudioFilter(AudioFilter filter) {
		recordingPlayer.getAudioStream().removeAudioFilter(filter);
	}

	public synchronized void setRecording(Recording recording) {
		if (nonNull(recordingPlayer)) {
			closeRecording();
		}

		recordingPlayer = new RecordingPlayer(context,
				context.getConfiguration().getAudioConfig(),
				audioSystemProvider);
		recordingPlayer.setRecording(recording);

		try {
			init();
		}
		catch (ExecutableException e) {
			LOG.error("Initialize recording failed", e);
		}
	}

	public synchronized void closeRecording() {
		if (nonNull(recordingPlayer) && !recordingPlayer.destroyed()) {
			try {
				destroy();
			}
			catch (ExecutableException e) {
				LOG.error("Close recording failed", e);
			}
		}
	}

	public synchronized void selectPreviousPage() throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPreviousPage();
		context.setSeeking(false);
	}

	public synchronized void selectNextPage() throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectNextPage();
		context.setSeeking(false);
	}

	public synchronized void selectPage(int pageNumber) throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPage(pageNumber);
		context.setSeeking(false);
	}

	public synchronized void selectPage(Page page) throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPage(page);
		context.setSeeking(false);
	}

	public synchronized void setVolume(float volume) {
		if (nonNull(recordingPlayer)) {
			recordingPlayer.setVolume(volume);
		}
	}

	public synchronized void seek(double time) throws ExecutableException {
		if (started() || destroyed()) {
			return;
		}
		if (context.isSeeking()) {
			return;
		}
		if (nonNull(recordingPlayer)) {
			context.setSeeking(true);
			recordingPlayer.seek(time);
			context.setSeeking(false);
		}
	}

	/**
	 * Sets the playback to the selected time
	 *
	 * @param timeMs the time in milliseconds
	 * @throws ExecutableException
	 */
	public synchronized void seek(int timeMs) throws ExecutableException {
		if (started() || destroyed()) {
			return;
		}
		if (context.isSeeking()) {
			return;
		}
		if (nonNull(recordingPlayer)) {
			context.setSeeking(true);
			recordingPlayer.seek(timeMs);
			context.setSeeking(false);
		}
	}

	@Override
	protected synchronized void initInternal() throws ExecutableException {
		if (!recordingPlayer.initialized()) {
			AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();

			recordingPlayer.addStateListener(playbackStateListener);
			recordingPlayer.init();
			recordingPlayer.setVolume((float) audioConfig.getPlaybackVolume());
		}
	}

	@Override
	protected synchronized void startInternal() throws ExecutableException {
		if (!recordingPlayer.started()) {
			recordingPlayer.start();
		}
	}

	@Override
	protected synchronized void stopInternal() throws ExecutableException {
		if (recordingPlayer.started() || recordingPlayer.suspended()) {
			recordingPlayer.stop();
		}
	}

	@Override
	protected synchronized void suspendInternal() throws ExecutableException {
		if (recordingPlayer.started()) {
			recordingPlayer.suspend();
		}
	}

	@Override
	protected synchronized void destroyInternal() throws ExecutableException {
		if (!recordingPlayer.created() && !recordingPlayer.destroyed()) {
			recordingPlayer.removeStateListener(playbackStateListener);
			recordingPlayer.destroy();
		}
	}
}
