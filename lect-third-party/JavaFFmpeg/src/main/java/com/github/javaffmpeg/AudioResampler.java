package com.github.javaffmpeg;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.swresample.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swresample.*;

public class AudioResampler {

	/** The re-sample context */
	private SwrContext convertContext;
	
	/** Re-sampled sample buffer */
	private RingBuffer buffer;
	
	/** Amount of samples per output frame */
	private int frameSamples;
	
	/** Input audio format */
	private AudioFormat srcFormat;
	
	/** Output audio format */
	private AudioFormat dstFormat;
	
	
	/**
	 * Initializes the {@code AudioResampler} with specified input and output audio formats.
	 * 
	 * @param inLayout  input channel layout.
	 * @param inFormat  input sample format.
	 * @param inRate    input sample rate.
	 * @param outLayout output channel layout.
	 * @param outFormat output sample format.
	 * @param outRate   output sample rate.
	 * @param frameSamples amount of samples per output frame.
	 * 
	 * @throws JavaFFmpegException if re-sampler cannot be opened.
	 */
	public void open(AudioFormat srcFormat, AudioFormat dstFormat, int frameSamples) throws JavaFFmpegException {
		if (srcFormat == null || dstFormat == null)
			throw new JavaFFmpegException("Invalid audio format provided: source " + srcFormat + " dest " + dstFormat);
		
		if (srcFormat.equals(dstFormat))
			return;
		
		this.srcFormat = srcFormat;
		this.dstFormat = dstFormat;
		
		this.frameSamples = frameSamples;
		
		convertContext = swr_alloc();
		
		if (convertContext == null)
			throw new JavaFFmpegException("Could not allocate the audio conversion context.");
		
		long dstChannelLayout = dstFormat.getChannelLayout().value();
		long srcChannelLayout = srcFormat.getChannelLayout().value();
		
		av_opt_set_int(convertContext, "ocl", dstChannelLayout, 0);
		av_opt_set_int(convertContext, "osf", dstFormat.getSampleFormat().value(), 0);
		av_opt_set_int(convertContext, "osr", dstFormat.getSampleRate(), 0);
		av_opt_set_int(convertContext, "icl", srcChannelLayout, 0);
		av_opt_set_int(convertContext, "isf", srcFormat.getSampleFormat().value(), 0);
		av_opt_set_int(convertContext, "isr", srcFormat.getSampleRate(), 0);
		av_opt_set_int(convertContext, "tsf", AV_SAMPLE_FMT_NONE, 0);
		av_opt_set_int(convertContext, "ich", av_get_channel_layout_nb_channels(srcChannelLayout), 0);
		av_opt_set_int(convertContext, "och", av_get_channel_layout_nb_channels(dstChannelLayout), 0);
		av_opt_set_int(convertContext, "uch", 0, 0);

		if (swr_init(convertContext) < 0)
            throw new JavaFFmpegException("Could not initialize the conversion context.");
		
		buffer = new RingBuffer(1024 * 1000, dstFormat.getChannels());
	}
	
	public AudioFrame[] resample(AudioFrame frame) throws JavaFFmpegException {
		List<AudioFrame> frames = new ArrayList<AudioFrame>();
		
		int outputChannels = dstFormat.getChannels();
		int outputFormat = dstFormat.getSampleFormat().value();
		
		int planes = av_sample_fmt_is_planar(outputFormat) != 0 ? outputChannels : 1;
		//int destSamples = Math.min(frameSamples, (int) av_rescale_rnd(frameSamples, outputRate, inputRate, AV_ROUND_DOWN));
		int destSamples = frameSamples;
		
		// use enough space to avoid buffering
		int bufferSamples = destSamples * 128;
		AudioFrame tempFrame = new AudioFrame(dstFormat, bufferSamples);
		
		//System.out.println("frame size " + frameSamples + " dest size " + destSamples);
		
		int inSamples = 0;
		int outSamples = bufferSamples; // make sure we get everything out
		
		PointerPointer<BytePointer> inPointer = null;
		
		if (frame != null) {
			inSamples = frame.getSampleCount();
			inPointer = frame.getData();
		}
		
		int resampled = swr_convert(convertContext, tempFrame.getData(), outSamples, inPointer, inSamples);
		
		// re-sampled plane size
		int limit = (resampled * outputChannels * av_get_bytes_per_sample(outputFormat)) / planes;
		
		// write samples into buffer in case the current frame cannot be fully filled
		for (int i = 0; i < planes; i++) {
			ByteBuffer buf = tempFrame.getPlane(i).asByteBuffer();
			buf.limit(Math.min(limit, buf.capacity()));
			
			buffer.write(i, buf);
		}
		
		// release memory of temporary working space
		tempFrame.clear();
		
		// output frame plane size
		int bufferSize = (destSamples * outputChannels * av_get_bytes_per_sample(outputFormat)) / planes;
		
		// get buffered samples that fit in one frame
		while (buffer.available() >= bufferSize) {
			AudioFrame outFrame = new AudioFrame(dstFormat, destSamples);
			
			for (int i = 0; i < planes; i++) {
				byte[] data = new byte[bufferSize];
				
				try {
					buffer.read(i, data);
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				outFrame.getPlane(i).position(0).put(data);
			}
			
			outFrame.setSampleCount(frameSamples);
			
			frames.add(outFrame);
		}
		
		return frames.toArray(new AudioFrame[0]);
	}
	
	public void close() {
		if (convertContext != null) {
            swr_free(convertContext);
            convertContext = null;
        }
	}
	
}
