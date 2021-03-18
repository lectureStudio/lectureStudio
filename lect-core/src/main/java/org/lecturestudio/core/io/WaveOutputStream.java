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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import org.lecturestudio.core.audio.AudioFormat;

public class WaveOutputStream {

	private static final String RIFF_HEADER = "RIFF";
	private static final String WAVE_HEADER = "WAVE";
	private static final String FMT_HEADER  = "fmt ";
	private static final String DATA_HEADER = "data";

	private final SeekableByteChannel channel;

	private AudioFormat audioFormat;


	public WaveOutputStream(SeekableByteChannel channel) throws IOException {
		this.channel = channel;
		
		reset();
	}

	public void setAudioFormat(AudioFormat format) throws IOException {
		this.audioFormat = format;
		
		// Write header provisionally.
		writeHeader();
		reset();
	}

	public int write(byte[] data, int offset, int length) throws IOException {
		if (channel.isOpen())
			return channel.write(ByteBuffer.wrap(data, offset, length));
		
		return 0;
	}

	public void reset() throws IOException {
		channel.position(44);
	}

	public void close() throws IOException {
		writeHeader();
		
		channel.close();
	}

	private void writeHeader() throws IOException {
		// Assume audio length does not exceeds 2^32 bytes.
		int chunkSize = (int) (36 + channel.size());
		int subchunk1Size = 16; // 16 for PCM
		short audioFormat = 1; // PCM = 1
		short numChannels = (short) this.audioFormat.getChannels();
		int sampleRate = (int) this.audioFormat.getSampleRate();
		int byteRate = sampleRate * numChannels * this.audioFormat.getBytesPerSample();
		short blockAlign = (short) (numChannels * this.audioFormat.getBytesPerSample());
		short bitsPerSample = (short) this.audioFormat.getBitsPerSample();
		int subchunk2Size = (int) channel.size();

		ByteBuffer header = ByteBuffer.allocate(44);
		// RIFF chunk
		header.put(RIFF_HEADER.getBytes());
		header.put(BitConverter.getLittleEndianBytes(chunkSize));
		header.put(WAVE_HEADER.getBytes());
		// fmt chunk
		header.put(FMT_HEADER.getBytes());
		header.put(BitConverter.getLittleEndianBytes(subchunk1Size));
		header.put(BitConverter.getLittleEndianBytes(audioFormat));
		header.put(BitConverter.getLittleEndianBytes(numChannels));
		header.put(BitConverter.getLittleEndianBytes(sampleRate));
		header.put(BitConverter.getLittleEndianBytes(byteRate));
		header.put(BitConverter.getLittleEndianBytes(blockAlign));
		header.put(BitConverter.getLittleEndianBytes(bitsPerSample));
		// data chunk
		header.put(DATA_HEADER.getBytes());
		header.put(BitConverter.getLittleEndianBytes(subchunk2Size));
		header.clear();

		channel.position(0);
		channel.write(header);
	}

}
