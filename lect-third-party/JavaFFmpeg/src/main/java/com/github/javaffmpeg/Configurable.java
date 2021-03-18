package com.github.javaffmpeg;

import java.util.ArrayList;
import java.util.List;

public abstract class Configurable {

	protected MediaType mediaType;

	protected PixelFormat pixelFormat;

	protected SampleFormat sampleFormat;
	
	protected ChannelLayout channelLayout;

	protected int imageWidth;
	protected int imageHeight;
	protected int gopSize;

	protected int audioChannels;

	protected int sampleRate;

	protected int bitrate;

	protected int profile;

	protected int audioBufferSize;
	
	protected double frameRate;

	protected double quality = -1;
	
	protected boolean twoPass;
	
	protected int pass;
	
	protected String profilePath;

	protected List<CodecFlag> flags = new ArrayList<CodecFlag>();


	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType type) {
		this.mediaType = type;
	}
	
	public void setImageWidth(int width) throws JavaFFmpegException {
		this.imageWidth = width;
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	public void setImageHeight(int height) throws JavaFFmpegException {
		this.imageHeight = height;
	}
	
	public int getImageHeight() {
		return imageHeight;
	}
	
	public void setGOPSize(int size) throws JavaFFmpegException {
		this.gopSize = size;
	}
	
	public int getGOPSize() {
		return this.gopSize;
	}
	
	public void setPixelFormat(PixelFormat pixelFormat) throws JavaFFmpegException {
		this.pixelFormat = pixelFormat;
	}
	
	public PixelFormat getPixelFormat() {
		return pixelFormat;
	}
	
	public void setSampleFormat(SampleFormat sampleFormat) throws JavaFFmpegException {
		this.sampleFormat = sampleFormat;
	}
	
	public SampleFormat getSampleFormat() {
		return sampleFormat;
	}
	
	public void setChannelLayout(ChannelLayout layout) {
		this.channelLayout = layout;
	}
	
	public ChannelLayout getChannelLayout() {
		return channelLayout;
	}
	
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
	
	public int getBitrate() {
		return bitrate;
	}
	
	public void setAudioBufferSize(int size) {
    	this.audioBufferSize = size;
    }
    
    public int getAudioBufferSize() {
    	return audioBufferSize;
    }
	
	public void setFramerate(double framerate) throws JavaFFmpegException {
		this.frameRate = framerate;
	}
	
	public double getFramerate() {
		return frameRate;
	}
	
	public void setSampleRate(int samplerate) throws JavaFFmpegException {
		this.sampleRate = samplerate;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public void setAudioChannels(int channels) throws JavaFFmpegException {
		this.audioChannels = channels;
	}
	
	public int getAudioChannels() {
		return audioChannels;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	public double getQuality() {
		return quality;
	}
	
	public void setTwoPass(boolean twoPass) {
		this.twoPass = twoPass;
	}

	public boolean getTwoPass() {
		return twoPass;
	}
	
	public void setProfilePath(String profilePath) {
		this.profilePath = profilePath;
	}

	public String getProfilePath() {
		return profilePath;
	}
	
	public void setPass(int pass) throws JavaFFmpegException {
		if (pass < 1 && pass > 2) {
			throw new JavaFFmpegException("Only pass 1 and pass 2 supported.");
		}
		
		this.pass = pass;
	}
	
	public int getPass() {
		return pass;
	}

	public void setProfile(int profile) {
		this.profile = bitrate;
	}

	public int getProfile() {
		return profile;
	}

	public void setFlag(CodecFlag flag) throws JavaFFmpegException {
		if (flag == null)
			throw new JavaFFmpegException("Could not set codec flag. Null provided.");

		flags.add(flag);
	}

}
