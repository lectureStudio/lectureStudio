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

/**
 * An input stream that supports dynamic exclusion of byte ranges.
 * <p>
 * Exclusion intervals use exclusive end semantics: [start, end) means bytes
 * from start (inclusive) to end (exclusive) are excluded.
 * <p>
 * The stream maintains two coordinate systems:
 * <ul>
 *   <li><b>Physical position</b>: The actual byte position in the underlying stream</li>
 *   <li><b>Virtual position</b>: The logical position after exclusions are applied</li>
 * </ul>
 * <p>
 * Exclusions are always kept sorted and merged to ensure consistent behavior.
 */
public class DynamicInputStream extends InputStream implements Cloneable {

	/** The underlying input stream. */
	protected final InputStream stream;

	/** 
	 * The canonical list of exclusion intervals, always sorted by start position
	 * and non-overlapping. Uses exclusive end semantics [start, end).
	 */
	protected List<Interval<Long>> exclusions = new ArrayList<>();

	/** Audio filters mapped to their applicable intervals (in physical coordinates). */
	protected Map<AudioFilter, Interval<Long>> filters = new HashMap<>();

	/** Current physical position in the underlying stream. */
	private long physicalPosition = 0;

	/** Total length of the underlying stream (cached for efficiency). */
	private long streamLength = -1;

	/**
	 * Creates a new instance of {@link DynamicInputStream} with the specified input stream.
	 *
	 * @param inputStream The underlying input stream.
	 */
	public DynamicInputStream(InputStream inputStream) {
		this.stream = inputStream;
	}

	/**
	 * Adds an exclusion interval. The interval will be merged with any
	 * overlapping existing exclusions.
	 * <p>
	 * Interval uses exclusive end semantics: [start, end).
	 *
	 * @param interval The interval to exclude (in physical coordinates).
	 */
	public void addExclusion(Interval<Long> interval) {
		if (interval == null || interval.getStart() >= interval.getEnd()) {
			return;
		}

		exclusions.add(new Interval<>(interval.getStart(), interval.getEnd()));
		normalizeExclusions();
	}

	/**
	 * Removes an exclusion interval. Only exact matches are removed.
	 *
	 * @param interval The interval to remove.
	 */
	public void removeExclusion(Interval<Long> interval) {
		exclusions.removeIf(iv -> 
			iv.getStart().equals(interval.getStart()) && 
			iv.getEnd().equals(interval.getEnd()));
	}

	/**
	 * Clears all exclusion intervals.
	 */
	public void clearExclusions() {
		exclusions.clear();
	}

	/**
	 * Returns a defensive copy of the exclusion intervals.
	 *
	 * @return A new list containing copies of all exclusion intervals.
	 */
	public List<Interval<Long>> getExclusions() {
		List<Interval<Long>> copy = new ArrayList<>(exclusions.size());
		for (Interval<Long> iv : exclusions) {
			copy.add(new Interval<>(iv.getStart(), iv.getEnd()));
		}
		return copy;
	}

	/**
	 * Sets the exclusions from a list, replacing any existing exclusions.
	 * The intervals will be normalized (sorted and merged).
	 *
	 * @param intervals The new exclusion intervals.
	 */
	public void setExclusions(List<Interval<Long>> intervals) {
		exclusions.clear();
		if (intervals != null) {
			for (Interval<Long> iv : intervals) {
				if (iv.getStart() < iv.getEnd()) {
					exclusions.add(new Interval<>(iv.getStart(), iv.getEnd()));
				}
			}
		}
		normalizeExclusions();
	}

	/**
	 * Converts a virtual position to a physical position.
	 * <p>
	 * Virtual positions represent the logical byte index after all exclusions
	 * have been applied. Physical positions are actual byte indices in the
	 * underlying stream.
	 *
	 * @param virtualPos The virtual position.
	 *
	 * @return The corresponding physical position.
	 */
	public long virtualToPhysical(long virtualPos) {
		long physical = virtualPos;

		for (Interval<Long> ex : exclusions) {
			if (ex.getStart() <= physical) {
				// This exclusion comes before or at our current position.
				physical += ex.getEnd() - ex.getStart();
			}
			else {
				// Exclusions are sorted, no more can affect us.
				break;
			}
		}

		return physical;
	}

	/**
	 * Converts a physical position to a virtual position.
	 *
	 * @param physicalPos The physical position.
	 *
	 * @return The corresponding virtual position, or -1 if the physical position
	 *         is within an excluded region.
	 */
	public long physicalToVirtual(long physicalPos) {
		long virtual = physicalPos;

		for (Interval<Long> ex : exclusions) {
			if (ex.getEnd() <= physicalPos) {
				// This exclusion is entirely before our position.
				virtual -= ex.getEnd() - ex.getStart();
			}
			else if (ex.getStart() <= physicalPos) {
				// Position is within an excluded region.
				return -1;
			}
			else {
				// Exclusions are sorted, no more can affect us.
				break;
			}
		}

		return virtual;
	}

	/**
	 * Calculates the total length of all excluded bytes.
	 *
	 * @return The total excluded length.
	 */
	public long getExcludedLength() {
		long total = 0;
		for (Interval<Long> iv : exclusions) {
			total += iv.getEnd() - iv.getStart();
		}
		return total;
	}

	/**
	 * Returns the current physical position in the stream.
	 *
	 * @return The current physical position.
	 */
	public long getPosition() {
		return physicalPosition;
	}

	/**
	 * Returns the current virtual position in the stream.
	 *
	 * @return The current virtual position.
	 */
	public long getVirtualPosition() {
		return physicalToVirtual(physicalPosition);
	}

	/**
	 * Associates an audio filter with a specific interval (in physical coordinates).
	 *
	 * @param filter   The audio filter.
	 * @param interval The interval where the filter applies.
	 */
	public void setAudioFilter(AudioFilter filter, Interval<Long> interval) {
		filters.put(filter, new Interval<>(interval.getStart(), interval.getEnd()));
	}

	/**
	 * Removes an audio filter.
	 *
	 * @param filter The filter to remove.
	 */
	public void removeAudioFilter(AudioFilter filter) {
		filters.remove(filter);
	}

	@Override
	public synchronized int read() throws IOException {
		byte[] single = new byte[1];
		int result = read(single, 0, 1);
		return result == -1 ? -1 : single[0] & 0xFF;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
		if (length == 0) {
			return 0;
		}

		// Skip any excluded regions at the current position.
		skipExcludedRegions();

		// Find the next exclusion boundary.
		long maxRead = length;
		for (Interval<Long> ex : exclusions) {
			if (ex.getStart() > physicalPosition) {
				maxRead = Math.min(maxRead, ex.getStart() - physicalPosition);
				break;
			}
		}

		// Read up to the boundary.
		int toRead = (int) Math.min(length, maxRead);
		int bytesRead = stream.read(buffer, offset, toRead);

		if (bytesRead > 0) {
			applyAudioFilters(buffer, offset, bytesRead);
			physicalPosition += bytesRead;
		}

		return bytesRead;
	}

	@Override
	public synchronized long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}

		long remaining = n;
		long skipped = 0;

		while (remaining > 0) {
			// Skip any excluded regions at the current position.
			skipExcludedRegions();

			// Find distance to the next exclusion.
			long distanceToExclusion = Long.MAX_VALUE;
			for (Interval<Long> ex : exclusions) {
				if (ex.getStart() > physicalPosition) {
					distanceToExclusion = ex.getStart() - physicalPosition;
					break;
				}
			}

			// Skip either to the exclusion or the remaining amount.
			long toSkip = Math.min(remaining, distanceToExclusion);
			long actualSkip = stream.skip(toSkip);

			if (actualSkip <= 0) {
				break;
			}

			physicalPosition += actualSkip;
			skipped += actualSkip;
			remaining -= actualSkip;
		}

		return skipped;
	}

	@Override
	public synchronized int available() throws IOException {
		long total = getStreamLength();
		long virtualLength = total - getExcludedLength();
		long virtualPos = physicalToVirtual(physicalPosition);
		
		if (virtualPos < 0) {
			virtualPos = 0;
		}
		
		return (int) Math.max(0, virtualLength - virtualPos);
	}

	@Override
	public synchronized void reset() throws IOException {
		stream.reset();

		physicalPosition = 0;
	}

	@Override
	public synchronized void mark(int readLimit) {
		stream.mark(readLimit);
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	public synchronized void close() throws IOException {
		stream.close();
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

		// Deep copy exclusions.
		for (Interval<Long> iv : exclusions) {
			clone.exclusions.add(new Interval<>(iv.getStart(), iv.getEnd()));
		}

		// Deep copy filters.
		for (var entry : filters.entrySet()) {
			Interval<Long> iv = entry.getValue();
			clone.filters.put(entry.getKey(), new Interval<>(iv.getStart(), iv.getEnd()));
		}

		clone.streamLength = this.streamLength;

		return clone;
	}

	/**
	 * Gets the total length of the underlying stream.
	 *
	 * @return The stream length.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	protected long getStreamLength() throws IOException {
		if (streamLength < 0) {
			streamLength = stream.available() + physicalPosition;
		}
		return streamLength;
	}

	/**
	 * Sets the stream length (for subclasses that know the exact length).
	 *
	 * @param length The stream length.
	 */
	protected void setStreamLength(long length) {
		this.streamLength = length;
	}

	/**
	 * Normalizes the exclusion list by sorting and merging overlapping intervals.
	 */
	private void normalizeExclusions() {
		if (exclusions.size() <= 1) {
			return;
		}

		// Sort by start position.
		// TODO
		exclusions.sort(Comparator.comparingLong(Interval::getStart));

		// Merge overlapping intervals.
		List<Interval<Long>> merged = new ArrayList<>();
		Interval<Long> current = exclusions.get(0);

		for (int i = 1; i < exclusions.size(); i++) {
			Interval<Long> next = exclusions.get(i);

			if (next.getStart() <= current.getEnd()) {
				// Overlapping or adjacent, merge them.
				current = new Interval<>(
					current.getStart(),
					Math.max(current.getEnd(), next.getEnd())
				);
			}
			else {
				merged.add(current);
				current = next;
			}
		}
		merged.add(current);

		exclusions = merged;
	}

	/**
	 * Skips over any excluded regions at the current physical position.
	 */
	private void skipExcludedRegions() throws IOException {
		boolean skipped;
		do {
			skipped = false;
			for (Interval<Long> ex : exclusions) {
				if (ex.getStart() <= physicalPosition && physicalPosition < ex.getEnd()) {
					// The current position is in an excluded region, skip to the end.
					long toSkip = ex.getEnd() - physicalPosition;
					long actualSkip = stream.skip(toSkip);
					physicalPosition += actualSkip;
					skipped = true;
					break;
				}
				else if (ex.getStart() > physicalPosition) {
					break; // No more exclusions can affect the current position.
				}
			}
		} while (skipped);
	}

	/**
	 * Applies audio filters to the given buffer region.
	 */
	private void applyAudioFilters(byte[] buffer, int offset, int length) {
		long startPos = physicalPosition;
		long endPos = physicalPosition + length;

		for (var entry : filters.entrySet()) {
			Interval<Long> filterRange = entry.getValue();
			AudioFilter filter = entry.getKey();

			// Calculate overlap between buffer region and filter range.
			long overlapStart = Math.max(startPos, filterRange.getStart());
			long overlapEnd = Math.min(endPos, filterRange.getEnd());

			if (overlapStart < overlapEnd) {
				int bufferOffset = offset + (int) (overlapStart - startPos);
				int processLength = (int) (overlapEnd - overlapStart);
				filter.process(buffer, bufferOffset, processLength);
			}
		}
	}
}
