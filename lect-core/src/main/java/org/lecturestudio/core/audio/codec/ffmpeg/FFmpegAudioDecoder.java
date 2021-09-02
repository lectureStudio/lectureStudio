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
import com.github.javaffmpeg.AudioResampler;
import com.github.javaffmpeg.Codec;
import com.github.javaffmpeg.CodecID;
import com.github.javaffmpeg.Decoder;
import com.github.javaffmpeg.JavaFFmpegException;
import com.github.javaffmpeg.MediaPacket;
import com.github.javaffmpeg.MediaType;
import com.github.javaffmpeg.SampleFormat;

import java.nio.ByteBuffer;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.codec.AudioDecoder;

import org.bytedeco.javacpp.BytePointer;

/**
 * FFmpeg audio decoder implementation.
 *
 * @link https://ffmpeg.org
 *
 * @author Alex Andres
 */
public class FFmpegAudioDecoder extends AudioDecoder {

	/** The sample size in bytes. */
	private static final int SAMPLE_SIZE = 2;

	/** The internal FFmpeg decoder. */
	private Decoder decoder;

	/** The internal audio resampler */
	private com.github.javaffmpeg.AudioResampler resampler;


	/**
	 * Create a FFmpegAudioDecoder with the specified codec ID. Based on the ID
	 * the corresponding FFmpeg decoder will be created.
	 *
	 * @param codecID The ID of the codec to use.
	 */
	public FFmpegAudioDecoder(CodecID codecID) {
		try {
			decoder = new Decoder(Codec.getDecoderById(codecID));
			decoder.setMediaType(MediaType.AUDIO);
		}
		catch (JavaFFmpegException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(byte[] input, int length, long timestamp) throws Exception {
		ByteBuffer buffer = ByteBuffer.wrap(input, 0, length);
		MediaPacket packet = new MediaPacket(buffer);

		AudioFrame frame = decoder.decodeAudio(packet);

		if (frame != null) {
			if (resampler != null) {
				AudioFrame[] frames = resampler.resample(frame);

				for (AudioFrame resFrame : frames) {
					processAudioFrame(resFrame, timestamp);

					resFrame.clear();
				}
			}
			else {
				processAudioFrame(frame, timestamp);
			}

			frame.clear();
		}

		packet.clear();
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
			decoder.setSampleRate(sampleRate);
			decoder.setSampleFormat(SampleFormat.S16);
			decoder.setAudioChannels(channels);
			decoder.open(null);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		// requested format
		com.github.javaffmpeg.AudioFormat reqFormat = new com.github.javaffmpeg.AudioFormat();
		reqFormat.setChannelLayout(Audio.getChannelLayout(channels));
		reqFormat.setChannels(channels);
		reqFormat.setSampleFormat(SampleFormat.S16);
		reqFormat.setSampleRate(sampleRate);

		// decoder format
		com.github.javaffmpeg.AudioFormat decFormat = new com.github.javaffmpeg.AudioFormat();
		decFormat.setChannelLayout(decoder.getChannelLayout());
		decFormat.setChannels(decoder.getAudioChannels());
		decFormat.setSampleFormat(decoder.getSampleFormat());
		decFormat.setSampleRate(decoder.getSampleRate());

		// in some cases the decoder chooses its own parameters, e.g. OPUS
		if (!reqFormat.equals(decFormat)) {
			int samples = sampleRate / 50;    // 20 ms audio
			resampler = new AudioResampler();

			try {
				resampler.open(decFormat, reqFormat, samples);
			}
			catch (Exception e) {
				throw new ExecutableException(e);
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		decoder.close();
		resampler.close();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private void processAudioFrame(AudioFrame frame, long timestamp) {
		int planes = frame.getPlaneCount();
		int size = planes * frame.getPlane(0).limit();
		byte[] samples = new byte[size];

		// interleave planes
		for (int i = 0; i < planes; i++) {
			BytePointer plane = frame.getPlane(i);
			ByteBuffer pBuffer = plane.asByteBuffer();
			int pLength = plane.limit();
			int offset = i * planes;

			for (int j = 0, k = offset; j < pLength; j += SAMPLE_SIZE) {
				samples[k++] = (byte) (pBuffer.get() & 0xFF);
				samples[k++] = (byte) (pBuffer.get() & 0xFF);

				k += SAMPLE_SIZE * (planes - 1);
			}
		}

		fireAudioDecoded(samples, samples.length, timestamp);
	}

}
