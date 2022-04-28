/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.WavFileSink;

/**
 * Mixes multiple independent audio streams into a single audio stream.
 *
 * @author Alex Andres
 */
public class AudioMixer extends ExecutableBase {

	private AudioFormat audioFormat;

	private AudioSink audioSink;

	private RingBuffer ringBuffer;

	private File outputFile;

	private volatile boolean mix;


	/**
	 * Get the audio format of audio samples for this sink.
	 *
	 * @return The audio format of samples to write.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	/**
	 * @param audioFormat The audio format of the mixed audio stream.
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}

	/**
	 * @param file The audio file in which the mixed audio stream is written
	 *             to.
	 */
	public void setOutputFile(File file) {
		this.outputFile = file;
	}

	/**
	 * @return The audio file which contains the mixed audio stream.
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * @param mix True to mix all audio streams into the target stream.
	 */
	public void setMixAudio(boolean mix) {
		this.mix = mix;
	}

	/**
	 * @param frame The audio frame that should be mixed into the target stream.
	 */
	public void addAudioFrame(final AudioFrame frame) {
		if (!started() || !mix) {
			return;
		}

		int length = frame.getFrames() * (frame.getBitsPerSample() / 8);

		ringBuffer.write(frame.getData(), 0, length);
	}

	/**
	 * Write and mix the provided audio data.
	 *
	 * @param data   The audio data to mix with other streams.
	 * @param offset The offset in the provided audio data.
	 * @param length The length of the provided audio data to write.
	 *
	 * @return The number of written bytes into the mixed audio stream.
	 *
	 * @throws IOException If the audio data could not be mixed.
	 */
	public int write(byte[] data, int offset, int length) throws IOException {
		if (!mix) {
			return audioSink.write(data, offset, length);
		}

		byte[] buffer = new byte[length];

		if (ringBuffer.available() > 1) {
			int read = ringBuffer.read(buffer);

			ByteBuffer buffer1 = ByteBuffer.wrap(data, offset, length);
			ByteBuffer buffer2 = ByteBuffer.wrap(buffer, 0, read);

			AudioUtils.mixAudio(buffer1, buffer2, buffer);
		}
		else {
			System.arraycopy(data, 0, buffer, 0, length);
		}

		return audioSink.write(buffer, 0, length);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		ringBuffer = new RingBuffer(1024 * 1024);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			audioSink = new WavFileSink(outputFile);
			audioSink.setAudioFormat(audioFormat);
			audioSink.open();
		}
		catch (IOException e) {
			throw new ExecutableException("Could not create audio sink", e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		ringBuffer.clear();

		try {
			audioSink.close();
		}
		catch (IOException e) {
			throw new ExecutableException("Could not close audio sink", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}
}
