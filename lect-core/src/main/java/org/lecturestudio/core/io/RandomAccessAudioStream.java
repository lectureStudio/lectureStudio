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
		// Calculate available bytes accounting for:
		// 1. Current position in the stream
		// 2. Exclusions that occur after the current position
		long currentPos = getPosition();
		long remainingBytes = streamLength - currentPos;
		
		// Calculate exclusions that occur after current position
		long toExcludeAfter = 0;
		for (Interval<Long> iv : exclusions) {
			// Only count exclusions that start after or at current position
			if (iv.getStart() >= currentPos) {
				// Full exclusion is after current position
				toExcludeAfter += iv.lengthLong();
			}
			else if (iv.getEnd() >= currentPos) {
				// Exclusion overlaps with current position - count only the part after
				toExcludeAfter += iv.getEnd() - currentPos + 1;
			}
		}

		return (int) Math.max(0, remainingBytes - toExcludeAfter);
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

		// Copy audio format
		clone.audioFormat = audioFormat;

		// Clear any exclusions that might have been inherited from inputStream.clone()
		// We need to clone our own exclusions state
		clone.clearExclusions();
		
		// Clone persistent exclusions (exclude list) - these are restored on reset()
		// We clone from exclude to ensure reset() works correctly on the clone
		for (Interval<Long> iv : exclude) {
			// Create new interval instances to avoid reference sharing
			clone.addExclusion(new Interval<>(iv.getStart(), iv.getEnd()));
		}
		
		// The active exclusions list (exclusions) will be populated by addExclusion()
		// above, which adds to both exclusions and exclude. However, if some exclusions
		// were removed from the active list during reads, we need to match that state.
		// Since we're cloning, we want the clone to start fresh, so having all exclusions
		// in the active list is correct - they'll be removed as the clone reads past them.

		// Clone audio filters with new interval instances
		for (var entry : filters.entrySet()) {
			Interval<Long> filterInterval = entry.getValue();
			Interval<Long> clonedFilterInterval = new Interval<>(
				filterInterval.getStart(), filterInterval.getEnd());
			clone.setAudioFilter(entry.getKey(), clonedFilterInterval);
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
	 * Bounds the interval to valid stream positions.
	 * For audio streams, this typically means avoiding the file header
	 * (first ~44 bytes for WAV files, but can vary).
	 * 
	 * @param interval The interval to bound
	 */
	private void boundInterval(Interval<Long> interval) {
		if (interval.lengthLong() == 0) {
			return;
		}
		
		// Ensure start is not negative
		long start = Math.max(0, interval.getStart());
		
		// For audio files, avoid excluding the header (typically first 44 bytes for WAV)
		// Use a conservative approach: if start is very small, move it to after header
		// This is a heuristic - ideally we'd detect actual header size
		final long MIN_AUDIO_HEADER_SIZE = 44; // Typical WAV header size
		if (start < MIN_AUDIO_HEADER_SIZE) {
			start = MIN_AUDIO_HEADER_SIZE;
		}
		
		// Ensure end doesn't exceed stream length
		long end = Math.min(streamLength, interval.getEnd());
		
		// Ensure start <= end
		if (start > end) {
			start = end;
		}
		
		interval.set(start, end);
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
