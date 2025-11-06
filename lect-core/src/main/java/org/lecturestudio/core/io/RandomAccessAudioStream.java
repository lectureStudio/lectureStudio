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

/**
 * A random access audio stream that extends DynamicInputStream with audio-specific
 * functionality. This class provides methods for handling audio exclusions in
 * millisecond units and supports audio format management.
 * 
 * <p>This class is thread-safe and inherits all thread safety guarantees from
 * DynamicInputStream.
 * 
 * @author Alex Andres
 */
public class RandomAccessAudioStream extends DynamicInputStream {

	/** The underlying dynamic input stream */
	private final DynamicInputStream inputStream;

	/** The total length of the stream in bytes */
	private final long streamLength;

	/** Whether the stream is encoded (not decoded to PCM) */
	private final boolean encoded;

	/** The audio format of this stream */
	private volatile AudioFormat audioFormat;

	/**
	 * Create a new instance of {@link RandomAccessAudioStream} with the specified file.
	 * (Creates a new {@link RandomAccessStream} with the file and calls
	 * {@link #RandomAccessAudioStream(DynamicInputStream)} with it.)
	 *
	 * @param file The audio file (must not be null).
	 * @throws IOException if the file cannot be opened or read
	 * @throws IllegalArgumentException if file is null
	 */
	public RandomAccessAudioStream(File file) throws IOException {
		this(createRandomAccessStream(file));
	}

	/**
	 * Create a new instance of {@link RandomAccessAudioStream} with the specified inputStream.
	 * (Calls {@link #RandomAccessAudioStream(DynamicInputStream, boolean)} with the input stream and {@code false}
	 * as {@code encoded} parameter).
	 *
	 * @param inputStream The input stream (must not be null).
	 * @throws IOException if the stream cannot be processed
	 * @throws IllegalArgumentException if inputStream is null
	 */
	public RandomAccessAudioStream(DynamicInputStream inputStream) throws IOException {
		this(validateInputStream(inputStream), false);
	}

	/**
	 * Create a new instance of {@link RandomAccessAudioStream} with the specified inputStream and encoding flag.
	 *
	 * @param inputStream The input stream (must not be null).
	 * @param encoded Whether the stream is encoded (not decoded to PCM).
	 * @throws IOException if the stream cannot be processed
	 * @throws IllegalArgumentException if inputStream is null
	 */
	public RandomAccessAudioStream(DynamicInputStream inputStream, boolean encoded) throws IOException {
		super(encoded ? inputStream : getAudioStream(inputStream));

		this.encoded = encoded;
		this.inputStream = inputStream;

		if (stream instanceof AudioInputStream audioInputStream) {
			audioFormat = AudioUtils.createAudioFormat(audioInputStream.getFormat());
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
	 * @param targetFormat The new audio format (must not be null).
	 * @throws IllegalArgumentException if targetFormat is null
	 */
	public synchronized void setAudioFormat(AudioFormat targetFormat) {
		if (targetFormat == null) {
			throw new IllegalArgumentException("Audio format cannot be null");
		}
		this.audioFormat = targetFormat;
	}

	/**
	 * Add an exclusive interval in milliseconds. This excludes everything except
	 * the specified interval from the stream.
	 *
	 * @param interval The interval to keep (must not be null).
	 * @throws IllegalArgumentException if interval is null
	 */
	public synchronized void addExclusiveMillis(Interval<Long> interval) {
		if (interval == null) {
			throw new IllegalArgumentException("Interval cannot be null");
		}
		if (audioFormat == null) {
			throw new IllegalStateException("Audio format is not set");
		}
		
		long start = AudioUtils.getAudioBytePosition(audioFormat, interval.getStart());
		long end = AudioUtils.getAudioBytePosition(audioFormat, interval.getEnd());

		addExclusion(new Interval<>(0L, start));

		if (end < streamLength) {
			addExclusion(new Interval<>(end, streamLength));
		}
	}

	/**
	 * Remove an exclusive interval in milliseconds.
	 *
	 * @param interval The interval to remove (must not be null).
	 * @throws IllegalArgumentException if interval is null
	 */
	public synchronized void removeExclusiveMillis(Interval<Long> interval) {
		if (interval == null) {
			throw new IllegalArgumentException("Interval cannot be null");
		}
		if (audioFormat == null) {
			throw new IllegalStateException("Audio format is not set");
		}
		
		long start = AudioUtils.getAudioBytePosition(audioFormat, interval.getStart());
		long end = AudioUtils.getAudioBytePosition(audioFormat, interval.getEnd());

		Interval<Long> iv1 = new Interval<>(0L, start);
		Interval<Long> iv2 = new Interval<>(end, streamLength);

		boundInterval(iv1);
		boundInterval(iv2);

		super.removeExclusion(iv1);
		super.removeExclusion(iv2);
	}

	/**
	 * Add an exclusion interval in milliseconds.
	 *
	 * @param start The start time in milliseconds.
	 * @param end The end time in milliseconds.
	 * @throws IllegalArgumentException if start > end
	 * @throws IllegalStateException if audio format is not set
	 */
	public synchronized void addExclusionMillis(long start, long end) {
		if (start > end) {
			throw new IllegalArgumentException("Start time cannot be greater than end time");
		}
		if (audioFormat == null) {
			throw new IllegalStateException("Audio format is not set");
		}
		
		long startBytes = AudioUtils.getAudioBytePosition(audioFormat, start);
		long endBytes = AudioUtils.getAudioBytePosition(audioFormat, end);

		addExclusion(new Interval<>(startBytes, endBytes));
	}

	/**
	 * Get the length of the stream in milliseconds.
	 *
	 * @return The length in milliseconds.
	 * @throws IllegalStateException if audio format is not set
	 */
	public synchronized long getLengthInMillis() {
		AudioFormat format = getAudioFormat();
		if (format == null) {
			throw new IllegalStateException("Audio format is not set");
		}

		double bytesPerSecond = AudioUtils.getBytesPerSecond(format);
		if (bytesPerSecond <= 0) {
			return 0;
		}

		return (long) ((getLength() / bytesPerSecond) * 1000);
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

		// Copy audio format if set
		if (audioFormat != null) {
			clone.setAudioFormat(audioFormat);
		}

		for (Interval<Long> iv : exclude) {
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

	/**
	 * Bound the interval to valid ranges. This ensures the interval doesn't
	 * start before the audio data begins or extend beyond the stream length.
	 * 
	 * @param interval The interval to bound (must not be null).
	 */
	private void boundInterval(Interval<Long> interval) {
		if (interval == null || interval.lengthLong() == 0) {
			return;
		}
		
		// Skip WAV header (typically 44 bytes, but use 70 for safety margin)
		// and ensure we don't start before the actual audio data
		// Only adjust if the interval would remain valid after adjustment
		if (interval.getStart() < 70 && interval.getEnd() > 72) {
			// Interval spans across the header boundary, adjust start to skip header
			interval.set(72L, interval.getEnd());
		}
		// If interval is entirely before 72, leave it as-is (may be valid for encoded streams)
		
		// Ensure we don't extend beyond the stream length
		if (interval.getEnd() > streamLength) {
			long newEnd = streamLength;
			// Only adjust if the interval would remain valid
			if (interval.getStart() < newEnd) {
				interval.set(interval.getStart(), newEnd);
			} else {
				// Interval is entirely beyond stream, set to zero length
				interval.set(newEnd, newEnd);
			}
		}
	}

	private static RandomAccessStream createRandomAccessStream(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		}
		return new RandomAccessStream(file);
	}
	
	private static DynamicInputStream validateInputStream(DynamicInputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("Input stream cannot be null");
		}
		return inputStream;
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
