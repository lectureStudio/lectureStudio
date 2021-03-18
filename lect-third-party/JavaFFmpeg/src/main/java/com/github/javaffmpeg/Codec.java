package com.github.javaffmpeg;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.avcodec.*;
import org.bytedeco.javacpp.avutil.AVDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.AVRational;
import static org.bytedeco.javacpp.avutil.av_q2d;

public class Codec {

	private AVCodec avCodec;
	
	private AVCodecContext avContext;
	
	
	private Codec() {

	}
	
	int open(AVDictionary avDictionary) throws JavaFFmpegException {
		AVCodecContext avContext = avcodec_alloc_context3(avCodec);
		
		return open(avDictionary, avContext);
	}
	
	int open(AVDictionary avDictionary, AVCodecContext avContext) throws JavaFFmpegException {
		this.avContext = avContext;
		
		if (avContext == null)
			throw new JavaFFmpegException("Could not allocate codec context.");
		
		int ret = avcodec_open2(avContext, avCodec, avDictionary);
		
		return ret;
	}
	
	AVCodecContext getContext() {
		return avContext;
	}
	
	AVCodec getCodec() {
		return avCodec;
	}
	
	public static Codec getEncoderById(CodecID codecId) throws JavaFFmpegException {
		if (codecId == null)
			throw new NullPointerException("CodecID is null.");
		
		AVCodec avCodec = avcodec_find_encoder(codecId.value());
		
		if (avCodec == null || avCodec.isNull())
			throw new JavaFFmpegException("Encoder not found: " + codecId.toString());
		
		Codec codec = new Codec();
		codec.avCodec = avCodec;
		
		return codec;
	}
	
	public static Codec getDecoderById(CodecID codecId) throws JavaFFmpegException {
		if (codecId == null)
			throw new NullPointerException("CodecID is null.");
		
		AVCodec avCodec = avcodec_find_decoder(codecId.value());
		
		if (avCodec == null || avCodec.isNull())
			throw new JavaFFmpegException("Decoder not found: " + codecId.toString());
		
		Codec codec = new Codec();
		codec.avCodec = avCodec;
		
		return codec;
	}
	
	public static Codec getEncoderByName(String avCodecName) throws JavaFFmpegException {
		if (avCodecName == null || avCodecName.isEmpty())
			throw new NullPointerException("Codec name is null or empty.");
		
		AVCodec avCodec = avcodec_find_encoder_by_name(avCodecName);
		
		if (avCodec == null || avCodec.isNull())
			throw new JavaFFmpegException("Encoder not found: " + avCodecName);
		
		Codec codec = new Codec();
		codec.avCodec = avCodec;
		
		return codec;
	}
	
	public static Codec getDecoderByName(String avCodecName) throws JavaFFmpegException {
		if (avCodecName == null || avCodecName.isEmpty())
			throw new NullPointerException("Codec name is null or empty.");
		
		AVCodec avCodec = avcodec_find_decoder_by_name(avCodecName);
		
		if (avCodec == null || avCodec.isNull())
			throw new JavaFFmpegException("Decoder not found: " + avCodecName);
		
		Codec codec = new Codec();
		codec.avCodec = avCodec;
		
		return codec;
	}
	
	public static String[] getInstalledCodecNames() {
		List<String> names = new ArrayList<String>();

		AVCodec codec = null;
		while ((codec = av_codec_next(codec)) != null)
			names.add(codec.name().getString());

		return names.toArray(new String[0]);
	}
	
	public String getName() {
		if (avCodec == null)
			return null;
		
		return avCodec.name().getString();
	}
	
	public String getNameLong() {
		if (avCodec == null)
			return null;
		
		return avCodec.long_name().getString();
	}
	
	public CodecID getID() {
		if (avCodec.isNull())
			return null;
		
		return CodecID.byId(avCodec.id());
	}
	
	public MediaType getType() {
		if (avCodec.isNull())
			return null;
		
		return MediaType.byId(avCodec.type());
	}
	
	public boolean canDecode() {
		if (avCodec.isNull())
			return false;
		
		return av_codec_is_decoder(avCodec) != 0;
	}

	public boolean canEncode() {
		if (avCodec.isNull())
			return false;
		
		return av_codec_is_encoder(avCodec) != 0;
	}
	
	public int getCapabilities() {
		return avCodec.capabilities();
	}
	
	public boolean hasCapability(CodecCapability flag) {
		if (avCodec.isNull())
			return false;
		
		return (avCodec.capabilities() & flag.value()) != 0;
	}
	
	public SampleFormat[] getSupportedSampleFormats() {
		IntPointer sampleFormatsPointer = avCodec.sample_fmts();
		
		if (getType() != MediaType.AUDIO || sampleFormatsPointer == null)
			return null;
		
		List<SampleFormat> sampleFormats = new ArrayList<SampleFormat>();
		
		int format;
		int index = 0;
		while ((format = sampleFormatsPointer.get(index++)) != -1)
			sampleFormats.add(SampleFormat.byId(format));
		
		return sampleFormats.toArray(new SampleFormat[0]);
	}

	public Integer[] getSupportedSampleRates() {
		IntPointer sampleRatesPointer = avCodec.supported_samplerates();

		if (getType() != MediaType.AUDIO || sampleRatesPointer == null)
			return null;

		List<Integer> sampleRates = new ArrayList<Integer>();

		int sampleRate;
		int index = 0;
		while ((sampleRate = sampleRatesPointer.get(index++)) != 0)
			sampleRates.add(sampleRate);

		// ascending order
		Collections.sort(sampleRates);

		return sampleRates.toArray(new Integer[0]);
	}

	public Integer[] getSupportedFrameRates() {
		AVRational frameRates = avCodec.supported_framerates();

		if (getType() != MediaType.VIDEO || frameRates == null)
			return null;

		List<Integer> rates = new ArrayList<Integer>();

		AVRational frameRate;
		int index = 0;
		while ((frameRate = frameRates.position(index++)) != null)
			rates.add((int) av_q2d(frameRate));

		// ascending order
		Collections.sort(rates);

		return rates.toArray(new Integer[0]);
	}

	public ChannelLayout[] getSupportedChannelLayouts() {
		LongPointer layoutsPointer = avCodec.channel_layouts();

		if (getType() != MediaType.AUDIO || layoutsPointer == null)
			return null;

		List<ChannelLayout> layouts = new ArrayList<ChannelLayout>();

		long layout;
		int index = 0;
		while ((layout = layoutsPointer.get(index++)) != 0)
			layouts.add(ChannelLayout.byId(layout));

		// ascending order
		Collections.sort(layouts);

		return layouts.toArray(new ChannelLayout[0]);
	}

	public PixelFormat[] getSupportedPixelFormats() {
		IntPointer formatsPointer = avCodec.pix_fmts();

		if (getType() != MediaType.VIDEO || formatsPointer == null)
			return null;

		List<PixelFormat> pixelFormats = new ArrayList<PixelFormat>();

		int format;
		int index = 0;
		while ((format = formatsPointer.get(index++)) != -1) {
			PixelFormat pixelFormat = PixelFormat.byId(format);
			if (pixelFormat != null)
				pixelFormats.add(pixelFormat);
		}

		// ascending order
		Collections.sort(pixelFormats);

		return pixelFormats.toArray(new PixelFormat[0]);
	}

}
