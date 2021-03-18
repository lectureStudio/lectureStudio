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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public class SeekableChannelInputStream extends InputStream {

	private final SeekableByteChannel channel;

	private ByteBuffer bb;

	private byte[] bs;

	private byte[] b1;


	public SeekableChannelInputStream(SeekableByteChannel channel) {
		this.channel = channel;
	}

	@Override
	public synchronized int read() throws IOException {
		if (b1 == null) {
			b1 = new byte[1];
		}

		int n = this.read(b1);
		if (n == 1) {
			return b1[0] & 0xff;
		}
		return -1;
	}

	@Override
	public synchronized int read(byte[] bs, int off, int len)
			throws IOException {
		Objects.checkFromIndexSize(off, len, bs.length);

		if (len == 0) {
			return 0;
		}

		ByteBuffer bb = ((this.bs == bs) ? this.bb : ByteBuffer.wrap(bs));
		bb.limit(Math.min(off + len, bb.capacity()));
		bb.position(off);

		this.bb = bb;
		this.bs = bs;

		return read(bb);
	}

	@Override
	public int available() throws IOException {
		long rem = Math.max(0, channel.size() - channel.position());

		return (rem > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) rem;
	}

	@Override
	public synchronized long skip(long n) throws IOException {
		long pos = channel.position();
		long newPos;

		if (n > 0) {
			newPos = pos + n;
			long size = channel.size();
			if (newPos < 0 || newPos > size) {
				newPos = size;
			}
		}
		else {
			newPos = Long.max(pos + n, 0);
		}

		channel.position(newPos);

		return newPos - pos;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public synchronized void reset() throws IOException {
		channel.position(0);
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	protected int read(ByteBuffer bb) throws IOException {
		return channel.read(bb);
	}
}
