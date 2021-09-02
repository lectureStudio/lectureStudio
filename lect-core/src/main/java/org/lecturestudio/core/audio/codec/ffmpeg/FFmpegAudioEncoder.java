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

package org.lecturestudio.core.audio.codec.ffmpeg;

import com.github.javaffmpeg.Audio;
import com.github.javaffmpeg.AudioFrame;
import com.github.javaffmpeg.Codec;
import com.github.javaffmpeg.CodecID;
import com.github.javaffmpeg.Encoder;
import com.github.javaffmpeg.JavaFFmpegException;
import com.github.javaffmpeg.MediaPacket;
import com.github.javaffmpeg.MediaType;
import com.github.javaffmpeg.SampleFormat;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.codec.AudioEncoder;

/**
 * FFmpeg audio encoder implementation.
 *
 * @link https://ffmpeg.org
 *
 * @author Alex Andres
 */
public class FFmpegAudioEncoder extends AudioEncoder {

	/** An array of supported audio formats. */
	private AudioFormat[] supportedFormats;

	/** The internal FFmpeg encoder. */
	private Encoder encoder;

	/** The internal encoding format. */
	private com.github.javaffmpeg.AudioFormat format;

	/** The sample size in bytes. */
	private int sampleSize;


	/**
	 * Create a {@link FFmpegAudioEncoder} with the specified codec ID. Based on the ID
	 * the corresponding FFmpeg encoder will be created.
	 *
	 * @param codecId The ID of the codec to use.
	 */
	public FFmpegAudioEncoder(CodecID codecId) {
		try {
			encoder = new Encoder(Codec.getEncoderById(codecId));
			encoder.setMediaType(MediaType.AUDIO);

			setBitrate(128000);

			getInputFormats();
		}
		catch (JavaFFmpegException e) {
			e.printStackTrace();
		}
	}

	@Override
	public AudioFormat[] getSupportedFormats() {
		return supportedFormats;
	}

	@Override
	public void process(byte[] input, int length, long timestamp) throws Exception {
		int samples = input.length / sampleSize;

		AudioFrame frame = new AudioFrame(format, samples);

		// mono stream
		if (format.getChannels() == 1) {
			frame.getPlane(0).asByteBuffer().put(input);
		}
		else {
			throw new Exception("Frame input only for mono audio implemented.");
		}

		MediaPacket[] packets = encoder.encodeAudio(frame);

		if (packets != null) {
			for (MediaPacket packet : packets) {
				if (packet == null) {
					continue;
				}

				byte[] outputData = new byte[packet.getData().limit()];
				packet.getData().get(outputData);

				fireAudioEncoded(outputData, outputData.length, timestamp);

				packet.clear();
			}
		}

		frame.clear();
	}

	@Override
	protected void initInternal() throws ExecutableException {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		AudioFormat inputFormat = getFormat();

		int sampleRate = inputFormat.getSampleRate();
		int channels = inputFormat.getChannels();

		try {
			encoder.setMediaType(MediaType.AUDIO);
			encoder.setBitrate(getBitrate());
			encoder.setSampleRate(sampleRate);
			encoder.setSampleFormat(SampleFormat.S16);
			encoder.setAudioChannels(channels);
			encoder.setQuality(0);    // Quality-based encoding not supported.
			encoder.open(null);
		}
		catch (JavaFFmpegException e) {
			throw new ExecutableException(e);
		}

		format = new com.github.javaffmpeg.AudioFormat();
		format.setChannelLayout(Audio.getChannelLayout(channels));
		format.setChannels(channels);
		format.setSampleFormat(SampleFormat.S16);
		format.setSampleRate(sampleRate);

		sampleSize = 2 * format.getChannels();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		encoder.close();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	/**
	 * Assigns all supported audio formats to {@link #supportedFormats}.
	 */
	private void getInputFormats() {
		if (encoder == null) {
			return;
		}

		Integer[] sampleRates = encoder.getCodec().getSupportedSampleRates();

		if (sampleRates == null) {
			return;
		}

		supportedFormats = new AudioFormat[sampleRates.length];

		AudioFormat.Encoding encoding = AudioFormat.Encoding.S16LE;
		int channels = 1;

		for (int i = 0; i < sampleRates.length; i++) {
			int sampleRate = sampleRates[i];

			AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, channels);

			supportedFormats[i] = audioFormat;
		}
	}

}
