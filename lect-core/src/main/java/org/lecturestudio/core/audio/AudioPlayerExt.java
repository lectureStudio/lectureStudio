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

import java.security.InvalidParameterException;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.io.AudioPlaybackBuffer;
import org.lecturestudio.core.io.PlaybackData;
import org.lecturestudio.core.net.Synchronizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extended audio player implementation.
 *
 * @author Alex Andres
 */
public class AudioPlayerExt extends ExecutableBase implements Player {

	private static final Logger LOG = LogManager.getLogger(AudioPlayerExt.class);

	/** The audio playback device. */
	private AudioOutputDevice playbackDevice;

	/** The audio source. */
	private AudioPlaybackBuffer audioSource;

	/** The player state listener. */
	private ExecutableStateListener stateListener;

	/** The playback thread. */
	private Thread thread;


	/**
	 * Create an AudioPlayerExt with the specified playback device and audio
	 * buffer.
	 *
	 * @param device The audio playback device.
	 * @param buffer The audio source.
	 */
	public AudioPlayerExt(AudioOutputDevice device, AudioPlaybackBuffer buffer) {
		if (isNull(device)) {
			throw new NullPointerException("Missing audio playback device.");
		}
		if (isNull(buffer)) {
			throw new NullPointerException("Missing audio buffer source.");
		}

		this.playbackDevice = device;
		this.audioSource = buffer;
	}

	@Override
	public void setVolume(float volume) {
		if (volume < 0 || volume > 1) {
			throw new InvalidParameterException("Volume value should be within 0 and 1.");
		}

		playbackDevice.setVolume(volume);
	}

	@Override
	public void seek(int time) {
		audioSource.skip(time);
	}

	@Override
	public void setProgressListener(AudioPlaybackProgressListener listener) {

	}

	@Override
	public void setStateListener(ExecutableStateListener listener) {
		this.stateListener = listener;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		audioSource.reset();

		AudioFormat format = audioSource.getAudioFormat();

		if (!playbackDevice.supportsAudioFormat(format)) {
			throw new ExecutableException("Audio device does not support the needed audio format.");
		}

		try {
			playbackDevice.setAudioFormat(format);
			playbackDevice.open();
			playbackDevice.start();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
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
			synchronized (thread) {
				thread.interrupt();
			}

			audioSource.reset();
		}
		catch (Exception e) {
			throw new ExecutableException("Audio source could not be reset.", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			playbackDevice.close();
			audioSource.reset();
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



	private class AudioReaderTask implements Runnable {

		@Override
		public void run() {
			int bytesRead;
			ExecutableState state;

			while (playbackDevice.isOpen()) {
				state = getState();

				if (state == ExecutableState.Started) {
					try {
						PlaybackData<byte[]> samples = audioSource.take();
						
						if (nonNull(samples)) {
							bytesRead = samples.getData().length;

							Synchronizer.setAudioTime(samples.getTimestamp());

							if (bytesRead > 0 && playbackDevice.isOpen()) {
								playbackDevice.write(samples.getData(), 0, bytesRead); // TODO check deviceBufferSize
							}
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

			try {
				playbackDevice.stop();
				playbackDevice.close();
			}
			catch (Exception e) {
				LOG.error("Stop audio playback device failed.", e);
			}
		}

	}

}
