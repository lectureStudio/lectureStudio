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

package org.lecturestudio.core.io;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.util.AudioUtils;

public class RandomAccessAudioStream extends DynamicInputStream {

	private final DynamicInputStream inputStream;

	private final long streamLength;

	private final boolean encoded;

	private AudioFormat audioFormat;

	/**
	 * Create a new instance of {@link RandomAccessAudioStream} with the specified file.
	 * (Creates a new {@link RandomAccessStream} with the file and calls
	 * {@link #RandomAccessAudioStream(DynamicInputStream)} with it.)
	 *
	 * @param file The file.
	 */
	public RandomAccessAudioStream(File file) throws IOException {
		this(new RandomAccessStream(file));
	}

	/**
	 * Create a new instance of {@link RandomAccessAudioStream} with the specified inputStream.
	 * (Calls {@link #RandomAccessAudioStream(DynamicInputStream, boolean)} with the input stream and {@code false}
	 * as {@code encoded} parameter).
	 *
	 * @param inputStream The input stream.
	 */
	public RandomAccessAudioStream(DynamicInputStream inputStream) throws IOException {
		this(inputStream, false);
	}

	public RandomAccessAudioStream(DynamicInputStream inputStream, boolean encoded) throws IOException {
		super(encoded ? inputStream : getAudioStream(inputStream));

		this.encoded = encoded;
		this.inputStream = inputStream;

		if (stream instanceof AudioInputStream) {
			audioFormat = AudioUtils.createAudioFormat(((AudioInputStream) stream).getFormat());
		}

		streamLength = stream.available();
	}

	/**
	 * Get the audio format.
	 *
	 * @return The audio format.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	/**
	 * Set a new audio format.
	 *
	 * @param targetFormat The new audio format.
	 */
	public void setAudioFormat(AudioFormat targetFormat) {
		this.audioFormat = targetFormat;
	}

	public void addExclusiveMillis(Interval<Long> interval) {
		long start = AudioUtils.getAudioBytePosition(audioFormat, interval.getStart());
		long end = AudioUtils.getAudioBytePosition(audioFormat, interval.getEnd());

		addExclusion(new Interval<>(0L, start));

		if (end < streamLength) {
			addExclusion(new Interval<>(end, streamLength));
		}
	}

	public void removeExclusiveMillis(Interval<Long> interval) {
		long start = AudioUtils.getAudioBytePosition(audioFormat, interval.getStart());
		long end = AudioUtils.getAudioBytePosition(audioFormat, interval.getEnd());

		Interval<Long> iv1 = new Interval<>(0L, start);
		Interval<Long> iv2 = new Interval<>(end, streamLength);

		boundInterval(iv1);
		boundInterval(iv2);

		super.removeExclusion(iv1);
		super.removeExclusion(iv2);
	}

	public void addExclusionMillis(long start, long end) {
		start = AudioUtils.getAudioBytePosition(audioFormat, start);
		end = AudioUtils.getAudioBytePosition(audioFormat, end);

		addExclusion(new Interval<>(start, end));
	}

	public long getLengthInMillis() {
		AudioFormat format = getAudioFormat();

		double bytesPerSecond = AudioUtils.getBytesPerSecond(format);

		return (long) ((getLength() / (float) bytesPerSecond) * 1000);
	}

	@Override
	public int available() {
		// Take future exclusion into account.
		long toExclude = getToExcludeLength();

		return (int) (streamLength - getPosition() - toExclude);
	}

	@Override
	public void addExclusion(Interval<Long> interval) {
		if (interval.lengthLong() == 0) {
			return;
		}

		boundInterval(interval);

		super.addExclusion(interval);
	}

	@Override
	public RandomAccessAudioStream clone() {
		RandomAccessAudioStream clone;

		try {
			clone = new RandomAccessAudioStream(inputStream.clone(), encoded);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (Interval<Long> iv : exclusions) {
			clone.addExclusion(new Interval<>(iv.getStart(), iv.getEnd()));
		}
		for (var entry : filters.entrySet()) {
			clone.setAudioFilter(entry.getKey(), entry.getValue());
		}

		return clone;
	}

	public synchronized long getLength() {
		return streamLength - getExcludedLength();
	}

	public void write(SeekableByteChannel channel) throws IOException {
		WaveOutputStream outputStream = new WaveOutputStream(channel);
		outputStream.setAudioFormat(audioFormat);

		byte[] buffer = new byte[8192];
		int read;

		while ((read = read(buffer)) > 0) {
			outputStream.write(buffer, 0, read);
		}

		outputStream.close();
	}

	private void boundInterval(Interval<Long> interval) {
		if (interval.lengthLong() == 0) {
			return;
		}
		if (interval.getStart() < 70) {
			interval.set(72L, interval.getEnd());
		}
		if (interval.getEnd() > streamLength) {
			interval.set(interval.getStart(), streamLength);
		}
	}

	private static AudioInputStream getAudioStream(DynamicInputStream inputStream) {
		try {
			return AudioSystem.getAudioInputStream(inputStream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
