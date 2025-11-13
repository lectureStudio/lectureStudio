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

import org.lecturestudio.core.audio.filter.AudioFilter;
import org.lecturestudio.core.model.Interval;

public class DynamicInputStream extends InputStream implements Cloneable {

	protected List<Interval<Long>> exclusions = new ArrayList<>();

	protected List<Interval<Long>> exclude = new ArrayList<>();

	protected Map<AudioFilter, Interval<Long>> filters = new HashMap<>();

	protected InputStream stream;

	private long readPointer = 0;


	/**
	 * Creates a new instance of {@link DynamicInputStream} with the specified
	 * input stream.
	 *
	 * @param inputStream The input stream.
	 */
	public DynamicInputStream(InputStream inputStream) {
		stream = inputStream;
	}

	/**
	 * Associate an {@link Interval} with the specified filter in the
	 * {@link #filters} map.
	 *
	 * @param filter   The filter with which the {@link Interval} should be
	 *                 associated.
	 * @param interval The {@link Interval}
	 */
	public void setAudioFilter(AudioFilter filter, Interval<Long> interval) {
		filters.put(filter, interval);
	}

	/**
	 * Removes the specified filter from {@link #filters}.
	 *
	 * @param filter The filter to be removed.
	 */
	public void removeAudioFilter(AudioFilter filter) {
		filters.remove(filter);
	}

	/**
	 * Add the specified {@link Interval} to {@link #exclusions} and
	 * {@link #exclude}.
	 *
	 * @param interval The {@link Interval} to add.
	 */
	public void addExclusion(Interval<Long> interval) {
		exclusions.add(interval);
		exclude.add(interval);
	}

	/**
	 * Remove the specified {@link Interval} from {@link #exclusions} and
	 * {@link #exclude}.
	 *
	 * @param interval The {@link Interval} to remove.
	 */
	public void removeExclusion(Interval<Long> interval) {
		exclusions.remove(interval);
		exclude.remove(interval);
	}

	public void clearExclusions() {
		exclusions.clear();
		exclude.clear();
	}

	/**
	 * Get the list of active exclusion intervals.
	 * 
	 * <p><strong>Warning:</strong> This method returns a direct reference to the internal
	 * exclusions list. Modifying the returned list will affect this stream's behavior.
	 * For a safe snapshot, create a copy of the intervals:
	 * <pre>{@code
	 * List<Interval<Long>> snapshot = new ArrayList<>();
	 * for (Interval<Long> iv : stream.getExclusions()) {
	 *     snapshot.add(new Interval<>(iv.getStart(), iv.getEnd()));
	 * }
	 * }</pre>
	 * 
	 * @return The list of active exclusion intervals (may be modified during reads)
	 */
	public List<Interval<Long>> getExclusions() {
		return exclusions;
	}

	/**
	 * Get the position of the {@link DynamicInputStream}.
	 *
	 * @return The {@link #readPointer}.
	 */
	public long getPosition() {
		return readPointer;
	}

	/**
	 * Get the total length of all {@link Interval}s in {@link #exclusions}.
	 *
	 * @return The sum of the {@link Interval} lengths in {@link #exclusions}.
	 */
	public long getExcludedLength() {
		long excluded = 0;

		for (Interval<Long> iv : exclusions) {
			excluded += iv.lengthLong();
		}

		return excluded;
	}

	@Override
	public int available() throws IOException {
		// Take future exclusion into account.
		long toExclude = getToExcludeLength();

		return (int) (stream.available() - toExclude);
	}

	@Override
	public DynamicInputStream clone() {
		DynamicInputStream clone = new DynamicInputStream(stream);

		try {
			clone.reset();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Clone the persistent exclude list (used for reset operations)
		// This ensures the clone has the same persistent exclusions that will be
		// restored when reset() is called
		clone.exclude.clear();
		for (Interval<Long> iv : exclude) {
			// Create new interval instances to avoid reference sharing
			clone.exclude.add(new Interval<>(iv.getStart(), iv.getEnd()));
		}

		// Clone the active exclusions list
		// Note: exclusions may have been modified during reads (items removed as we pass them),
		// so we need to clone the current state, not just repopulate from exclude
		clone.exclusions.clear();
		for (Interval<Long> iv : exclusions) {
			// Create new interval instances to avoid reference sharing
			Interval<Long> clonedInterval = new Interval<>(iv.getStart(), iv.getEnd());
			clone.exclusions.add(clonedInterval);
		}

		// Clone audio filters with new interval instances
		for (var entry : filters.entrySet()) {
			Interval<Long> filterInterval = entry.getValue();
			Interval<Long> clonedFilterInterval = new Interval<>(
				filterInterval.getStart(), filterInterval.getEnd());
			clone.setAudioFilter(entry.getKey(), clonedFilterInterval);
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
				// Skip to one byte after the exclusion interval
				long bytesToSkip = (iv.getEnd() + 1) - lpos;
				long skipped = stream.skip(bytesToSkip);
				readPointer += skipped;

				read = stream.read();

				if (read >= 0) {
					readPointer++;
				}

				iter.remove();

				foundGap = true;
				break;
			}
		}

		if (!foundGap) {
			read = stream.read();
			if (read >= 0) {
				readPointer++;
			}
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
		if (n <= 0) {
			return 0;
		}

		long logicalTargetPos = readPointer + n;
		long padding = 0;

		// Calculate padding by finding all exclusions between current position and target
		Iterator<Interval<Long>> iter = exclusions.iterator();

		while (iter.hasNext()) {
			Interval<Long> iv = iter.next();

			// Check if exclusion overlaps with the skip range
			if (iv.getEnd() >= readPointer && iv.getStart() <= logicalTargetPos) {
				// Exclusion overlaps with skip range
				long overlapStart = Math.max(iv.getStart(), readPointer);
				long overlapEnd = Math.min(iv.getEnd(), logicalTargetPos);
				
				if (overlapStart <= overlapEnd) {
					// Add the length of the overlapping exclusion (inclusive)
					padding += overlapEnd - overlapStart + 1;
				}
				
				// Remove exclusion if we've completely passed it
				if (iv.getEnd() < readPointer) {
					iter.remove();
				}
			}
		}

		// Skip physical bytes (logical bytes + padding for exclusions)
		long physicalBytesToSkip = n + padding;
		long skipped = stream.skip(physicalBytesToSkip);

		readPointer += skipped;

		// Return the logical bytes skipped (excluding padding)
		// If we couldn't skip all requested bytes, return what we actually skipped logically
		if (skipped < physicalBytesToSkip) {
			// Adjust for partial skip
			long logicalSkipped = Math.max(0, skipped - padding);
			return logicalSkipped;
		}

		return n;
	}

	/**
	 * Calculate the padding (total length of exclusions) that occurs before
	 * the specified interval's start position in the logical stream.
	 * 
	 * @param interval The interval to calculate padding for
	 * @param <T>      The {@link Number} type of the specified {@link Interval}.
	 *
	 * @return The calculated padding (total bytes excluded before interval start).
	 */
	public <T extends Number> long getPadding(Interval<T> interval) {
		long padding = 0;
		long logicalStart = interval.getStart().longValue();

		// Sort exclusions by start position for accurate calculation
		List<Interval<Long>> sortedExclusions = new ArrayList<>(exclusions);
		sortedExclusions.sort(Comparator.comparingLong(Interval::getStart));

		for (Interval<Long> iv : sortedExclusions) {
			// Only count exclusions that end before or at the logical start position
			if (iv.getEnd() < logicalStart) {
				padding += iv.lengthLong();
			}
			// If exclusion starts before logical start but ends after, count partial
			else if (iv.getStart() < logicalStart && iv.getEnd() >= logicalStart) {
				padding += logicalStart - iv.getStart();
			}
		}

		return padding;
	}

	/**
	 * Adjusts the given interval to account for exclusions that overlap with it.
	 * Returns an interval in the physical stream coordinates that corresponds to
	 * the logical interval after accounting for excluded regions.
	 * 
	 * This method does NOT modify the exclusions list.
	 * 
	 * @param interval The logical interval to adjust
	 * @return The adjusted interval in physical stream coordinates
	 */
	public Interval<Long> getEnclosedPadding(Interval<Long> interval) {
		long logicalStart = interval.getStart();
		long logicalEnd = interval.getEnd();
		long physicalStart = logicalStart;
		long physicalEnd = logicalEnd;

		// Create a copy of exclusions to avoid modifying the original
		List<Interval<Long>> exclusionsCopy = new ArrayList<>(exclusions);
		exclusionsCopy.sort(Comparator.comparingLong(Interval::getStart));

		// Calculate padding before the interval start
		long paddingBefore = 0;
		for (Interval<Long> iv : exclusionsCopy) {
			if (iv.getEnd() < logicalStart) {
				paddingBefore += iv.lengthLong();
			}
			else if (iv.getStart() < logicalStart && iv.getEnd() >= logicalStart) {
				paddingBefore += logicalStart - iv.getStart();
			}
		}

		physicalStart = logicalStart + paddingBefore;

		// Calculate padding within the interval
		long paddingWithin = 0;
		for (Interval<Long> iv : exclusionsCopy) {
			// Check if exclusion overlaps with the interval
			if (iv.getStart() < logicalEnd && iv.getEnd() >= logicalStart) {
				long overlapStart = Math.max(iv.getStart(), logicalStart);
				long overlapEnd = Math.min(iv.getEnd(), logicalEnd);
				if (overlapStart <= overlapEnd) {
					paddingWithin += overlapEnd - overlapStart + 1;
				}
			}
		}

		physicalEnd = logicalEnd + paddingBefore + paddingWithin;

		return new Interval<>(physicalStart, physicalEnd);
	}

	/**
	 * Get the length to exclude. (Sums up the length of every {@link Interval}
	 * in {@link #exclusions} that is not part of another {@link Interval} of
	 * {@link #exclusions}.)
	 *
	 * @return The length to exclude.
	 */
	protected long getToExcludeLength() {
		long toExclude = 0;

		Set<Integer> toSkip = new HashSet<>();
		int count = exclusions.size();

		for (int i = 0; i < count; i++) {
			Interval<Long> iv = exclusions.get(i);

			for (int j = i + 1; j < count; j++) {
				Interval<Long> iv2 = exclusions.get(j);

				if (iv.contains(iv2)) {
					toSkip.add(j);
				}
				if (iv2.contains(iv)) {
					toSkip.add(i);
				}
			}
		}

		for (int i = 0; i < count; i++) {
			if (!toSkip.contains(i)) {
				Interval<Long> iv = exclusions.get(i);
				toExclude += iv.lengthLong();
			}
		}

		return toExclude;
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
				// Current position is inside an exclusion - skip to after it
				long bytesToSkip = (iv.getEnd() + 1) - lpos;
				long skipped = stream.skip(bytesToSkip);
				readPointer += skipped;
				foundGap = true;

				iter.remove();
				break;
			}
			else if ((lpos < iv.getStart() && rpos > iv.getEnd()) || iv.contains(rpos)) {
				// Read spans across an exclusion or ends inside it
				int lenBefore = (int) (iv.getStart() - lpos);
				
				// Read data before exclusion
				if (lenBefore > 0) {
					int readBefore = stream.read(buffer, offset, lenBefore);
					read += readBefore;
					readPointer += readBefore;
					
					processAudioFilters(buffer, offset, readBefore);
					offset += readBefore;
				}

				// Skip the exclusion
				long bytesToSkip = (iv.getEnd() + 1) - iv.getStart();
				long skipped = stream.skip(bytesToSkip);
				readPointer += skipped;

				// If read ended inside exclusion, we're done
				if (iv.contains(rpos)) {
					foundGap = true;
					iter.remove();
					break;
				}

				// Continue reading after exclusion if there's more to read
				int remaining = length - read;
				if (remaining > 0) {
					int readAfter = stream.read(buffer, offset, remaining);
					read += readAfter;
					readPointer += readAfter;
					
					processAudioFilters(buffer, offset, readAfter);
				}

				foundGap = true;
				iter.remove();
				break;
			}
		}

		if (!foundGap) {
			// No exclusion found - normal read
			read = stream.read(buffer, offset, length);
			
			if (read > 0) {
				processAudioFilters(buffer, offset, read);
				readPointer += read;
			}
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
