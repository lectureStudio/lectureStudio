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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RandomAccessStream extends DynamicInputStream {

	/** Logger for {@link RandomAccessStream}. */
	private static final Logger LOG = LogManager.getLogger(RandomAccessStream.class);

	/** The source file. */
	private final File sourceFile;

	/** The start pointer. */
	private final long startPointer;

	/** The length of the {@link RandomAccessStream} */
	private final long length;

	/**
	 * Create a new instance of {@link RandomAccessStream} with the specified source file.
	 * (Calls {@link #RandomAccessStream(File, long, long)} with the specified file, {@code 0} as start pointer and
	 * the length of the specified file as length.)
	 *
	 * @param file The source file.
	 */
	public RandomAccessStream(File file) throws IOException {
		this(file, 0, file.length());
	}

	/**
	 * Create a new instance of {@link RandomAccessStream} with the specified source file, start pointer and length.
	 *
	 * @param file The source file.
	 * @param startPointer The start pointer.
	 * @param length The length of the {@link RandomAccessStream}
	 */
	public RandomAccessStream(File file, long startPointer, long length) throws IOException {
		super(new SeekableChannelInputStream(
				Files.newByteChannel(Paths.get(file.getPath()),
						StandardOpenOption.READ).position(startPointer)));

		this.sourceFile = file;
		this.startPointer = startPointer;
		this.length = length;
	}

	@Override
	public int available() {
		// Take future exclusion into account.
		long toExclude = getToExcludeLength();

		return (int) (length - toExclude);
	}

	@Override
	public RandomAccessStream clone() {
		RandomAccessStream stream = null;

		try {
			stream = new RandomAccessStream(sourceFile, startPointer, length);
		}
		catch (IOException e) {
			LOG.error("Clone stream failed", e);
		}

		return stream;
	}

	@Override
	public synchronized void reset() throws IOException {
		super.reset();

		stream.skip(startPointer);
	}
}
