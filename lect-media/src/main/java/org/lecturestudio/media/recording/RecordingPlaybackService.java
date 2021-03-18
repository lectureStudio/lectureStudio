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

import static java.util.Objects.nonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.media.playback.PlaybackContext;
import org.lecturestudio.media.playback.RecordingPlayer;

@Singleton
public class RecordingPlaybackService extends ExecutableBase {

	private final static Logger LOG = LogManager.getLogger(RecordingPlaybackService.class);

	private final PlaybackContext context;

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
	RecordingPlaybackService(ApplicationContext context) {
		this.context = (PlaybackContext) context;
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

	public void setRecording(Recording recording) {
		if (nonNull(recordingPlayer)) {
			closeRecording();
		}

		recordingPlayer = new RecordingPlayer(context, context.getConfiguration().getAudioConfig());
		recordingPlayer.setRecording(recording);

		try {
			init();
		}
		catch (ExecutableException e) {
			LOG.error("Initialize recording failed", e);
		}
	}

	public void closeRecording() {
		if (nonNull(recordingPlayer) && !recordingPlayer.destroyed()) {
			try {
				destroy();
			}
			catch (ExecutableException e) {
				LOG.error("Close recording failed", e);
			}
		}
	}

	public void selectPreviousPage() throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPreviousPage();
		context.setSeeking(false);
	}

	public void selectNextPage() throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectNextPage();
		context.setSeeking(false);
	}

	public void selectPage(int pageNumber) throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPage(pageNumber);
		context.setSeeking(false);
	}

	public void selectPage(Page page) throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPage(page);
		context.setSeeking(false);
	}

	public void setVolume(float volume) {
		if (nonNull(recordingPlayer)) {
			recordingPlayer.setVolume(volume);
		}
	}

	public void seek(double time) throws ExecutableException {
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

	@Override
	protected void initInternal() throws ExecutableException {
		if (!recordingPlayer.initialized()) {
			AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();

			recordingPlayer.addStateListener(playbackStateListener);
			recordingPlayer.init();
			recordingPlayer.setVolume((float) audioConfig.getPlaybackVolume());
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (!recordingPlayer.started()) {
			recordingPlayer.start();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (recordingPlayer.started() || recordingPlayer.suspended()) {
			recordingPlayer.stop();
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (recordingPlayer.started()) {
			recordingPlayer.suspend();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		if (!recordingPlayer.created() && !recordingPlayer.destroyed()) {
			recordingPlayer.removeStateListener(playbackStateListener);
			recordingPlayer.destroy();
		}
	}
}
