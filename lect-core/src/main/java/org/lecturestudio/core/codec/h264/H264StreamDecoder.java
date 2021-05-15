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

package org.lecturestudio.core.codec.h264;

import com.github.javaffmpeg.Codec;
import com.github.javaffmpeg.CodecID;
import com.github.javaffmpeg.Decoder;
import com.github.javaffmpeg.Image;
import com.github.javaffmpeg.JavaFFmpegException;
import com.github.javaffmpeg.MediaFrame;
import com.github.javaffmpeg.MediaPacket;
import com.github.javaffmpeg.VideoFrame;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lecturestudio.core.codec.VideoDecoder;
import org.lecturestudio.core.codec.VideoDecoderCallback;

/**
 * H.264 video decoder implementation.
 *
 * @author Alex Andres
 */
public class H264StreamDecoder implements VideoDecoder {

	/** The decoded frame sink. */
	private final VideoDecoderCallback callback;

	/** Internal FFmpeg video decoder. */
	private Decoder videoDecoder;

	/** Number of frames decoded. */
	private long framesReceived = 0;

	/** Number of bytes decoded. */
	private long bytesReceived = 0;

	/** The start time of decoding. */
	private long start = 0;


	/**
	 * Create a {@link H264StreamDecoder} with the specified callback that receives the
	 * decoded video frames.
	 *
	 * @param callback The decoded frame sink.
	 */
	public H264StreamDecoder(VideoDecoderCallback callback) {
		this.callback = callback;

		initialize();
	}

	@Override
	public void decode(ByteBuffer data) {
		if (data == null) {
			return;
		}
		if (start < 1) {
			start = System.currentTimeMillis();
		}

		bytesReceived += data.limit();

		try {
			MediaPacket mediaPacket = new MediaPacket(data);
			VideoFrame mediaFrame = videoDecoder.decodeVideo(mediaPacket);

			if (mediaFrame.hasFrame()) {
				if (mediaFrame.getType() == MediaFrame.Type.VIDEO) {
					BufferedImage image = Image.createImage(mediaFrame.getData(), mediaFrame.getWidth(),
							mediaFrame.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

					framesReceived++;

					callback.frameDecoded(image);
				}
			}

			mediaPacket.clear();
		}
		catch (JavaFFmpegException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float getFPS() {
		return framesReceived / getSeconds();
	}

	@Override
	public float getBitrate() {
		return bytesReceived * 8.f / 1000.f / getSeconds();
	}

	@Override
	public long getTotalBytesReceived() {
		return bytesReceived;
	}

	/**
	 * Release assigned resources by the video decoder.
	 */
	public void dispose() {
		videoDecoder.close();
	}

	private float getSeconds() {
		return (System.currentTimeMillis() - start) / 1000.f;
	}

	private void initialize() {
		try {
			videoDecoder = new Decoder(Codec.getDecoderById(CodecID.H264));
			videoDecoder.open(null);
		}
		catch (JavaFFmpegException e) {
			e.printStackTrace();
		}
	}

}
