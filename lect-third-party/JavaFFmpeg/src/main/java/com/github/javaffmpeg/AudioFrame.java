package com.github.javaffmpeg;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;

import static org.bytedeco.javacpp.avutil.*;

public class AudioFrame extends MediaFrame {

	private AudioFormat format;

	private PointerPointer<BytePointer> samplePointer;

	private BytePointer[] planePointers;
	
	private int samples;

	
	public AudioFrame(AudioFormat format, int samples) {
		int channels = format.getChannels();
		int sampleFormat = format.getSampleFormat().value();
		int planes = av_sample_fmt_is_planar(sampleFormat) != 0 ? channels : 1;
		int planeLength = (samples * channels * av_get_bytes_per_sample(sampleFormat)) / planes;
		
		this.format = format;
		this.samples = samples;
		this.planePointers = new BytePointer[planes];
		this.samplePointer = new PointerPointer<>(planes);
		
		for (int i = 0; i < planes; i++) {
			this.planePointers[i] = new BytePointer(av_malloc(planeLength)).capacity(planeLength);
			this.planePointers[i].limit(planeLength);
			this.samplePointer.put(i, planePointers[i]);
		}
	}
	
	public AudioFormat getAudioFormat() {
		return format;
	}

	public PointerPointer<BytePointer> getData() {
		return samplePointer;
	}
	
	public int getBufferSize() {
		return planePointers[0].capacity();
	}
	
	public BytePointer[] getPlanes() {
		return planePointers;
	}
	
	public BytePointer getPlane(int i) {
		return planePointers[i];
	}
	
	public int getPlaneCount() {
		return planePointers.length;
	}
	
	public void setSampleCount(int samples) {
		this.samples = samples;
	}
	
	public int getSampleCount() {
		return samples;
	}
	
	@Override
	public Type getType() {
		return Type.AUDIO;
	}
	
	@Override
	public boolean hasFrame() {
		return planePointers != null && planePointers.length > 0;
	}
	
	public void clear() {
		if (planePointers != null) {
			for (int i = 0; i < getPlaneCount(); i++) {
				av_free(planePointers[i].position(0));
				samplePointer.put(i, null);
			}
			planePointers = null;
		}
	}
	
}
