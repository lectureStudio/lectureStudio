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
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.filter.AudioFilter;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.media.playback.RecordingPlayer;
import org.lecturestudio.media.video.VideoRenderSurface;

/**
 * Service responsible for managing the playback of lecture recordings.
 * <p>
 * This service provides functionality for controlling the playback of recordings,
 * including playing, stopping, seeking, and navigating through pages.
 * It also supports audio filtering and video rendering capabilities.
 * <p>
 * The service maintains the playback state and synchronizes with the EditorContext
 * to handle user interactions such as page selection and seeking operations.
 *
 * @author Alex Andres
 */
@Singleton
public class RecordingPlaybackService extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RecordingPlaybackService.class);

	/**  Provider for audio system functionality used for playback operations. */
	private final AudioSystemProvider audioSystemProvider;

	/** The editor context that maintains the application state and configuration. */
	private final EditorContext context;

	/** Listener that monitors the recording player's state changes. */
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

	/** The player responsible for playing back recordings. */
	private RecordingPlayer recordingPlayer;

	/** Surface where video frames from the recording are rendered. */
	private VideoRenderSurface videoRenderSurface;


	/**
	 * Constructs a new RecordingPlaybackService.
	 * <p>
	 * Initializes the service with the provided application context and audio system provider.
	 * Sets up a listener for the primary selection property to handle seeking operations
	 * when the selection changes.
	 *
	 * @param context             The application context, which must be an instance of EditorContext.
	 * @param audioSystemProvider The provider for audio system functionality.
	 */
	@Inject
	RecordingPlaybackService(ApplicationContext context, AudioSystemProvider audioSystemProvider) {
		this.audioSystemProvider = audioSystemProvider;
		this.context = (EditorContext) context;
		this.context.primarySelectionProperty().addListener((o, oldValue, newValue) -> {
			if (initialized() || suspended() || stopped()) {
				try {
					seek(newValue);
				}
				catch (ExecutableException e) {
					LOG.error("Seek recording failed", e);
				}
			}
		});
	}

	/**
	 * Gets the total duration of the current recording.
	 *
	 * @return The total duration as a Time object, or null if no recording player is available.
	 */
	public Time getDuration() {
		return nonNull(recordingPlayer) ? recordingPlayer.getDuration() : null;
	}

	/**
	 * Gets the current playback position within the recording.
	 *
	 * @return The elapsed time in milliseconds as a Long, or null if no recording player is available.
	 */
	public Long getElapsedTime() {
		return nonNull(recordingPlayer) ? recordingPlayer.getElapsedTime() : null;
	}

	/**
	 * Applies an audio filter to a specific interval of the recording.
	 *
	 * @param filter   The audio filter to apply to the audio stream.
	 * @param interval The time interval within which the filter should be applied.
	 *
	 * @throws NullPointerException If recordingPlayer is null.
	 */
	public void setAudioFilter(AudioFilter filter, Interval<Long> interval) {
		recordingPlayer.getAudioStream().setAudioFilter(filter, interval);
	}

	/**
	 * Removes a previously applied audio filter from the recording.
	 *
	 * @param filter The audio filter to remove from the audio stream.
	 *
	 * @throws NullPointerException If recordingPlayer is null.
	 */
	public void removeAudioFilter(AudioFilter filter) {
		recordingPlayer.getAudioStream().removeAudioFilter(filter);
	}

	/**
	 * Sets the surface where to render the video frames.
	 *
	 * @param renderSurface The surface where to render the video frames.
	 */
	public void setVideoRenderSurface(VideoRenderSurface renderSurface) {
		videoRenderSurface = renderSurface;
	}

	/**
	 * Sets a new recording for playback.
	 * <p>
	 * If there's already a recording player, it closes the current recording first.
	 * Then it creates a new recording player with the provided recording and initializes it.
	 *
	 * @param recording The recording to be played back.
	 */
	public synchronized void setRecording(Recording recording) {
		if (nonNull(recordingPlayer)) {
			closeRecording();
		}

		recordingPlayer = new RecordingPlayer(context, context.getConfiguration().getAudioConfig(),
				audioSystemProvider);
		recordingPlayer.setRecording(recording);
		recordingPlayer.setVideoRenderSurface(videoRenderSurface);

		try {
			init();
		}
		catch (ExecutableException e) {
			LOG.error("Initialize recording failed", e);
		}
	}

	/**
	 * Closes the current recording.
	 * <p>
	 * If there is an active recording player that hasn't been destroyed,
	 * this method will attempt to destroy it.
	 */
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

	/**
	 * Navigates to the previous page in the recording.
	 * <p>
	 * Sets the seeking state in the context before and after the operation.
	 *
	 * @throws Exception If navigation to the previous page fails.
	 */
	public synchronized void selectPreviousPage() throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPreviousPage();
		context.setSeeking(false);
	}

	/**
	 * Navigates to the next page in the recording.
	 * <p>
	 * Sets the seeking state in the context before and after the operation.
	 *
	 * @throws Exception If navigation to the next page fails.
	 */
	public synchronized void selectNextPage() throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectNextPage();
		context.setSeeking(false);
	}

	/**
	 * Selects a page in the recording by its page number.
	 * <p>
	 * Sets the seeking state in the context before and after the operation.
	 *
	 * @param pageNumber The page number to select.
	 *
	 * @throws Exception If page selection fails.
	 */
	public synchronized void selectPage(int pageNumber) throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPage(pageNumber);
		context.setSeeking(false);
	}

	/**
	 * Selects a specific page object in the recording.
	 * <p>
	 * Sets the seeking state in the context before and after the operation.
	 *
	 * @param page The page object to select.
	 *
	 * @throws Exception If page selection fails.
	 */
	public synchronized void selectPage(Page page) throws Exception {
		context.setSeeking(true);
		recordingPlayer.selectPage(page);
		context.setSeeking(false);
	}

	/**
	 * Sets the playback volume.
	 * <p>
	 * Changes the volume of the recording player if it exists.
	 *
	 * @param volume The volume level to set, typically between 0.0 and 1.0.
	 */
	public synchronized void setVolume(float volume) {
		if (nonNull(recordingPlayer)) {
			recordingPlayer.setVolume(volume);
		}
	}

	/**
	 * Stops the video playback of the current recording.
	 * <p>
	 * If there is an active recording player, this method will instruct it to stop
	 * video playback while potentially leaving other aspects of playback unaffected.
	 * Does nothing if no recording player is available.
	 */
	public void stopVideo() {
		if (nonNull(recordingPlayer)) {
			recordingPlayer.stopVideo();
		}
	}

	/**
	 * Seeks to a specific time position in the recording.
	 * <p>
	 * Does nothing if the player is already started or destroyed, or if seeking
	 * is already in progress.
	 *
	 * @param time The time position to seek to, expressed as a ratio (0.0 to 1.0)
	 *             of the total recording duration.
	 *
	 * @throws ExecutableException If seeking fails.
	 */
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
	 * Sets the playback to the selected time in millisecond precision.
	 *
	 * @param timeMs The time in milliseconds.
	 *
	 * @throws ExecutableException If the playback could not be set to the
	 *                             provided timestamp.
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
