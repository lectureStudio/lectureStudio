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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.AudioFileWriter;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;
import org.concentus.OpusSignal;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusTags;

/**
 * Opus audio file reading implementation. This reader can parse the format
 * information from the Opus audio stream, and can produce audio input streams
 * to be used for playback.
 *
 * @author Alex Andres
 */
public class OpusAudioFileWriter extends AudioFileWriter {

	private final int bitrate;

	private final int complexity;

	private final OpusSignal signalType;


	public OpusAudioFileWriter() {
		this(64000, 8);
	}

	public OpusAudioFileWriter(int bitrate, int complexity) {
		this(bitrate, complexity, OpusSignal.OPUS_SIGNAL_AUTO);
	}

	public OpusAudioFileWriter(int bitrate, int complexity, OpusSignal signalType) {
		this.bitrate = bitrate;
		this.complexity = complexity;
		this.signalType = signalType;
	}

	@Override
	public Type[] getAudioFileTypes() {
		return new Type[] { Type.WAVE };
	}

	@Override
	public Type[] getAudioFileTypes(AudioInputStream stream) {
		int channels = stream.getFormat().getChannels();

		if (!Encoding.PCM_SIGNED.equals(stream.getFormat().getEncoding())
				|| channels > 2 || channels < 1) {
			return new Type[0];
		}

		Encoding encoding = stream.getFormat().getEncoding();

		if (!OpusEncoding.OPUS.equals(encoding)) {
			return new Type[0];
		}

		return new Type[] { Type.WAVE };
	}

	@Override
	public int write(AudioInputStream stream, Type fileType, OutputStream out)
			throws IOException {
		if (!fileType.equals(OpusFileFormatType.OPUS)) {
			throw new IllegalArgumentException("File type " + fileType + " is not supported");
		}

		AudioFormat format = stream.getFormat();
		int sampleRate = (int) format.getSampleRate();
		int channels = format.getChannels();
		OpusEncoder encoder;

		try {
			encoder = new OpusEncoder(sampleRate, channels,
					OpusApplication.OPUS_APPLICATION_AUDIO);
		}
		catch (OpusException e) {
			throw new IOException(e);
		}

		encoder.setBitrate(bitrate);
		encoder.setComplexity(complexity);
		encoder.setSignalType(signalType);

		OpusInfo info = new OpusInfo();
		info.setSampleRate(sampleRate);
		info.setNumChannels(channels);

		OpusTags tags = new OpusTags();

		OpusFile file = new OpusFile(out, info, tags);

		int encoded = 0;
		int packetSamples = 960; // 20ms of audio: 48000 Hz / 1000 * 20ms = 960
		int granulePosition = 0;
		int granuleStep = packetSamples * 48000 / sampleRate;
		short[] pcm = new short[packetSamples * channels];
		byte[] input = new byte[packetSamples * channels * 2];
		byte[] packetBuffer = new byte[1275]; // Maximum possible number of octets

		try {
			while (stream.read(input) > 0) {
				bytesToShorts(input, pcm);

				int bytesEncoded = encoder.encode(pcm, 0, packetSamples, packetBuffer, 0, packetBuffer.length);
				byte[] packet = new byte[bytesEncoded];

				System.arraycopy(packetBuffer, 0, packet, 0, bytesEncoded);

				granulePosition += granuleStep;

				OpusAudioData data = new OpusAudioData(packet);
				data.setGranulePosition(granulePosition);

				file.writeAudioData(data);

				encoded += bytesEncoded;
			}
		}
		catch (OpusException e) {
			throw new IOException(e);
		}

		file.close();

		return encoded;
	}

	@Override
	public int write(AudioInputStream stream, Type fileType, File file)
			throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			return write(stream, fileType, out);
		}
	}

	private static void bytesToShorts(byte[] input, short[] pcm) {
		for (int c = 0, idx = 0; c < pcm.length; idx += 2, c++) {
			pcm[c] = (short) (((input[idx] & 0xff) | (input[idx + 1] << 8)) & 0xffff);
		}
	}
}
