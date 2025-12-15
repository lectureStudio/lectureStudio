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
 * An audio stream that supports random access and dynamic exclusion of audio segments.
 * <p>
 * This class extends {@link DynamicInputStream} with audio-specific functionality:
 * <ul>
 *   <li>Time-to-byte position conversion</li>
 *   <li>Audio format handling</li>
 *   <li>WAV file output</li>
 * </ul>
 * <p>
 * Exclusion intervals can be specified in either byte positions or milliseconds.
 * All intervals use exclusive end semantics: [start, end).
 */
public class RandomAccessAudioStream extends DynamicInputStream {

	/** Minimum byte position to preserve WAV header integrity. */
	private static final long MIN_BYTE_POSITION = 72;

	/** The original input stream for cloning. */
	private final DynamicInputStream sourceStream;

	/** Total length of the audio stream in bytes. */
	private final long totalLength;

	/** Whether the stream is encoded (not raw PCM). */
	private final boolean encoded;

	/** The audio format of the stream. */
	private AudioFormat audioFormat;

	/**
	 * Creates a new {@link RandomAccessAudioStream} from a file.
	 *
	 * @param file The audio file.
	 * @throws IOException If the file cannot be read.
	 */
	public RandomAccessAudioStream(File file) throws IOException {
		this(new RandomAccessStream(file));
	}

	/**
	 * Creates a new {@link RandomAccessAudioStream} from an input stream.
	 * The stream is assumed to be unencoded (raw PCM or WAV).
	 *
	 * @param inputStream The input stream.
	 *
	 * @throws IOException If the stream cannot be read.
	 */
	public RandomAccessAudioStream(DynamicInputStream inputStream) throws IOException {
		this(inputStream, false);
	}

	/**
	 * Creates a new {@link RandomAccessAudioStream} from an input stream.
	 *
	 * @param inputStream The input stream.
	 * @param encoded     Whether the stream is encoded.
	 *
	 * @throws IOException If the stream cannot be read.
	 */
	public RandomAccessAudioStream(DynamicInputStream inputStream, boolean encoded) throws IOException {
		super(encoded ? inputStream : wrapAsAudioStream(inputStream));

		this.sourceStream = inputStream;
		this.encoded = encoded;

		// Extract audio format from AudioInputStream if available
		if (stream instanceof AudioInputStream audioInputStream) {
			this.audioFormat = AudioUtils.createAudioFormat(audioInputStream.getFormat());
		}

		this.totalLength = stream.available();
		setStreamLength(totalLength);
	}

	/**
	 * Gets the audio format.
	 *
	 * @return The audio format.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	/**
	 * Sets the audio format.
	 *
	 * @param format The new audio format.
	 */
	public void setAudioFormat(AudioFormat format) {
		this.audioFormat = format;
	}

	/**
	 * Gets the total length of the stream after exclusions, in bytes.
	 *
	 * @return The effective stream length in bytes.
	 */
	public synchronized long getLength() {
		return totalLength - getExcludedLength();
	}

	/**
	 * Gets the total length of the stream after exclusions, in milliseconds.
	 *
	 * @return The effective stream length in milliseconds.
	 */
	public long getLengthInMillis() {
		double bytesPerSecond = AudioUtils.getBytesPerSecond(audioFormat);
		return (long) ((getLength() / bytesPerSecond) * 1000);
	}

	/**
	 * Converts a time position in milliseconds to a byte position.
	 *
	 * @param millis The time in milliseconds.
	 *
	 * @return The corresponding byte position.
	 */
	public long millisToBytes(long millis) {
		return AudioUtils.getAudioBytePosition(audioFormat, millis);
	}

	/**
	 * Converts a byte position to a time position in milliseconds.
	 *
	 * @param bytes The byte position.
	 *
	 * @return The corresponding time in milliseconds.
	 */
	public long bytesToMillis(long bytes) {
		double bytesPerSecond = AudioUtils.getBytesPerSecond(audioFormat);
		return (long) ((bytes / bytesPerSecond) * 1000);
	}

	/**
	 * Adds an exclusion by time range in milliseconds.
	 * The interval is converted to byte positions.
	 *
	 * @param startMillis Start time in milliseconds.
	 * @param endMillis   End time in milliseconds.
	 */
	public void addExclusionMillis(long startMillis, long endMillis) {
		long startBytes = millisToBytes(startMillis);
		long endBytes = millisToBytes(endMillis);

		addExclusion(new Interval<>(startBytes, endBytes));
	}

	/**
	 * Adds an exclusion interval in virtual coordinates.
	 * <p>
	 * This method converts the virtual (post-exclusion) coordinates to
	 * physical coordinates before adding the exclusion. Use this when
	 * the user selects a region in the edited/virtual timeline.
	 *
	 * @param virtualStartMillis Virtual start time in milliseconds.
	 * @param virtualEndMillis   Virtual end time in milliseconds.
	 */
	public void addExclusionVirtualMillis(long virtualStartMillis, long virtualEndMillis) {
		long virtualStartBytes = millisToBytes(virtualStartMillis);
		long virtualEndBytes = millisToBytes(virtualEndMillis);

		long physicalStart = virtualToPhysical(virtualStartBytes);
		long physicalEnd = virtualToPhysical(virtualEndBytes);

		addExclusion(new Interval<>(physicalStart, physicalEnd));
	}

	/**
	 * Adds an "exclusive" region, keeping only the specified time range
	 * and excluding everything else.
	 *
	 * @param interval The time interval to keep (in milliseconds).
	 */
	public void addExclusiveMillis(Interval<Long> interval) {
		long startBytes = millisToBytes(interval.getStart());
		long endBytes = millisToBytes(interval.getEnd());

		// Exclude everything before and after the specified range.
		if (startBytes > MIN_BYTE_POSITION) {
			addExclusion(new Interval<>(MIN_BYTE_POSITION, startBytes));
		}
		if (endBytes < totalLength) {
			addExclusion(new Interval<>(endBytes, totalLength));
		}
	}

	/**
	 * Removes an "exclusive" region that was previously added.
	 *
	 * @param interval The time interval that was being kept (in milliseconds).
	 */
	public void removeExclusiveMillis(Interval<Long> interval) {
		long startBytes = millisToBytes(interval.getStart());
		long endBytes = millisToBytes(interval.getEnd());

		// Remove the exclusions before and after.
		removeExclusion(new Interval<>(MIN_BYTE_POSITION, startBytes));
		removeExclusion(new Interval<>(endBytes, totalLength));
	}

	@Override
	public void addExclusion(Interval<Long> interval) {
		if (interval == null || interval.getStart() >= interval.getEnd()) {
			return;
		}

		// Bound the interval to valid stream positions.
		Interval<Long> bounded = boundInterval(interval);
		if (bounded.getStart() >= bounded.getEnd()) {
			return;
		}

		super.addExclusion(bounded);
	}

	/**
	 * Converts a virtual time position to a physical byte position.
	 *
	 * @param virtualMillis Virtual time in milliseconds.
	 *
	 * @return Physical byte position.
	 */
	public long virtualMillisToPhysicalBytes(long virtualMillis) {
		long virtualBytes = millisToBytes(virtualMillis);
		return virtualToPhysical(virtualBytes);
	}

	/**
	 * Converts a physical byte position to a virtual time position.
	 *
	 * @param physicalBytes Physical byte position.
	 *
	 * @return Virtual time in milliseconds, or -1 if in the excluded region.
	 */
	public long physicalBytesToVirtualMillis(long physicalBytes) {
		long virtualBytes = physicalToVirtual(physicalBytes);
		if (virtualBytes < 0) {
			return -1;
		}
		return bytesToMillis(virtualBytes);
	}

	@Override
	public int available() {
		return (int) Math.max(0, getLength() - getVirtualPosition());
	}

	/**
	 * Writes the stream content (respecting exclusions) to a WAV file.
	 *
	 * @param channel The output channel.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	public void write(SeekableByteChannel channel) throws IOException {
		WaveOutputStream outputStream = new WaveOutputStream(channel);
		outputStream.setAudioFormat(audioFormat);

		byte[] buffer = new byte[8192];
		int bytesRead;

		while ((bytesRead = read(buffer)) > 0) {
			outputStream.write(buffer, 0, bytesRead);
		}

		outputStream.close();
	}

	@Override
	public RandomAccessAudioStream clone() {
		try {
			RandomAccessAudioStream clone = new RandomAccessAudioStream(
				sourceStream.clone(), encoded
			);

			// Copy exclusions (deep copy).
			clone.setExclusions(getExclusions());

			// Copy filters (deep copy).
			for (var entry : filters.entrySet()) {
				Interval<Long> iv = entry.getValue();
				clone.setAudioFilter(entry.getKey(), new Interval<>(iv.getStart(), iv.getEnd()));
			}

			return clone;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Bounds an interval to valid stream positions.
	 */
	private Interval<Long> boundInterval(Interval<Long> interval) {
		long start = Math.max(MIN_BYTE_POSITION, interval.getStart());
		long end = Math.min(totalLength, interval.getEnd());

		return new Interval<>(start, end);
	}

	/**
	 * Wraps an input stream as an AudioInputStream.
	 */
	private static AudioInputStream wrapAsAudioStream(DynamicInputStream inputStream) {
		try {
			return AudioSystem.getAudioInputStream(inputStream);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to create audio stream", e);
		}
	}
}
