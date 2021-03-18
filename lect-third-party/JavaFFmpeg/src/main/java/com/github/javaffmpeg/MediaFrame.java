package com.github.javaffmpeg;

public class MediaFrame {

	public enum Type { AUDIO, VIDEO };
	
	private Rational timebase;
	
	private long pts;
	
	private long dts;
	
	private long duration;
	
	private int streamIndex;
	
	private Type type;
	
	private boolean keyFrame;
	private boolean hasFrame;

	
	public MediaFrame() {
		
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
	
	public void setStreamIndex(int index) {
		this.streamIndex = index;
	}
	
	public int getStreamIndex() {
		return streamIndex;
	}
	
	public boolean isKeyFrame() {
		return keyFrame;
	}
	
	public void setKeyFrame(boolean keyFrame) {
		this.keyFrame = keyFrame;
	}

	public Type getType() {
		return type;
	}
	
	public boolean hasFrame() {
		return hasFrame;
	}
	
}
