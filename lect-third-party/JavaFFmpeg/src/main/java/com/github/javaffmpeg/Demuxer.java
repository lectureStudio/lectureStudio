package com.github.javaffmpeg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bytedeco.javacpp.avcodec.AVCodecContext;
import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avformat.*;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.avutil.*;

import static org.bytedeco.javacpp.avcodec.av_free_packet;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class Demuxer extends Configurable {

	private Map<Integer, Long> streamProgress = new HashMap<>();
	
	private MediaPacket mediaPacket;
	
	private Decoder videoDecoder;
	private Decoder audioDecoder;
	
	private AVFormatContext formatContext;

	private AVStream videoStream;
	private AVStream audioStream;
	
	private AVCodecContext videoCodecContext;
	private AVCodecContext audioCodecContext;
	
	private AVPacket avPacket;
	
	private String format = null;


	public void open(RandomAccessStream inputStream) throws JavaFFmpegException, IOException {
		streamProgress.clear();
		
		videoDecoder = null;
		audioDecoder = null;
		
		int bufferSize = 4 * 1024;
		
		// Buffer must have AVPROBE_PADDING_SIZE of extra allocated bytes filled with zero.
		BytePointer buffer = new BytePointer(bufferSize + avformat.AVPROBE_PADDING_SIZE);
		AVInputStream streamPointer = new AVInputStream(inputStream);
		
		AVIOContext ioCtx = avio_alloc_context(buffer, bufferSize, 0, null, streamPointer.readFunc, null, streamPointer.seekFunc);
		
		if (ioCtx == null || ioCtx.isNull()) {
			throw new JavaFFmpegException("Alloc io context failed.");
		}
		
		formatContext = avformat_alloc_context();
		
		if (formatContext == null || formatContext.isNull()) {
			throw new JavaFFmpegException("Alloc format context failed.");
		}
		
		// Set the custom IOContext.
		formatContext.pb(ioCtx);
		
		// Determining the input format.
		byte[] header = new byte[bufferSize];
		inputStream.read(header, 0, bufferSize);
		// Reset the data pointer back to the beginning.
		inputStream.reset();
		
		buffer.put(header, 0, bufferSize).position(0);
		
		AVProbeData probeData = new AVProbeData();
		probeData.buf(buffer);
		probeData.buf_size(bufferSize);
		
		AVInputFormat inputFormat = av_probe_input_format(probeData, 1);
		
		if (inputFormat == null || inputFormat.isNull()) {
			throw new JavaFFmpegException("Probe input format failed.");
		}
		
		formatContext.iformat(inputFormat);
		formatContext.flags(AVFormatContext.AVFMT_FLAG_CUSTOM_IO);
		
		if (avformat_open_input(formatContext, (String)null, null, null) < 0) {
			throw new JavaFFmpegException("Open input format failed.");
		}
		
	    if (avformat_find_stream_info(formatContext, (AVDictionary)null) < 0) {
	        throw new JavaFFmpegException("Find stream information failed.");
	    }
	    
	    av_dump_format(formatContext, 0, "stream", 0);
	    
	 	// find the first video and audio stream
	 	int streams = formatContext.nb_streams();
	 	
	 	for (int i = 0; i < streams; i++) {
			AVStream stream = formatContext.streams(i);
			AVCodecContext context = stream.codec();
			
			if (videoStream == null && context.codec_type() == avutil.AVMEDIA_TYPE_VIDEO) {
				videoStream = stream;
				videoCodecContext = context;
			}
			else if (audioStream == null && context.codec_type() == avutil.AVMEDIA_TYPE_AUDIO) {
				audioStream = stream;
				audioCodecContext = context;
			}
			
			streamProgress.put(stream.index(), 0L);
		}
	 	
	 	if (videoStream == null && audioStream == null)
			throw new JavaFFmpegException("Could not find any video or audio stream.");
		
		// try creating decoder in separate way, since video or audio may be not present
		if (videoCodecContext != null)
			initVideoDecoder(videoCodecContext);

		if (audioCodecContext != null)
			initAudioDecoder(audioCodecContext);
		
		avPacket = new AVPacket();
	}
	
	public void open(String inputPath) throws JavaFFmpegException {
		streamProgress.clear();
		
		videoDecoder = null;
		audioDecoder = null;
		
		AVInputFormat inputFormat = null;
		if (format != null && format.length() > 0) {
			inputFormat = av_find_input_format(format);
			
			if (inputFormat == null)
				throw new JavaFFmpegException("Could not find input format: " + format);
		}

		formatContext = new AVFormatContext(null);

		AVDictionary options = new AVDictionary(null);
		if (frameRate > 0) {
			AVRational r = av_d2q(frameRate, 1001000);
			av_dict_set(options, "framerate", r.num() + "/" + r.den(), 0);
		}
		if (imageWidth > 0 && imageHeight > 0)
			av_dict_set(options, "video_size", imageWidth + "x" + imageHeight, 0);

		if (sampleRate > 0)
			av_dict_set(options, "sample_rate", "" + sampleRate, 0);

		if (audioChannels > 0)
			av_dict_set(options, "channels", "" + audioChannels, 0);
		
		if (format.equalsIgnoreCase("dshow")) {
			av_dict_set(options, "avioflags", "direct", 0);
		}
		
		if (audioBufferSize > 0) {
			if (format.equalsIgnoreCase("dshow")) {
				av_dict_set(options, "audio_buffer_size", "" + audioBufferSize, 0);
			}
			if (format.equalsIgnoreCase("pulse")) {
				av_dict_set(options, "fragment_size", "" + audioBufferSize, 0);
			}
		}

		if (avformat_open_input(formatContext, inputPath, inputFormat, options) < 0)
			throw new JavaFFmpegException("Could not open input: " + inputPath);

		av_dict_free(options);

		// retrieve stream information
		if (avformat_find_stream_info(formatContext, (AVDictionary) null) < 0)
			throw new JavaFFmpegException("Could not find stream information.");

		//av_dump_format(formatContext, 0, inputPath, 0);
		
		// find the first video and audio stream
		int streams = formatContext.nb_streams();
		
		// get a pointer to the codec context for the video or audio stream
		for (int i = 0; i < streams; i++) {
			AVStream stream = formatContext.streams(i);
			AVCodecContext context = stream.codec();
			
			if (videoStream == null && context.codec_type() == avutil.AVMEDIA_TYPE_VIDEO) {
				videoStream = stream;
				videoCodecContext = context;
			}
			else if (audioStream == null && context.codec_type() == avutil.AVMEDIA_TYPE_AUDIO) {
				audioStream = stream;
				audioCodecContext = context;
			}
			
			streamProgress.put(stream.index(), 0L);
		}
		
		if (videoStream == null && audioStream == null)
			throw new JavaFFmpegException("Could not find any video or audio stream.");
		
		// try creating decoder in separate way, since video or audio may be not present
		if (videoCodecContext != null)
			initVideoDecoder(videoCodecContext);

		if (audioCodecContext != null)
			initAudioDecoder(audioCodecContext);
		
		avPacket = new AVPacket();
	}
	
	public void close() {
		if (formatContext != null && !formatContext.isNull()) {
			avformat_close_input(formatContext);
			formatContext = null;
		}
		
		// set explicitly to null since avformat_close_input already released the memory.
		// this way the decoder knows the context is already closed.
		if (videoCodecContext != null)
			videoCodecContext.setNull();

		if (audioCodecContext != null)
			audioCodecContext.setNull();
		
		if (videoDecoder != null) {
			videoDecoder.close();
			videoDecoder = null;
		}
		if (audioDecoder != null) {
			audioDecoder.close();
			audioDecoder = null;
		}
	}
	
	public double getStreamProgress(int streamIndex) {
		Long position = streamProgress.get(streamIndex);
		if (position == null)
			return 0;
		
		long duration = Long.MAX_VALUE;
		
		if (videoStream != null && videoStream.index() == streamIndex) {
			duration = videoStream.duration();
		}
		if (audioStream != null && audioStream.index() == streamIndex) {
			duration = audioStream.duration();
		}
		
		return position * 1.D / duration; 
	}
	
	public double getTotalProgress() {
		double total = 0;
		long duration = Long.MAX_VALUE;
		
		for (Integer index : streamProgress.keySet()) {
			Long position = streamProgress.get(index);
			
			if (videoStream != null && videoStream.index() == index) {
				duration = videoStream.duration();
			}
			if (audioStream != null && audioStream.index() == index) {
				duration = audioStream.duration();
			}
			
			total += (position * 1.D / duration);
		}
		
		return total / streamProgress.size();
	}
	
	public MediaFrame readFrame() throws JavaFFmpegException {
		MediaFrame mediaFrame = new MediaFrame();
		
        while (mediaFrame != null && !mediaFrame.hasFrame()) {
			if (av_read_frame(formatContext, avPacket) < 0) {
				if (videoStream != null) {
					// Video codec may have buffered some frames.
					avPacket.stream_index(videoStream.index());
					avPacket.data(null);
					avPacket.size(0);
				}
				else {
					return null;
				}
			}
			
			// Update stream progress.
			Long position = streamProgress.get(avPacket.stream_index());
			streamProgress.put(avPacket.stream_index(), position + avPacket.duration());
			
			mediaPacket = new MediaPacket(avPacket);
			
			if (videoStream != null && avPacket.stream_index() == videoStream.index()) {
				mediaFrame = videoDecoder.decodeVideo(mediaPacket);
			}
			else if (audioStream != null && avPacket.stream_index() == audioStream.index()) {
				mediaFrame = audioDecoder.decodeAudio(mediaPacket);
			}
			
			av_free_packet(avPacket);
        }
		
		return mediaFrame;
	}
	
    public void setInputFormat(String format) {
        this.format = format;
    }
	
	public String getInputFormat() {
		if (formatContext == null) {
			return format;
		}
		else {
			return formatContext.iformat().name().getString();
		}
	}

	public int getImageWidth() {
		return videoDecoder == null ? super.getImageWidth() : videoDecoder.getImageWidth();
	}
	
	public int getImageHeight() {
		return videoDecoder == null ? super.getImageHeight() : videoDecoder.getImageHeight();
	}
	
	public int getAudioChannels() {
		return audioDecoder == null ? super.getAudioChannels() : audioDecoder.getAudioChannels();
	}

	public PixelFormat getPixelFormat() {
		return audioDecoder == null ? super.getPixelFormat() : audioDecoder.getPixelFormat();
	}

	public SampleFormat getSampleFormat() {
		return audioDecoder == null ? super.getSampleFormat() : audioDecoder.getSampleFormat();
	}
	
	public double getFrameRate() {
		if (videoStream == null) {
			return super.getFramerate();
		}
		else {
			AVRational rate = videoStream.r_frame_rate();
			return av_q2d(rate);
		}
	}

	public int getSampleRate() {
		return audioDecoder == null ? super.getSampleRate() : audioDecoder.getSampleRate();
	}
	
	private void initVideoDecoder(AVCodecContext codecContext) {
		if (codecContext == null)
			return;
		
		try {
			Codec videoCodec = Codec.getDecoderById(CodecID.byId(codecContext.codec_id()));

			videoDecoder = new Decoder(videoCodec, codecContext);
			videoDecoder.setPixelFormat(getPixelFormat());
			videoDecoder.open(null);
		}
		catch (JavaFFmpegException e) {
			e.printStackTrace();
		}
	}
	
	private void initAudioDecoder(AVCodecContext codecContext) {
		if (codecContext == null)
			return;
		
		try {
			Codec audioCodec = Codec.getDecoderById(CodecID.byId(codecContext.codec_id()));
			
			audioDecoder = new Decoder(audioCodec, codecContext);
			audioDecoder.open(null);
		}
		catch (JavaFFmpegException e) {
			e.printStackTrace();
		}
	}
	
}
