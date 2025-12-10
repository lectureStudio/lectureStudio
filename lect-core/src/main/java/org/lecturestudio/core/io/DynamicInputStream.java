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

	public List<Interval<Long>> getExclusions() {
		return exclusions;
	}

	public List<Interval<Long>> getExcluded() {
		return exclude;
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

		for (Interval<Long> iv : exclude) {
			clone.exclusions.add(new Interval<>(iv.getStart(), iv.getEnd()));
			clone.exclude.add(new Interval<>(iv.getStart(), iv.getEnd()));
		}

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

				iter.remove();

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

		for (Interval<Long> iv : exclude) {
			exclusions.add(new Interval<>(iv.getStart(), iv.getEnd()));
		}
	}

	@Override
	public long skip(long n) throws IOException {
		long nextPos = readPointer + n;
		long padding = 0;

		Iterator<Interval<Long>> iter = exclusions.iterator();

		while (iter.hasNext()) {
			Interval<Long> iv = iter.next();

			if (iv.getStart() <= nextPos) {
				padding += iv.lengthLong() + 1;
				nextPos += iv.lengthLong() + 1;

				iter.remove();
			}
		}

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

				iter.remove();
				break;
			}
			else if (interval.contains(iv.getStart())) {
				end = start + interval.lengthLong() + iv.lengthLong();

				iter.remove();
				break;
			}
			else if (interval.contains(iv.getEnd())) {
				start = iv.getStart();
				end -= iv.getEnd() - interval.getStart();

				iter.remove();
				break;
			}
		}

		return new Interval<>(start, end);
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
				readPointer += stream.skip(iv.getEnd() - lpos + 1);
				foundGap = true;

				iter.remove();
				break;
			}
			else if (lpos < iv.getStart() && rpos > iv.getEnd() || iv.contains(rpos)) {
				int len = (int) (iv.getStart() - lpos);

				read += stream.read(buffer, offset, len);

				processAudioFilters(buffer, offset, read);

				readPointer += stream.skip(iv.lengthLong() + 1);
				readPointer += read;
				foundGap = true;

				iter.remove();
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
