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

package org.lecturestudio.media.audio.opus;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;
import org.gagravarr.opus.OpusAudioData;

public class OpusStreamReader extends InputStream {

	private static final int DEFAULT_INITIAL_BUFFER_SIZE = 8192;

	private static final int BUFFER_SIZE = 1024 * 1024;

	private final byte[] decodeBuffer = new byte[BUFFER_SIZE];

	private final byte[] one = new byte[1];

	private final OpusDecoder decoder;

	private final OpusStream in;

	private final int channels;

	private byte[] buffer;

	private int index;
	private int limit;
	private int capacity;

	private boolean closed;
	private boolean eos;


	public OpusStreamReader(OpusStream stream, AudioFormat format) throws IOException {
		this(stream, format, DEFAULT_INITIAL_BUFFER_SIZE);
	}

	public OpusStreamReader(OpusStream stream, AudioFormat format, int initialBufferSize) throws IOException {
		try {
			decoder = new OpusDecoder((int) format.getSampleRate(), format.getChannels());
		}
		catch (Exception e) {
			throw new IOException(e);
		}

		in = stream;
		capacity = initialBufferSize;
		buffer = new byte[capacity];
		channels = format.getChannels();
	}

	@Override
	public void reset() {

	}

	@Override
	public int read() throws IOException {
		int amount = read(one, 0, 1);
		return (amount < 0) ? -1 : one[0] & 0xff;
	}

	@Override
	public int read(byte[] data, int offset, int length) throws IOException {
		if (data == null) {
			throw new NullPointerException();
		}
		else if ((offset < 0) || (offset + length > data.length) || (length < 0)) {
			throw new IndexOutOfBoundsException();
		}
		else if (closed) {
			throw new IOException("Stream closed");
		}

		while (index >= limit) {
			if (eos) {
				return -1;
			}
			readAudioPacket();
		}

		if (limit - index < length) {
			length = limit - index;
		}

		System.arraycopy(buffer, index, data, offset, length);

		index += length;

		return length;
	}

	@Override
	public long skip(long amount) throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}
		else if (amount <= 0) {
			return 0;
		}

		while (index >= limit) {
			if (eos) {
				return 0;
			}
			readAudioPacket();
		}

		if (limit - index < amount) {
			amount = limit - index;
		}

		index += (int) amount;

		return amount;
	}

	@Override
	public int available() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}

		return limit - index;
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			in.close();
		}
	}

	private void setEOS() {
		eos = true;
	}

	private void writeBuffer(byte[] data, int length) {
		if (data == null) {
			throw new NullPointerException();
		}
		else if (length > data.length || length < 0) {
			throw new IndexOutOfBoundsException();
		}

		if (index >= limit) {
			index = limit = 0;
		}
		if (limit + length > capacity) {
			capacity = capacity * 2 + length;
			byte[] tmp = new byte[capacity];

			System.arraycopy(buffer, index, tmp, 0, limit - index);

			buffer = tmp;
			limit -= index;
			index = 0;
		}

		System.arraycopy(data, 0, buffer, limit, length);

		limit += length;
	}

	private void readAudioPacket() throws IOException {
		OpusAudioData nextPacket = in.getNextAudioPacket();

		if (nextPacket != null) {
			try {
				byte[] packetData = nextPacket.getData();

				int decodedSamples = decoder.decode(packetData, 0,
						packetData.length, decodeBuffer, 0, BUFFER_SIZE,
						false);

				if (decodedSamples < 0) {
					setEOS();
					throw new IOException("Decode error");
				}

				writeBuffer(decodeBuffer, decodedSamples * channels * 2); // 2 bytes per sample
			}
			catch (OpusException e) {
				setEOS();
				throw new IOException(e);
			}
		}
		else {
			setEOS();
		}
	}
}
