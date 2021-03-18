package com.github.javaffmpeg;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.avcodec.AVCodecContext;

import java.util.Map;
import java.util.Map.Entry;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.*;

public class Coder extends Configurable {
	
	public enum State { Closed, Opened };
	
	protected int[] gotFrame = new int[1];
	
	protected AVCodecContext avContext;

	protected AVFrame avFrame;
	
	protected AVPacket avPacket;

	protected Codec codec;

	protected State state;
	
	
	public Coder(Codec codec) throws JavaFFmpegException {
		this(codec, null);
	}
	
	Coder(Codec codec, AVCodecContext avContext) throws JavaFFmpegException {
		this.codec = codec;
		this.avContext = avContext;
		
		state = State.Closed;
	}
	
	public void open(Map<String, String> options) throws JavaFFmpegException {
		if (avContext == null)
			avContext = avcodec_alloc_context3(codec.getCodec());

		if (profile > 0) {
			avContext.profile(profile);
		}
		if (quality > -10) {
			avContext.global_quality((int) Math.round(FF_QP2LAMBDA * quality));
		}
		
		avContext.bit_rate(bitrate);
		
		if (codec.getCodec().type() == AVMEDIA_TYPE_VIDEO) {
			if (imageWidth > 0) {
				avContext.width(imageWidth);
			}
			if (imageHeight > 0) {
				avContext.height(imageHeight);
			}
			if (pixelFormat != null) {
				avContext.pix_fmt(pixelFormat.value());
			}
			if (frameRate > 0) {
				avContext.time_base(av_inv_q(av_d2q(frameRate, 1001000)));
			}
			if (gopSize > 0) {
				avContext.gop_size(gopSize);
			}
		}
		
		if (codec.getCodec().type() == AVMEDIA_TYPE_AUDIO) {
			if (sampleFormat != null) {
				int sampleBitSize = av_get_bytes_per_sample(sampleFormat.value()) * 8;
				avContext.bits_per_raw_sample(sampleBitSize);
				
				avContext.sample_fmt(sampleFormat.value());
			}
			if (sampleRate > 0) {
				avContext.sample_rate(sampleRate);
				avContext.time_base().num(1).den(sampleRate);
			}
			if (audioChannels > 0) {
				avContext.channels(audioChannels);
				avContext.channel_layout(av_get_default_channel_layout(audioChannels));
			}
		}
		
		for (CodecFlag flag : flags)
			avContext.flags(avContext.flags() | (int)flag.value());

    	if (codec.hasCapability(CodecCapability.EXPERIMENTAL)) {
    		avContext.strict_std_compliance(AVCodecContext.FF_COMPLIANCE_EXPERIMENTAL);
    	}
		
		AVDictionary avDictionary = new AVDictionary(null);

		if (options != null) {
			for (Entry<String, String> e : options.entrySet()) {
				av_dict_set(avDictionary, e.getKey(), e.getValue(), 0);
			}
		}
		
		int ret = codec.open(avDictionary, avContext);
		
		av_dict_free(avDictionary);

		if (ret < 0) {
			close();
			throw new JavaFFmpegException("Could not open codec: error " + ret);
		}

        avFrame = av_frame_alloc();
        if (avFrame == null)
        	throw new JavaFFmpegException("Could not allocate frame.");
        
        avPacket = new AVPacket();
        if (avPacket == null)
        	throw new JavaFFmpegException("Could not allocate packet.");
        
        state = State.Opened;
	}
	
	public void close() {
		if (avFrame != null) {
			av_frame_free(avFrame);
			avFrame = null;
		}
		
		if (avContext != null && !avContext.isNull()) {
			avcodec.avcodec_close(avContext);

			if (avContext.extradata() != null)
				avutil.av_free(avContext.extradata());

			avutil.av_free(avContext);
			
			avContext = null;
		}
		
		state = State.Closed;
	}
	
	/**
	 * Flush buffers, should be called when seeking or when switching to a
	 * different stream.
	 */
	public void flush() {
		if (avContext == null)
			return;
		
		avcodec.avcodec_flush_buffers(avContext);
	}

	@Override
	public MediaType getMediaType() {
		if (avContext != null)
			return MediaType.byId(avContext.codec_type());

		return super.getMediaType();
	}

	@Override
	public int getImageWidth() {
		if (avContext != null)
			return avContext.width();

		return super.getImageWidth();
	}

	@Override
	public int getImageHeight() {
		if (avContext != null)
			return avContext.height();

		return super.getImageHeight();
	}

	@Override
	public int getGOPSize() {
		if (avContext != null)
			return avContext.gop_size();

		return super.getGOPSize();
	}

	@Override
	public PixelFormat getPixelFormat() {
		if (avContext != null)
			return PixelFormat.byId(avContext.pix_fmt());

		return super.getPixelFormat();
	}

	@Override
	public SampleFormat getSampleFormat() {
		if (avContext != null)
			return SampleFormat.byId(avContext.sample_fmt());

		return super.getSampleFormat();
	}
	
	@Override
	public ChannelLayout getChannelLayout() {
		if (avContext != null)
			return ChannelLayout.byId(avContext.channel_layout());

		return super.getChannelLayout();
	}

	@Override
	public int getBitrate() {
		if (avContext != null)
			return (int) avContext.bit_rate();

		return super.getBitrate();
	}

	@Override
	public double getFramerate() {
		if (avContext != null)
			return av_q2d(avContext.time_base());

		return super.getFramerate();
	}

	@Override
	public int getSampleRate() {
		if (avContext != null)
			return avContext.sample_rate();

		return super.getSampleRate();
	}

	@Override
	public int getAudioChannels() {
		if (avContext != null)
			return avContext.channels();

		return super.getAudioChannels();
	}
	
	@Override
	public double getQuality() {
		if (avContext != null)
			return Math.round(avContext.global_quality() / FF_QP2LAMBDA);

		return super.getQuality();
	}

	@Override
	public int getProfile() {
		if (avContext != null)
			return avContext.profile();

		return super.getProfile();
	}

}
