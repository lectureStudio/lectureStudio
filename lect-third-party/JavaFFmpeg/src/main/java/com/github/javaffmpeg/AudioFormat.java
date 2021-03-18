package com.github.javaffmpeg;

public class AudioFormat {

	private int sampleRate;
	
	private int channels;
	
    private ChannelLayout channelLayout;
    
    private SampleFormat sampleFormat;
	
    
    public AudioFormat() {
    	this(null, null, 0, 0);
    }
    
    public AudioFormat(SampleFormat sampleFormat, ChannelLayout channelLayout, int channels, int sampleRate) {
    	this.sampleFormat = sampleFormat;
    	this.channelLayout = channelLayout;
    	this.channels = channels;
    	this.sampleRate = sampleRate;
    }

	/**
	 * @return the sampleRate
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the channels
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @param channels the channels to set
	 */
	public void setChannels(int channels) {
		this.channels = channels;
	}

	/**
	 * @return the channelLayout
	 */
	public ChannelLayout getChannelLayout() {
		return channelLayout;
	}

	/**
	 * @param channelLayout the channelLayout to set
	 */
	public void setChannelLayout(ChannelLayout channelLayout) {
		this.channelLayout = channelLayout;
	}

	/**
	 * @return the sampleFormat
	 */
	public SampleFormat getSampleFormat() {
		return sampleFormat;
	}

	/**
	 * @param sampleFormat the sampleFormat to set
	 */
	public void setSampleFormat(SampleFormat sampleFormat) {
		this.sampleFormat = sampleFormat;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final AudioFormat other = (AudioFormat) obj;
		
		return sampleFormat == other.sampleFormat && channelLayout == other.channelLayout &&
				channels == other.channels && sampleRate == other.sampleRate;
	}
	
	@Override
	public String toString() {
		return sampleFormat.toString() + " " + sampleRate + " " + channelLayout.toString() + " " + channels;
	}
    
}
