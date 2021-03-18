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

package org.lecturestudio.core.audio;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.source.AudioSource;
import org.lecturestudio.core.model.Time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default audio player implementation.
 *
 * @author Alex Andres
 */
public class AudioPlayer extends ExecutableBase implements Player {

	private static final Logger LOG = LogManager.getLogger(AudioPlayer.class);

	/** The sync state that is shared with other media players. */
	private final SyncState syncState;

	/** The audio playback device. */
	private final AudioOutputDevice playbackDevice;

	/** The audio source. */
	private final AudioSource audioSource;

	/** The audio source size. */
	private final long inputSize;

	/** The playback progress listener. */
	private AudioPlaybackProgressListener progressListener;

	/** The player state listener. */
	private ExecutableStateListener stateListener;

	/** The playback thread. */
	private Thread thread;

	/** The current audio source reading position. */
	private long inputPos;


	/**
	 * Create an AudioPlayer with the specified playback device and source. The
	 * sync state is shared with other media players to keep different media
	 * sources in sync while playing.
	 *
	 * @param device    The audio playback device.
	 * @param source    The audio source.
	 * @param syncState The shared sync state.
	 *
	 * @throws Exception If the audio player failed to initialize.
	 */
	public AudioPlayer(AudioOutputDevice device, AudioSource source, SyncState syncState) throws Exception {
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
	public void seek(int timeMs) throws Exception {
		AudioFormat format = audioSource.getAudioFormat();

		float bytesPerSecond = AudioUtils.getBytesPerSecond(format);
		int skipBytes = Math.round(bytesPerSecond * timeMs / 1000F);

		audioSource.reset();
		audioSource.skip(skipBytes);

		inputPos = skipBytes;

		syncState.setAudioTime((long) (inputPos / (bytesPerSecond / 1000f)));
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
	protected void initInternal() throws ExecutableException {
    	try {
			audioSource.reset();
		}
		catch (Exception e) {
			throw new ExecutableException("Audio device could not be initialized.", e);
		}

		if (!playbackDevice.supportsAudioFormat(audioSource.getAudioFormat())) {
			throw new ExecutableException("Audio device does not support the needed audio format.");
		}

		try {
			playbackDevice.setAudioFormat(audioSource.getAudioFormat());
			playbackDevice.open();
			playbackDevice.start();
		}
		catch (Exception e) {
			throw new ExecutableException("Audio device could not be initialized.", e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (getPreviousState() == ExecutableState.Suspended) {
			synchronized (thread) {
				thread.notify();
			}
		}
		else {
			thread = new Thread(new AudioReaderTask(), getClass().getSimpleName());
			thread.start();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			audioSource.reset();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		inputPos = 0;
		syncState.reset();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			playbackDevice.close();
			audioSource.close();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void fireStateChanged() {
		if (nonNull(stateListener)) {
			stateListener.onExecutableStateChange(getPreviousState(), getState());
		}
	}

	private void onProgress(Time progress, Time duration, long progressMs) {
		if (nonNull(syncState)) {
			syncState.setAudioTime(progressMs);
		}
		if (nonNull(progressListener) && started()) {
			progress.setMillis(progressMs);

			progressListener.onAudioProgress(progress, duration);
		}
	}



	private class AudioReaderTask implements Runnable {

		@Override
		public void run() {
			byte[] buffer = new byte[playbackDevice.getBufferSize()];
			int bytesRead;

			// Calculate bytes per millisecond.
			float bpms = AudioUtils.getBytesPerSecond(audioSource.getAudioFormat()) / 1000f;

			Time progress = new Time(0);
			Time duration = new Time((long) (inputSize / bpms));

			ExecutableState state;

			while (true) {
				state = getState();

				if (state == ExecutableState.Started) {
					try {
						bytesRead = audioSource.read(buffer, 0, buffer.length);

						if (bytesRead > 0) {
							playbackDevice.write(buffer, 0, bytesRead);

							inputPos += bytesRead;

							onProgress(progress, duration, (long) (inputPos / bpms));
						}
						else if (bytesRead == -1) {
							// EOM
							break;
						}
					}
					catch (Exception e) {
						LOG.error("Play audio failed.", e);
						break;
					}
				}
				else if (state == ExecutableState.Suspended) {
					synchronized (thread) {
						try {
							thread.wait();
						}
						catch (Exception e) {
							// Ignore
						}
					}
				}
				else if (state == ExecutableState.Stopped) {
					return;
				}
			}

			// EOM
			try {
				stop();
			}
			catch (ExecutableException e) {
				LOG.error("Stop " + getClass().getName() + " failed.", e);
			}
		}

	}

}
