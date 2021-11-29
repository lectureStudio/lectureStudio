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

package org.lecturestudio.core.audio.source;

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;

/**
 * Random access stream implementation of {@link AudioSource}.
 *
 * @author Alex Andres
 */
public class RandomAccessAudioSource implements AudioSource {

	/** The random access audio stream. */
	private final RandomAccessAudioStream stream;


	/**
	 * Create a {@link RandomAccessAudioSource} with the specified random access audio
	 * stream.
	 *
	 * @param stream The random access audio stream.
	 */
	public RandomAccessAudioSource(RandomAccessAudioStream stream) {
		this.stream = stream;
	}

	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}

	@Override
	public int seekMs(int timeMs) throws IOException {
		float bytesPerSecond = AudioUtils.getBytesPerSecond(getAudioFormat());
		int skipBytes = Math.round(bytesPerSecond * timeMs / 1000F);

		reset();
		skip(skipBytes);

		return skipBytes;
	}

	@Override
	public void reset() throws IOException {
		stream.reset();
	}

	@Override
	public int read(byte[] data, int offset, int length) throws IOException {
		return stream.read(data, offset, length);
	}

	@Override
	public long getInputSize() {
		return stream.getLength();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return stream.getAudioFormat();
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	public void addExclusiveMillis(Interval<Long> interval) {
		stream.addExclusiveMillis(interval);
	}

	public void removeExclusiveMillis(Interval<Long> interval) {
		stream.removeExclusiveMillis(interval);
	}
}
