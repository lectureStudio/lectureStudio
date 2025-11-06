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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lecturestudio.core.audio.filter.AudioFilter;
import org.lecturestudio.core.model.Interval;

/**
 * A thread-safe input stream that supports dynamic exclusion of byte ranges
 * and audio filtering. This class allows skipping specific byte intervals
 * during reading and applying audio filters to specific ranges.
 * 
 * <p>This class is thread-safe and can be used concurrently by multiple threads.
 * All state-modifying operations are synchronized to prevent race conditions.
 * 
 * @author Alex Andres
 */
public class DynamicInputStream extends InputStream implements Cloneable {

	/** Thread-safe list of current exclusions (modified during reading) */
	protected final List<Interval<Long>> exclusions = new CopyOnWriteArrayList<>();

	/** Thread-safe list of original exclusions (used for reset) */
	protected final List<Interval<Long>> exclude = new CopyOnWriteArrayList<>();

	/** Thread-safe map of audio filters and their intervals */
	protected final Map<AudioFilter, Interval<Long>> filters = new ConcurrentHashMap<>();

	/** The underlying input stream */
	protected final InputStream stream;

	/** Current read position (volatile for thread safety) */
	private volatile long readPointer = 0;


	/**
	 * Creates a new instance of {@link DynamicInputStream} with the specified
	 * input stream.
	 *
	 * @param inputStream The input stream (must not be null).
	 * @throws IllegalArgumentException if inputStream is null
	 */
	public DynamicInputStream(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("Input stream cannot be null");
		}
		this.stream = inputStream;
	}

	/**
	 * Associate an {@link Interval} with the specified filter in the
	 * {@link #filters} map.
	 *
	 * @param filter   The filter with which the {@link Interval} should be
	 *                 associated (must not be null).
	 * @param interval The {@link Interval} (must not be null).
	 * @throws IllegalArgumentException if filter or interval is null
	 */
	public synchronized void setAudioFilter(AudioFilter filter, Interval<Long> interval) {
		if (filter == null) {
			throw new IllegalArgumentException("Filter cannot be null");
		}
		if (interval == null) {
			throw new IllegalArgumentException("Interval cannot be null");
		}
		filters.put(filter, interval);
	}

	/**
	 * Removes the specified filter from {@link #filters}.
	 *
	 * @param filter The filter to be removed (must not be null).
	 * @throws IllegalArgumentException if filter is null
	 */
	public synchronized void removeAudioFilter(AudioFilter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Filter cannot be null");
		}
		filters.remove(filter);
	}

	/**
	 * Add the specified {@link Interval} to {@link #exclusions} and
	 * {@link #exclude}.
	 *
	 * @param interval The {@link Interval} to add (must not be null).
	 * @throws IllegalArgumentException if interval is null, if interval has negative bounds, or if interval has invalid bounds (end < start)
	 */
	public synchronized void addExclusion(Interval<Long> interval) {
		if (interval == null) {
			throw new IllegalArgumentException("Interval cannot be null");
		}
		if (interval.getStart() != null && interval.getStart() < 0) {
			throw new IllegalArgumentException("Interval start must be non-negative");
		}
		if (interval.getEnd() != null && interval.getEnd() < 0) {
			throw new IllegalArgumentException("Interval end must be non-negative");
		}
		if (interval.getStart() != null && interval.getEnd() != null && interval.getEnd() < interval.getStart()) {
			throw new IllegalArgumentException("Interval end must be greater than or equal to start");
		}
		exclusions.add(interval);
		exclude.add(interval);
	}

	/**
	 * Remove the specified {@link Interval} from {@link #exclusions} and
	 * {@link #exclude}.
	 *
	 * @param interval The {@link Interval} to remove (must not be null).
	 * @throws IllegalArgumentException if interval is null
	 */
	public synchronized void removeExclusion(Interval<Long> interval) {
		if (interval == null) {
			throw new IllegalArgumentException("Interval cannot be null");
		}
		exclusions.remove(interval);
		exclude.remove(interval);
	}

	/**
	 * Clear all exclusions from both {@link #exclusions} and {@link #exclude}.
	 */
	public synchronized void clearExclusions() {
		exclusions.clear();
		exclude.clear();
	}

	/**
	 * Get a copy of the current exclusions list.
	 * 
	 * @return A new list containing the current exclusions (defensive copy).
	 */
	public synchronized List<Interval<Long>> getExclusions() {
		return new ArrayList<>(exclusions);
	}

	/**
	 * Get the position of the {@link DynamicInputStream}.
	 *
	 * @return The current read position.
	 */
	public long getPosition() {
		return readPointer;
	}

	/**
	 * Reset the read position to 0.
	 * This is a protected method to allow subclasses to reset the position
	 * when needed (e.g., during cloning).
	 */
	protected synchronized void resetPosition() {
		readPointer = 0;
	}

	/**
	 * Get the total length of all {@link Interval}s in {@link #exclusions}.
	 * This method properly handles overlapping intervals by merging them and
	 * calculating the total excluded length without double-counting.
	 *
	 * @return The total excluded length after merging overlapping intervals.
	 */
	public synchronized long getExcludedLength() {
		if (exclusions.isEmpty()) {
			return 0;
		}

		// Create a copy and sort by start position
		List<Interval<Long>> sortedExclusions = new ArrayList<>(exclusions);
		sortedExclusions.sort(Interval::compareTo);

		long totalExcluded = 0;
		long currentStart = -1;
		long currentEnd = -1;

		for (Interval<Long> interval : sortedExclusions) {
			long start = interval.getStart();
			long end = interval.getEnd();

			if (currentStart == -1) {
				// First interval
				currentStart = start;
				currentEnd = end;
			} else if (start <= currentEnd) {
				// Overlapping or adjacent interval - extend current range
				currentEnd = Math.max(currentEnd, end);
			} else {
				// Non-overlapping interval - add previous range and start new one
				totalExcluded += currentEnd - currentStart;
				currentStart = start;
				currentEnd = end;
			}
		}

		// Add the last range
		if (currentStart != -1) {
			totalExcluded += currentEnd - currentStart;
		}

		return totalExcluded;
	}

	@Override
	public int available() throws IOException {
		// Take future exclusion into account.
		long toExclude = getToExcludeLength();
		long available = stream.available() - toExclude;
		
		// Check for integer overflow
		if (available > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else if (available < 0) {
			return 0;
		}
		
		return (int) available;
	}

	@Override
	public synchronized DynamicInputStream clone() {
		DynamicInputStream clone = new DynamicInputStream(stream);

		try {
			clone.reset();
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to reset cloned stream", e);
		}

		// Copy exclusions
		for (Interval<Long> iv : exclusions) {
			clone.addExclusion(new Interval<>(iv.getStart(), iv.getEnd()));
		}

		// Copy filters
		for (var entry : filters.entrySet()) {
			clone.setAudioFilter(entry.getKey(), entry.getValue());
		}

		return clone;
	}

	@Override
	public synchronized void close() throws IOException {
		stream.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		stream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	public synchronized int read() throws IOException {
		long lpos = readPointer;
		int read = 0;
		boolean foundGap = false;

		Iterator<Interval<Long>> iter = exclusions.iterator();

		while (iter.hasNext()) {
			Interval<Long> iv = iter.next();

			if (iv.contains(lpos)) {
				readPointer += stream.skip(iv.getEnd() - lpos + 1);

				read = stream.read();

				readPointer++;

				// CopyOnWriteArrayList doesn't support iterator.remove()
				// Remove directly from the list instead
				exclusions.remove(iv);

				foundGap = true;
				break;
			}
		}

		if (!foundGap) {
			read = stream.read();
			readPointer++;
		}

		return read;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return readInterval(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return readInterval(buffer, offset, length);
	}

	@Override
	public synchronized void reset() throws IOException {
		stream.reset();

		readPointer = 0;

		exclusions.clear();
		exclusions.addAll(exclude);
	}

	@Override
	public long skip(long n) throws IOException {
		long nextPos = readPointer + n;
		long padding = 0;

		// Collect intervals to remove first, since CopyOnWriteArrayList iterator
		// doesn't support remove() and we may need to remove multiple elements
		List<Interval<Long>> toRemove = new ArrayList<>();

		Iterator<Interval<Long>> iter = exclusions.iterator();

		while (iter.hasNext()) {
			Interval<Long> iv = iter.next();

			if (iv.getStart() <= nextPos) {
				padding += iv.lengthLong() + 1;
				nextPos += iv.lengthLong() + 1;

				toRemove.add(iv);
			}
		}

		// Remove collected intervals
		exclusions.removeAll(toRemove);

		long skipped = stream.skip(n + padding);

		readPointer += skipped;

		return skipped - padding;
	}

	/**
	 * @param interval
	 * @param <T>      The {@link Number} type of the specified
	 *                 {@link Interval}.
	 *
	 * @return The calculated padding.
	 */
	public <T extends Number> long getPadding(Interval<T> interval) {
		long padding = 0;

		long start = interval.getStart().longValue();

		for (Interval<Long> iv : exclusions) {
			if (iv.getStart() <= (start + padding)) {
				padding += iv.lengthLong();
			}
		}

		return padding;
	}

	public Interval<Long> getEnclosedPadding(Interval<Long> interval) {
		long start = interval.getStart();
		long end = interval.getEnd();

		Iterator<Interval<Long>> iter = exclusions.iterator();

		while (iter.hasNext()) {
			Interval<Long> iv = iter.next();

			if (interval.contains(iv)) {
				end += iv.lengthLong();

				// CopyOnWriteArrayList doesn't support iterator.remove()
				// Remove directly from the list instead
				exclusions.remove(iv);
				break;
			}
			else if (interval.contains(iv.getStart())) {
				end = start + interval.lengthLong() + iv.lengthLong();

				// CopyOnWriteArrayList doesn't support iterator.remove()
				// Remove directly from the list instead
				exclusions.remove(iv);
				break;
			}
			else if (interval.contains(iv.getEnd())) {
				start = iv.getStart();
				end -= iv.getEnd() - interval.getStart();

				// CopyOnWriteArrayList doesn't support iterator.remove()
				// Remove directly from the list instead
				exclusions.remove(iv);
				break;
			}
		}

		return new Interval<>(start, end);
	}

	/**
	 * Get the length to exclude. This method properly handles overlapping intervals
	 * by merging them and calculating the total excluded length without double-counting.
	 *
	 * @return The total length to exclude.
	 */
	protected synchronized long getToExcludeLength() {
		if (exclusions.isEmpty()) {
			return 0;
		}

		// Create a copy and sort by start position
		List<Interval<Long>> sortedExclusions = new ArrayList<>(exclusions);
		sortedExclusions.sort(Interval::compareTo);

		long totalExcluded = 0;
		long currentStart = -1;
		long currentEnd = -1;

		for (Interval<Long> interval : sortedExclusions) {
			long start = interval.getStart();
			long end = interval.getEnd();

			if (currentStart == -1) {
				// First interval
				currentStart = start;
				currentEnd = end;
			} else if (start <= currentEnd) {
				// Overlapping or adjacent interval - extend current range
				currentEnd = Math.max(currentEnd, end);
			} else {
				// Non-overlapping interval - add previous range and start new one
				totalExcluded += currentEnd - currentStart;
				currentStart = start;
				currentEnd = end;
			}
		}

		// Add the last range
		if (currentStart != -1) {
			totalExcluded += currentEnd - currentStart;
		}

		return totalExcluded;
	}

	private synchronized int readInterval(byte[] buffer, int offset, int length) throws IOException {
		long lpos = readPointer;
		long rpos = lpos + length;
		int read = 0;
		boolean foundGap = false;

		Iterator<Interval<Long>> iter = exclusions.iterator();

		while (iter.hasNext()) {
			Interval<Long> iv = iter.next();

			if (iv.contains(lpos)) {
				readPointer += stream.skip(iv.getEnd() - lpos + 1);
				foundGap = true;

				// CopyOnWriteArrayList doesn't support iterator.remove()
				// Remove directly from the list instead
				exclusions.remove(iv);
				break;
			}
			else if (lpos < iv.getStart() && rpos > iv.getEnd() || iv.contains(rpos)) {
				int len = (int) (iv.getStart() - lpos);

				read += stream.read(buffer, offset, len);

				processAudioFilters(buffer, offset, read);

				readPointer += stream.skip(iv.lengthLong() + 1);
				readPointer += read;
				foundGap = true;

				// CopyOnWriteArrayList doesn't support iterator.remove()
				// Remove directly from the list instead
				exclusions.remove(iv);
				break;
			}
		}

		if (!foundGap) {
			read += stream.read(buffer, offset, length);

			processAudioFilters(buffer, offset, length);

			readPointer += read;
		}

		return read;
	}

	private void processAudioFilters(byte[] buffer, int offset, int length) {
		long lpos = readPointer;
		long rpos = readPointer + length;
		boolean done = false;

		for (var entry : filters.entrySet()) {
			Interval<Long> iv = entry.getValue();
			AudioFilter filter = entry.getKey();

			if (iv.contains(lpos)) {
				if (rpos <= iv.getEnd()) {
					// Test if interval encloses the total read count.
					done = iv.contains(rpos);
				}

				int processLength = (int) Math.min(rpos, iv.getEnd());

				filter.process(buffer, offset, processLength);

				offset += processLength;
			}
			else if (iv.contains(rpos)) {
				// Interval contains right side of the buffer to process.
				done = true;

				int processLength = (int) (rpos - iv.getStart());
				int processOffset = offset + length - processLength;

				filter.process(buffer, processOffset, processLength);

				offset += processLength;
			}
			else if (lpos < iv.getStart() && rpos > iv.getEnd()) {
				// Total read count encloses the interval.
				int processOffset = (int) (iv.getStart() - lpos);
				int processLength = (int) (iv.getEnd() - iv.getStart());

				filter.process(buffer, processOffset, processLength);

				offset += processLength;
			}

			if (done) {
				break;
			}
		}
	}
}
