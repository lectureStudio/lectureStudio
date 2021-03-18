package com.github.javaffmpeg;

import static org.bytedeco.javacpp.avutil.av_get_default_channel_layout;

import org.bytedeco.javacpp.BytePointer;

import java.nio.ByteBuffer;

public class Audio {
	
	private static final float twoPower7  = 128.0f;
	private static final float twoPower15 = 32768.0f;
	private static final float twoPower23 = 8388608.0f;
	private static final float twoPower31 = 2147483648.0f;
	

	public static ChannelLayout getChannelLayout(int channels) {
		long layout = av_get_default_channel_layout(channels);
	    ChannelLayout channelLayout = ChannelLayout.byId(layout);
	    
	    return channelLayout;
	}
	
	public static SampleFormat getSampleFormat(int sampleSize, boolean planar, boolean fltp) {
		switch (sampleSize) {
			case 1:
				return planar ? SampleFormat.U8P : SampleFormat.U8;
			case 2:
				return planar ? SampleFormat.S16P : SampleFormat.S16;
			case 4:
				if (fltp) {
					return planar ? SampleFormat.FLTP : SampleFormat.FLT;
				}
				else {
					return planar ? SampleFormat.S32P : SampleFormat.S32;
				}
			case 8:
				return planar ? SampleFormat.DBLP : SampleFormat.DBL;
			default:
				return null;
		}
	}
	
	public static int getFormatDepth(SampleFormat format) {
		switch (format) {
			case U8:	case U8P:
				return 1;
			case S16:	case S16P:
				return 2;
			case S32:	case S32P:
				return 4;
			case FLT:	case FLTP:
				return 4;
			case DBL:	case DBLP:
				return 8;
		
			default:
				return 0;
		}
	}
	
	public static Number getValue(ByteBuffer buffer, SampleFormat format, int index) {
		switch (format) {
			case U8:	case U8P:
				return buffer.get(index);
			case S16:	case S16P:
				return buffer.getShort(index);
			case S32:	case S32P:
				return buffer.getInt(index);
			case FLT:	case FLTP:
				return buffer.getFloat(index);
			case DBL:	case DBLP:
				return buffer.getDouble(index);
		
			default:
				return 0;
		}
	}
	
	public static byte[] getAudio8(AudioFrame frame) {
		BytePointer[] planes = frame.getPlanes();
		SampleFormat format = frame.getAudioFormat().getSampleFormat();
		int depth = getFormatDepth(format);
		
		int length = planes[0].limit() / depth;
		int channels = planes.length;
		byte[] samples = new byte[channels * length];
		
		for (int i = 0; i < channels; i++) {
			ByteBuffer buffer = planes[i].asByteBuffer();
			int offset = i * channels;
			
			for (int j = 0, k = offset; j < length; j++) {
				long sample = quantize8(getValue(buffer, format, j * depth).doubleValue() * twoPower7);
				samples[k++] = (byte) (sample & 0x80);
				
				// interleave
				k += 2 * (channels - 1);
			}
		}
		
		return samples;
	}
	
	public static void getAudio16(AudioFrame frame, byte[] dest) {
		BytePointer[] planes = frame.getPlanes();
		SampleFormat format = frame.getAudioFormat().getSampleFormat();
		
		int depth = getFormatDepth(format);
		int length = planes[0].limit() / depth;
		int channels = planes.length;
		
		for (int i = 0; i < channels; i++) {
			ByteBuffer buffer = planes[i].asByteBuffer();
			int offset = i * channels;
			
			for (int j = 0, k = offset; j < length; j++) {
				long sample = quantize16(getValue(buffer, format, j * depth).intValue());
				dest[k++] = (byte) ( sample        & 0xFF);
				dest[k++] = (byte) ((sample >> 8)  & 0xFF);
				
				// interleave
				k += 2 * (channels - 1);
			}
		}
	}
	
	public static byte[] getAudio24(AudioFrame frame) {
		BytePointer[] planes = frame.getPlanes();
		SampleFormat format = frame.getAudioFormat().getSampleFormat();
		int depth = getFormatDepth(format);
		
		int length = planes[0].limit() / depth;
		int channels = planes.length;
		byte[] samples = new byte[channels * length * 3];
		
		for (int i = 0; i < channels; i++) {
			ByteBuffer buffer = planes[i].asByteBuffer();
			int offset = i * channels;
			
			for (int j = 0, k = offset; j < length; j++) {
				long sample = quantize24(getValue(buffer, format, j * depth).doubleValue() * twoPower23);
				samples[k++] = (byte) ( sample        & 0xFF);
				samples[k++] = (byte) ((sample >> 8)  & 0xFF);
				samples[k++] = (byte) ((sample >> 16) & 0xFF);
				
				// interleave
				k += 2 * (channels - 1);
			}
		}
		
		return samples;
	}
	
	public static byte[] getAudio32(AudioFrame frame) {
		BytePointer[] planes = frame.getPlanes();
		SampleFormat format = frame.getAudioFormat().getSampleFormat();
		int depth = getFormatDepth(format);
		
		int length = planes[0].limit() / depth;
		int channels = planes.length;
		byte[] samples = new byte[channels * length * 4];
		
		for (int i = 0; i < channels; i++) {
			ByteBuffer buffer = planes[i].asByteBuffer();
			int offset = i * channels;
			
			for (int j = 0, k = offset; j < length; j++) {
				long sample = quantize32(getValue(buffer, format, j * depth).doubleValue() * twoPower31);
				samples[k++] = (byte) ( sample        & 0xFF);
				samples[k++] = (byte) ((sample >> 8)  & 0xFF);
				samples[k++] = (byte) ((sample >> 16) & 0xFF);
				samples[k++] = (byte) ((sample >> 24) & 0xFF);
				
				// interleave
				k += 2 * (channels - 1);
			}
		}
		
		return samples;
	}
	
	public static byte quantize8(double sample) {
		if (sample >= 127.0f) {
			return (byte) 127;
		}
		else if (sample <= -128) {
			return (byte) -128;
		}
		else {
			return (byte) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
		}
	}
	
	public static int quantize16(int sample) {
		if (sample >= 32767.0f) {
			return 32767;
		}
		else if (sample <= -32768.0f) {
			return -32768;
		}
		else {
			return (int) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
		}
	}
	
	public static int quantize24(double sample) {
		if (sample >= 8388607.0f) {
			return 8388607;
		}
		else if (sample <= -8388608.0f) {
			return -8388608;
		}
		else {
			return (int) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
		}
	}

	public static int quantize32(double sample) {
		if (sample >= 2147483647.0f) {
			return 2147483647;
		}
		else if (sample <= -2147483648.0f) {
			return -2147483648;
		}
		else {
			return (int) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
		}
	}
	
}
