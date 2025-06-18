/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.media.video;

import static java.util.Objects.isNull;
import static org.bytedeco.ffmpeg.global.swscale.SWS_SPLINE;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.media.config.AudioRenderConfiguration;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;

/**
 * FFmpegMuxer provides functionality to mux audio and video streams into a single media file.
 * This implementation uses FFmpeg (via JavaCV) to handle the encoding and multiplexing process.
 * The class supports various audio and video codecs, and can be configured with different
 * quality settings through a {@link RenderConfiguration} object.
 * <p>
 * The muxer follows the execution lifecycle defined by {@link ExecutableBase}, with proper
 * initialization, starting, stopping, and resource cleanup methods.
 *
 * @author Alex Andres
 */
public class FFmpegMuxer extends ExecutableBase implements VideoMuxer {

	/** The render configuration containing audio and video settings used to configure the FFmpeg recorder. */
	private final RenderConfiguration config;

	/** Converter used to transform Java BufferedImage objects into FFmpeg frames. */
	private final Java2DFrameConverter frameConverter = new Java2DFrameConverter();

	/** The FFmpeg recorder responsible for encoding and writing video and audio data to the output file. */
	private FFmpegFrameRecorder recorder;


	/**
	 * Constructs a new FFmpegMuxer with the specified render configuration.
	 * The configuration will be used to set up the FFmpeg recorder with appropriate
	 * audio and video encoding settings.
	 *
	 * @param renderConfig The configuration containing audio and video settings for the muxer.
	 */
	public FFmpegMuxer(RenderConfiguration renderConfig) {
		config = renderConfig;
	}

	@Override
	public void addVideoFrame(BufferedImage image) throws IOException {
		Frame frame = frameConverter.convert(image);
		recorder.record(frame, avutil.AV_PIX_FMT_ARGB);
	}

	@Override
	public void addAudioFrame(byte[] samples, int offset, int length) throws IOException {
		int nSamplesRead = length / 2;
		short[] shortSamples = new short[nSamplesRead];

		ByteBuffer.wrap(samples, offset, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortSamples);
		ShortBuffer samplesBuffer = ShortBuffer.wrap(shortSamples, 0, nSamplesRead);

		recorder.recordSamples(samplesBuffer);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(config)) {
			throw new ExecutableException("Render configuration must not be null.");
		}

		FFmpegLogCallback.setLevel(avutil.AV_LOG_ERROR);
		FFmpegLogCallback.set();

		AudioRenderConfiguration audioConfig = config.getAudioConfig();
		VideoRenderConfiguration videoConfig = config.getVideoConfig();

		try {
			recorder = FFmpegFrameRecorder.createDefault(config.getOutputFile(),
					(int) videoConfig.getDimension().getWidth(),
					(int) videoConfig.getDimension().getHeight());
			recorder.setFormat(config.getFileFormat());
			recorder.setOption("fps_mode", "cfr");
			// Video settings
			recorder.setFrameRate(videoConfig.getFrameRate());
			recorder.setVideoBitrate(videoConfig.getBitrate() * 1000);
			recorder.setVideoCodec(getVideoCodecId(videoConfig));
			recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
			recorder.setImageScalingFlags(SWS_SPLINE);
			recorder.setVideoOption("crf", "15");
			recorder.setVideoOption("preset", "medium");
			recorder.setVideoOption("v:profile", "high");
			recorder.setVideoOption("level", "4.1");
			recorder.setVideoOption("tune", "film");
			// Audio settings
			recorder.setAudioBitrate(audioConfig.getBitrate() * 1000);
			recorder.setAudioCodec(getAudioCodecId(audioConfig));
			recorder.setAudioChannels(audioConfig.getOutputFormat().getChannels());
			recorder.setSampleRate(audioConfig.getOutputFormat().getSampleRate());
		}
		catch (FFmpegFrameRecorder.Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			recorder.start();
		}
		catch (FFmpegFrameRecorder.Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			recorder.stop();
		}
		catch (FFmpegFrameRecorder.Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			recorder.release();
		}
		catch (FFmpegFrameRecorder.Exception e) {
			throw new ExecutableException(e);
		}
	}

	/**
	 * Converts the audio codec ID from the render configuration to the corresponding
	 * FFmpeg codec ID constant from avcodec.
	 *
	 * @param config The audio render configuration containing the codec ID to convert.
	 *
	 * @return The FFmpeg codec ID as an integer constant from avcodec.
	 *
	 * @throws IllegalArgumentException If the provided codec ID is not supported.
	 */
	private static int getAudioCodecId(AudioRenderConfiguration config) {
		return switch (config.getCodecID()) {
			case AAC -> avcodec.AV_CODEC_ID_AAC;
			case MP3 -> avcodec.AV_CODEC_ID_MP3;
			case OPUS -> avcodec.AV_CODEC_ID_OPUS;
			case VORBIS -> avcodec.AV_CODEC_ID_VORBIS;
			case WAV -> avcodec.AV_CODEC_ID_PCM_S16LE;
			default -> throw new IllegalArgumentException("Unsupported audio codec id.");
		};
	}

	/**
	 * Converts the video codec ID from the render configuration to the corresponding
	 * FFmpeg codec ID constant from avcodec.
	 *
	 * @param config The video render configuration containing the codec ID to convert.
	 *
	 * @return The FFmpeg codec ID as an integer constant from avcodec.
	 *
	 * @throws IllegalArgumentException If the provided codec ID is not supported.
	 */
	private static int getVideoCodecId(VideoRenderConfiguration config) {
		return switch (config.getCodecID()) {
			case AV1 -> avcodec.AV_CODEC_ID_AV1;
			case H264 -> avcodec.AV_CODEC_ID_H264;
			case H265 -> avcodec.AV_CODEC_ID_H265;
			default -> throw new IllegalArgumentException("Unsupported video codec id.");
		};
	}
}
