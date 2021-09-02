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
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * WAV header implementation.
 *
 * @author Alex Andres
 */
public class WaveHeader {

	private static final Map<Integer, String> formatTags;
	private static final Map<String, String> infoDescription;

	static
	{
		formatTags = new HashMap<>();
		formatTags.put(1, "PCM");
		formatTags.put(3, "IEEE float");
		formatTags.put(6, "8-bit ITU-T G.711 A-law");
		formatTags.put(7, "8-bit ITU-T G.711 µ-law");
		formatTags.put(65534, "Extensible");
		
		infoDescription = new HashMap<>();
		infoDescription.put("IARL", "Archival Location");
		infoDescription.put("IART", "Artist");
		infoDescription.put("ICMS", "Commissioned");
		infoDescription.put("ICMT", "Comments");
		infoDescription.put("ICOP", "Copyright");
		infoDescription.put("ICRD", "Creation date");
		infoDescription.put("ICRP", "Cropped");
		infoDescription.put("IDIM", "Dimensions");
		infoDescription.put("IDPI", "Dots Per Inch");
		infoDescription.put("IENG", "Engineer");
		infoDescription.put("IGNR", "Genre");
		infoDescription.put("IKEY", "Keywords");
		infoDescription.put("ILGT", "Lightness");
		infoDescription.put("IMED", "Medium");
		infoDescription.put("INAM", "Name");
		infoDescription.put("IPLT", "Palette Setting");
		infoDescription.put("IPRD", "Product");
		infoDescription.put("ISBJ", "Subject");
		infoDescription.put("ISFT", "Software");
		infoDescription.put("ISHP", "Sharpness");
		infoDescription.put("ISRC", "Source");
		infoDescription.put("ISRF", "Source Form");
		infoDescription.put("ITCH", "Technician");
	}

	private static final byte[] riff = { 'R', 'I', 'F', 'F' };
	private static final byte[] wave = { 'W', 'A', 'V', 'E' };
	private static final byte[] fmt  = { 'f', 'm', 't', ' ' };
	private static final byte[] fact = { 'f', 'a', 'c', 't' };
	private static final byte[] data = { 'd', 'a', 't', 'a' };
	private static final byte[] list = { 'L', 'I', 'S', 'T' };
	private static final byte[] info = { 'I', 'N', 'F', 'O' };
    
	private final Map<String, String> infoMap = new HashMap<>();
	
	private final ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
	
	
	
	private int formatTag;
	
	private int channels;
	
	private int samplesPerSec;
	
	private int avgBytesPerSec;
	
	private int blockAlign;
	
	private int bitsPerSample;
	
	private int audioDataSize;
	
	private int inputSize;

	/**
	 * Create a new instance of {@link WaveHeader} with the data of the specified input stream.
	 *
	 * @param inputStream The input stream.
	 */
	public WaveHeader(InputStream inputStream) throws IOException {
		parse(inputStream);
	}

	/**
	 * Get the audio format depending on {@link #bitsPerSample}.
	 *
	 * @return The audio format.
	 */
	public AudioFormat getAudioFormat() {
		AudioFormat.Encoding encoding = null;

		if (bitsPerSample == 16) {
			encoding = AudioFormat.Encoding.S16LE;
		}
		else if (bitsPerSample == 24) {
			encoding = AudioFormat.Encoding.S24LE;
		}
		else if (bitsPerSample == 32) {
			if (formatTag == 1) {
				encoding = AudioFormat.Encoding.S32LE;
			}
			else if (formatTag == 3) {
				encoding = AudioFormat.Encoding.FLOAT32LE;
			}
		}

		return new AudioFormat(encoding, samplesPerSec, channels);
	}

	/**
	 * Get the data length.
	 *
	 * @return The audio data size.
	 */
	public int getDataLength() {
		return audioDataSize;
	}

	/**
	 * Get the header length.
	 *
	 * @return The header length.
	 */
	public int getHeaderLength() {
		return inputSize - audioDataSize;
	}
	
	private void parse(InputStream stream) throws IOException {
		byte[] chunkId = new byte[4];
		
		stream.read(chunkId);
		
		if (!Arrays.equals(riff, chunkId)) {
			throw new IOException("Missing 'RIFF' chunk.");
		}
		
		inputSize = readInt(stream) + 8;
		
		// Read the WAVE ID.
		stream.read(chunkId);
		
		if (!Arrays.equals(wave, chunkId)) {
			throw new IOException("Invalid format. Expected 'WAVE' stream.");
		}
		
		// Read fmt chunk.
		stream.read(chunkId);

		if (!Arrays.equals(fmt, chunkId)) {
			throw new IOException("Missing 'RIFF' chunk.");
		}
		
		int fmtChunkSize = readInt(stream);
		
		formatTag = readShort(stream);
		channels = readShort(stream);
		samplesPerSec = readInt(stream);
		avgBytesPerSec = readInt(stream);
		blockAlign = readShort(stream);
		bitsPerSample = readShort(stream);
		
		// Check for Non-PCM data.
		if (fmtChunkSize == 18) {
			// Size of the extension: 0.
			int extSize = readShort(stream);

			readFactChunk(stream);
		}
		
		// Check for Extensible Format.
		if (fmtChunkSize == 40) {
			// Size of the extension: 22.
			int extSize = readShort(stream);
			System.out.println(extSize);

			int validBitsPerSample = readShort(stream);
			System.out.println(validBitsPerSample);
			
			int channelMask = readInt(stream);
			System.out.println(channelMask);
			
			// GUID (first two bytes are the data format code).
			byte[] subFormat = new byte[16];
			stream.read(subFormat);
			
			readFactChunk(stream);
		}
		
		boolean hasOtherData = false;
		
		// 
		stream.read(chunkId);
		int subChunkSize = readInt(stream);
		
		if (Arrays.equals(list, chunkId)) {
			readListChunk(stream, subChunkSize);
			hasOtherData = true;
		}
		
		// data
		if (hasOtherData) {
			stream.read(chunkId);
			
			if (!Arrays.equals(data, chunkId)) {
				throw new IOException("Missing 'data' chunk.");
			}
			
			audioDataSize = readInt(stream);
		}
		else {
			if (!Arrays.equals(data, chunkId)) {
				throw new IOException("Missing 'data' chunk.");
			}
			
			audioDataSize = subChunkSize;
		}
		
		stream.reset();
	}
	
	private void readListChunk(InputStream stream, int chunkSize) throws IOException {
		byte[] listType = new byte[4];

		chunkSize -= stream.read(listType);

		if (Arrays.equals(info, listType)) {
			// Read info tags.
			while (chunkSize > 0) {
				byte[] infoId = new byte[4];

				chunkSize -= stream.read(infoId);
				int infoSize = readInt(stream);

				chunkSize -= 4;

				byte[] infoData = new byte[infoSize];
				chunkSize -= stream.read(infoData);

				infoMap.put(new String(infoId), new String(infoData).trim());
			}
		}
		else {
			// Skip invalid data.
			
			System.out.println("Skipping unknown chunk: " + new String(listType));
			
			stream.skip(chunkSize);
		}
	}
	
	private void readFactChunk(InputStream stream) throws IOException {
		byte[] chunkId = new byte[4];
		
		stream.read(chunkId);
		
		int funcChunkSize = readInt(stream);
		
		// Check for "fact" chunk.
		if (!Arrays.equals(fact, chunkId)) {
			throw new IOException("Missing 'fact' chunk.");
		}
		
		int sampleLength = readInt(stream);
	}
	
	private short readShort(InputStream stream) throws IOException {
		stream.read(buffer.array(), 0, 2);
		buffer.clear();
		
		return buffer.getShort();
	}
	
	private int readInt(InputStream stream) throws IOException {
		stream.read(buffer.array(), 0, 4);
		buffer.clear();
		
		return buffer.getInt();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[RIFF WAVE]");
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Format Tag", formatTags.get(formatTag)));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Channels", channels));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Samples Per Sec", samplesPerSec));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Avg Bytes Per Sec", avgBytesPerSec));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Block Align", blockAlign));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Bits Per Sample", bitsPerSample));
		sb.append("\n");

		if (!infoMap.isEmpty()) {
			sb.append(String.format("%20s:", "Info"));
			sb.append("\n");

			for (String infoKey : infoMap.keySet()) {
				String infoValue = infoMap.get(infoKey);

				sb.append(String.format("%20s:%15s", infoDescription.get(infoKey), infoValue));
				sb.append("\n");
			}
		}
		
		sb.append(String.format("%20s:%15s", "Data Size", audioDataSize));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Input Size", inputSize));
		sb.append("\n");
		sb.append(String.format("%20s:%15s", "Header Size", (inputSize - audioDataSize)));
		
		return sb.toString();
	}
	
}
