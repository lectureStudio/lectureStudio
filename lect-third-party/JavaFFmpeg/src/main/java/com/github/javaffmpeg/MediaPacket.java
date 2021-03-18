package com.github.javaffmpeg;

import org.bytedeco.javacpp.avcodec.AVPacket;

import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.avcodec.av_free_packet;

public class MediaPacket {
	
	private AVPacket avPacket;
	
	private ByteBuffer packetData;
	
	private Rational timebase;
	
	private long pts;
	
	private long dts;
	
	private long duration;
	
	private boolean keyFrame;
	
	
	MediaPacket(AVPacket avPacket) {
		this.avPacket = avPacket;
	}
	
	AVPacket getAVPacket() {
		return avPacket;
	}

	public MediaPacket(ByteBuffer data) {
		this.packetData = data;
	}
	
	public ByteBuffer getData() {
		if (packetData == null && avPacket != null)
			packetData = avPacket.data().limit(avPacket.size()).asByteBuffer();

		return packetData;
	}
	
	public Rational getTimebase() {
		return timebase;
	}

	public void setTimebase(Rational timebase) {
		this.timebase = timebase;
	}

	public long getPts() {
		return pts;
	}

	public void setPts(long pts) {
		this.pts = pts;
	}

	public long getDts() {
		return dts;
	}

	public void setDts(long dts) {
		this.dts = dts;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setKeyFrame(boolean keyFrame) {
		this.keyFrame = keyFrame;
	}
	
	public boolean isKeyFrame() {
		return keyFrame;
	}
	
	public void clear() {
		if (avPacket != null)
			av_free_packet(avPacket);
	}

}
