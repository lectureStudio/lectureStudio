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

package org.lecturestudio.media.video;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.codec.CodecID;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.io.StreamGobbler;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.media.config.AudioRenderConfiguration;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;

public class FFmpegProcessMuxer extends ExecutableBase implements VideoMuxer {

	private static final Logger LOG = LogManager.getLogger(FFmpegProcessMuxer.class.getName());

	private final RenderConfiguration config;

	private ByteBuffer imageBuffer;

	private Process process;

	private OutputStream outStream;


	public FFmpegProcessMuxer(RenderConfiguration config) {
		this.config = config;
	}

	@Override
	public void addVideoFrame(BufferedImage image) throws IOException {
		if (outStream == null) {
			throw new IOException("No output stream available.");
		}
		if (!started()) {
			return;
		}

		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		byte[] imageBytes = null;

		if (dataBuffer instanceof DataBufferInt dataBufferInt) {
			int[] a = dataBufferInt.getData();

			if (isNull(imageBuffer) || imageBuffer.capacity() != a.length * 4) {
				imageBuffer = ByteBuffer.allocate(a.length * 4);
			}

			imageBuffer.asIntBuffer().put(a);
			imageBuffer.clear();

			imageBytes = imageBuffer.array();
		}

		if (imageBytes == null) {
			throw new IOException("Buffered image could not be converted.");
		}

		outStream.write(imageBytes);
	}

	@Override
	public void addAudioFrame(byte[] frame, int offset, int length) throws IOException {
		if (outStream == null) {
			throw new IOException("No output stream available.");
		}

		outStream.write(frame, offset, length);
	}

	@Override
	protected void initInternal() {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			createProcess(config);
		}
		catch (IOException e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			// Close the process output stream, otherwise the process remains open.
			if (nonNull(outStream)) {
				outStream.close();
				outStream = null;
			}

			// Wait for the process to finish.
			process.waitFor();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() {

	}

	private void createProcess(RenderConfiguration config) throws IOException {
		AudioRenderConfiguration audioConfig = config.getAudioConfig();
		VideoRenderConfiguration videoConfig = config.getVideoConfig();

		String format = config.getFileFormat();
		File outputFile = config.getOutputFile();
		String outputPath;

		if (outputFile == null) {
			if (OsInfo.isWindows()) {
				outputPath = "NUL";
			}
			else {
				outputPath = "/dev/null";
			}
		}
		else {
			outputPath = outputFile.getAbsolutePath();
		}

		// Load native FFmpeg.
		String platformName = OsInfo.getPlatformName();
		String libraryPath = System.getProperty("java.library.path", "lib/native/" + platformName);
		String ffmpegExec = libraryPath + "/ffmpeg";

		List<String> commands = new ArrayList<>();
		commands.add(ffmpegExec);

		if (audioConfig != null) {
			commands.addAll(getAudioParameters(audioConfig));
		}
		if (videoConfig != null) {
			commands.addAll(getVideoParameters(videoConfig));
		}

		// Use the full horsepower.
		commands.add("-threads");
		commands.add("0");
		commands.add("-f");
		commands.add(format);
		commands.add(outputPath);
		commands.add("-y");

		commands.add("-hide_banner");
		commands.add("-loglevel");
		commands.add("info");
		commands.add("-nostats");

		ProcessBuilder procBuilder = new ProcessBuilder(commands.toArray(new String[0]));
		procBuilder.redirectErrorStream(true);

		process = procBuilder.start();

		Consumer<String> consumer = LOG::info;

		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), consumer);
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), consumer);

		errorGobbler.start();
		outputGobbler.start();

		outStream = process.getOutputStream();
	}

	private List<String> getAudioParameters(AudioRenderConfiguration config) {
		List<String> params = new ArrayList<>();

		AudioFormat audioSrcFormat = config.getInputFormat();
		File inputFile = config.getVideoInputFile();
		String channels = Integer.toString(config.getOutputFormat().getChannels());
		String sampleRate = Float.valueOf(config.getOutputFormat().getSampleRate()).intValue() + "";
		String audioCodec = getAudioCodecName(config.getCodecID());
		//String bitrate = Integer.toString(config.getAudioBitRate() * 1000);

		if (inputFile != null) {
			params.add("-i");
			params.add(inputFile.getAbsolutePath());
		}

		// Set explicitly the audio input format, since the audio header may be missing.
		if (audioSrcFormat != null) {
			String srcChannels = Integer.toString(audioSrcFormat.getChannels());
			String srcSampleRate = Float.valueOf(audioSrcFormat.getSampleRate()).intValue() + "";

			params.add("-f");
			params.add("s16le");
			params.add("-ac");
			params.add(srcChannels);
			params.add("-ar");
			params.add(srcSampleRate);
		}

		params.add("-thread_queue_size");
		params.add("409600");
		params.add("-i");
		params.add("pipe:0");
		params.add("-c:v");
		params.add("copy");
		params.add("-c:a");
		params.add(audioCodec);
		params.add("-ac");
		params.add(channels);
		params.add("-ar");
		params.add(sampleRate);

		if (config.getVBR()) {
			setAudioVBR(config, params);
		}
		else {
			params.add("-b:a");
			params.add(Integer.toString(config.getBitrate() * 1000));
		}

		return params;
	}

	private void setAudioVBR(AudioRenderConfiguration config, List<String> params) {
		CodecID codecID = config.getCodecID();

		if (codecID == CodecID.AAC) {
			// Bitrate mapping: https://trac.ffmpeg.org/wiki/Encode/AAC
			int[] bitrates = { 20, 32, 48, 64, 96 };
			String[] values = { "1", "2", "3", "4", "5" };

			params.add("-profile:a");
			params.add("aac_he");

			setAudioVBR(config, "-vbr", params, bitrates, values);
		}
		else if (codecID == CodecID.MP3) {
			// Bitrate mapping: https://trac.ffmpeg.org/wiki/Encode/MP3
			int[] bitrates = { 85, 105, 120, 130, 150, 185, 195, 210, 250, 260 };
			String[] values = { "9", "8", "7", "6", "5", "4", "3", "2", "1", "0" };

			setAudioVBR(config, "-q:a", params, bitrates, values);
		}
		else if (codecID == CodecID.VORBIS) {
			// Bitrate mapping: https://en.wikipedia.org/wiki/Vorbis
			int[] bitrates = { 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 400 };
			String[] values = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };

			setAudioVBR(config, "-q:a", params, bitrates, values);
		}
		else if (codecID == CodecID.OPUS) {
			// Variable bitrate is used by default.
			String bitrate = Integer.toString(config.getBitrate() * 1000);

			params.add("-b:a");
			params.add(bitrate);
		}
	}

	private void setAudioVBR(AudioRenderConfiguration config, String qParam, List<String> params, int[] bitrates, String[] values) {
		int bitrate = config.getBitrate();
		// Select best quality as default.
		String value = values[values.length - 1];
		
		for (int i = 0; i < bitrates.length; i++) {
			if (bitrate <= bitrates[i]) {
				value = values[i];
				break;
			}
		}
		
		params.add(qParam);
		params.add(value);
	}
	
	private List<String> getVideoParameters(VideoRenderConfiguration config) {
		List<String> params = new ArrayList<>();
		
		Dimension2D size = config.getDimension();
		
		String frameRate = Integer.toString(config.getFrameRate());
		String pictureSize = Double.valueOf(size.getWidth()).intValue() + "x" + Double.valueOf(size.getHeight()).intValue();
		String videoCodec = getVideoCodecName(config.getCodecID());
		String bitrate = Integer.toString(config.getBitrate() * 1000);
		
		// Set explicitly the picture input format.
		params.add("-f");
		params.add("rawvideo");
		params.add("-pix_fmt");
		params.add("argb");
		params.add("-video_size");
		params.add(pictureSize);
		
		params.add("-r");
		params.add(frameRate);
		params.add("-i");
		params.add("pipe:0");
		params.add("-c:v");
		params.add(videoCodec);
		params.add("-video_size");
		params.add(pictureSize);
		params.add("-r");
		params.add(frameRate);
		params.add("-pix_fmt");
		params.add("yuv420p");
		params.add("-b:v");
		params.add(bitrate);
		
		if (config.getTwoPass()) {
			String pass = Integer.toString(config.getPass());
			String logfile = config.getTwoPassProfilePath();
			
			params.add("-pass");
			params.add(pass);
			params.add("-passlogfile");
			params.add(logfile);
			
			// No audio on first pass.
			if (config.getPass() == 1) {
				params.add("-an");
			}
		}
		else {
			params.add("-rtbufsize");
			params.add("10M");
			params.add("-bufsize");
			params.add("10M");
			params.add("-preset:v");
			params.add("veryslow");
		}
		
		if (config.getCodecID() == CodecID.H264) {
			params.add("-subq");
			params.add("6");
			params.add("-bf");
			params.add("3");
			params.add("-refs");
			params.add("2");
			params.add("-x264opts");
			params.add("8x8dct:b-pyramid=normal:weightb:rc-lookahead=100:keyint=500:scenecut=100");
		}
		
		return params;
	}
	
	private String getAudioCodecName(CodecID codecID) {
		switch (codecID) {
			case AAC:
				return "aac";
				
			case MP3:
				return "libmp3lame";
				
			case OPUS:
				return "libopus";
				
			case VORBIS:
				return "libvorbis";
				
			default:
				return "libmp3lame";
		}
	}
	
	private String getVideoCodecName(CodecID codecID) {
		switch (codecID) {
			case AV1:
				return "libaom-av1";

			case H264:
				return "libx264";

			case H264_AMF:
				return "h264_amf";

			case H264_NVIDIA:
				return "h264_nvenc";

			case H264_QSV:
				return "h264_qsv";

			case H265:
				return "libx265";

			case H265_AMF:
				return "hevc_amf";

			case H265_NVIDIA:
				return "hevc_nvenc";

			case H265_QSV:
				return "hevc_qsv";

			case VP9:
				return "libvpx-vp9";

			default:
				return "libx264";
		}
	}
}
