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

import org.lecturestudio.avdev.AudioSource;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.AudioPlaybackProgressListener;
import org.lecturestudio.core.audio.Player;
import org.lecturestudio.core.audio.io.AudioPlaybackBuffer;
import org.lecturestudio.core.io.PlaybackData;
import org.lecturestudio.core.net.Synchronizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extended AVdev audio player implementation.
 *
 * @author Alex Andres
 */
public class AvdevAudioPlayerExt extends ExecutableBase implements Player {

	private static final Logger LOG = LogManager.getLogger(AvdevAudioPlayerExt.class);

	/** The audio playback device. */
	private final AVdevAudioOutputDevice playbackDevice;

	/** The audio source. */
	private final AudioPlaybackBuffer audioSource;

	/** The player state listener. */
	private ExecutableStateListener stateListener;


	/**
	 * Create an AvdevAudioPlayerExt with the specified playback device and
	 * audio source.
	 *
	 * @param device The audio playback device.
	 * @param source The audio source.
	 */
	public AvdevAudioPlayerExt(AVdevAudioOutputDevice device, AudioPlaybackBuffer source) {
		if (isNull(device)) {
			throw new NullPointerException("Missing audio playback device.");
		}
		if (isNull(source)) {
			throw new NullPointerException("Missing audio source.");
		}

		this.playbackDevice = device;
		this.audioSource = source;
	}

	@Override
	public void setVolume(float volume) {
		if (volume < 0 || volume > 1) {
			return;
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

		try {
			playbackDevice.setSource(new AudioSource() {

				int bytesRead = 0;

				@Override
				public int read(byte[] data, int offset, int length) throws IOException {
					try {
						PlaybackData<byte[]> samples = audioSource.take();

						if (nonNull(samples)) {
							bytesRead = samples.getData().length;

							Synchronizer.setAudioTime(samples.getTimestamp());

							if (bytesRead != length) {
								LOG.warn("Buffer sizes differ - required: " + length + ", playback buffer: " + bytesRead);
							}

							int size = Math.min(bytesRead, length);

							System.arraycopy(samples.getData(), 0, data, 0, size);
						}
					}
					catch (Exception e) {
						LOG.error("Read playback buffer failed." , e);

						try {
							stop();
						}
						catch (ExecutableException ex) {
							throw new IOException(ex);
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
	protected void suspendInternal() throws ExecutableException {
		try {
			playbackDevice.stop();
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
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		audioSource.reset();
		
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

}
