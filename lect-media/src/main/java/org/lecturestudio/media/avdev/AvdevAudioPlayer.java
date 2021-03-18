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

package org.lecturestudio.media.avdev;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;

import org.lecturestudio.avdev.StreamListener;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioPlaybackProgressListener;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.Player;
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.audio.source.AudioSource;
import org.lecturestudio.core.model.Time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * AVdev audio player implementation.
 *
 * @author Alex Andres
 */
public class AvdevAudioPlayer extends ExecutableBase implements Player {

	private static final Logger LOG = LogManager.getLogger(AvdevAudioPlayer.class);

	/** The audio playback device. */
	private final AVdevAudioOutputDevice playbackDevice;

	/** The audio source. */
	private final AudioSource audioSource;

	/** The playback progress listener. */
	private AudioPlaybackProgressListener progressListener;

	/** The player state listener. */
	private ExecutableStateListener stateListener;

	/** The sync state that is shared with other media players. */
	private final SyncState syncState;

	/** The audio source size. */
	private final long inputSize;

	/** The current audio source reading position. */
	private long inputPos;


	/**
	 * Create an AvdevAudioPlayer with the specified playback device and source.
	 * The sync state is shared with other media players to keep different media
	 * sources in sync while playing.
	 *
	 * @param device    The audio playback device.
	 * @param source    The audio source.
	 * @param syncState The shared sync state.
	 *
	 * @throws Exception If the audio player failed to initialize.
	 */
	public AvdevAudioPlayer(AVdevAudioOutputDevice device, AudioSource source, SyncState syncState) throws Exception {
		if (isNull(device)) {
			throw new NullPointerException("Missing audio playback device.");
		}
		if (isNull(source)) {
			throw new NullPointerException("Missing audio source.");
		}

		this.playbackDevice = device;
		this.audioSource = source;
		this.syncState = syncState;
		this.inputSize = source.getInputSize();
	}

	@Override
	public void setVolume(float volume) {
		if (volume < 0 || volume > 1) {
			return;
		}

		playbackDevice.setVolume(volume);
	}

	@Override
	public void setProgressListener(AudioPlaybackProgressListener listener) {
		this.progressListener = listener;
	}

	@Override
	public void setStateListener(ExecutableStateListener listener) {
		this.stateListener = listener;
	}

	@Override
	public void seek(int time) throws Exception {
		AudioFormat format = audioSource.getAudioFormat();

		float bytesPerSecond = AudioUtils.getBytesPerSecond(format);
		int skipBytes = Math.round(bytesPerSecond * time / 1000F);

		audioSource.reset();
		audioSource.skip(skipBytes);

		inputPos = skipBytes;

		syncState.setAudioTime((long) (inputPos / (bytesPerSecond / 1000f)));
	}

	@Override
	protected void initInternal() throws ExecutableException {
		try {
			audioSource.reset();

			// Calculate bytes per millisecond.
			float bpms = AudioUtils.getBytesPerSecond(audioSource.getAudioFormat()) / 1000f;

			playbackDevice.setStreamListener(new StreamStateListener());
			playbackDevice.setSource(new org.lecturestudio.avdev.AudioSource() {
				int bytesRead = 0;
				final Time progress = new Time(0);
				final Time duration = new Time((long) (inputSize / bpms));

				@Override
				public int read(byte[] data, int offset, int length) throws IOException {
					bytesRead = audioSource.read(data, 0, length);
					inputPos += bytesRead;

					if (nonNull(syncState)) {
						syncState.setAudioTime((long) (inputPos / bpms));
					}

					if (bytesRead > 0 && inputSize > 0) {
						if (nonNull(progressListener) && started()) {
							progress.setMillis((long) (inputPos / bpms));

							progressListener.onAudioProgress(progress, duration);
						}
					}

					return bytesRead;
				}
			});
			playbackDevice.setAudioFormat(audioSource.getAudioFormat());
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}
	
	@Override
	protected void startInternal() throws ExecutableException {
		try {
			if (!playbackDevice.isOpen()) {
				playbackDevice.open();
			}

			playbackDevice.start();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			playbackDevice.stop();

			audioSource.reset();

			syncState.reset();
			inputPos = 0;
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		try {
			playbackDevice.stop();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			audioSource.close();
		}
		catch (IOException e) {
			throw new ExecutableException(e);
		}

		if (playbackDevice.isOpen()) {
			try {
				playbackDevice.close();
			}
			catch (Exception e) {
				throw new ExecutableException(e);
			}
		}
	}

	@Override
	protected void fireStateChanged() {
		if (nonNull(stateListener)) {
			stateListener.onExecutableStateChange(getPreviousState(), getState());
		}
	}



	private class StreamStateListener implements StreamListener {

		@Override
		public void streamOpened() {

		}

		@Override
		public void streamClosed() {

		}

		@Override
		public void streamStarted() {

		}

		@Override
		public void streamStopped() {

		}

		@Override
		public void streamEnded() {
			try {
				stop();
			}
			catch (ExecutableException e) {
				LOG.error("Stop audio player failed.", e);
			}
		}
	}

}
