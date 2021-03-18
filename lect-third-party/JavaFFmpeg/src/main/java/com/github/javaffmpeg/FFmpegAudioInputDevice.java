package com.github.javaffmpeg;

import java.util.concurrent.atomic.AtomicBoolean;

public class FFmpegAudioInputDevice {

	/** Represents the current device state: operating or not. */
    private final AtomicBoolean open = new AtomicBoolean(false);
    
    private Demuxer demuxer;

    private AudioFormat audioFormat;
    
    private String name;
    
    private String device;
    
    private String format;
    
    private int bufferMs;
    
    
    public FFmpegAudioInputDevice(String name, String device, String format) {
    	this.name = name;
        this.device = device;
        this.format = format;
    }
    
    public void open(AudioFormat audioFormat) throws JavaFFmpegException {
        if (!open.get()) {
            demuxer = new Demuxer();
            demuxer.setInputFormat(format);
            demuxer.setAudioBufferSize(bufferMs);
            demuxer.setAudioChannels(audioFormat.getChannels());
            demuxer.setSampleFormat(audioFormat.getSampleFormat());
            demuxer.setSampleRate(audioFormat.getSampleRate());
            demuxer.open(device);

            setAudioFormat(audioFormat);
            
            open.set(true);
        }
    }
    
    public void close() {
        if (open.compareAndSet(true, false)) {
        	if (demuxer != null)
        		demuxer.close();
        }
    }
    
    public boolean isOpen() {
        return open.get();
    }
	
    public String getName() {
        return name;
    }
    
    public void setAudioFormat(AudioFormat format) {
    	this.audioFormat = format;
    }
    
    public AudioFormat getAudioFormat() {
    	return audioFormat;
    }
    
    public void setBufferMilliseconds(int ms) {
    	this.bufferMs = ms;
    }
    
    public int getBufferSize() {
    	int sampleRate = audioFormat.getSampleRate();
    	int channels = audioFormat.getChannels();
    	int frameSize = Audio.getFormatDepth(audioFormat.getSampleFormat());
    	int size = (sampleRate * frameSize * channels * bufferMs) / (channels * 1000);
    	
    	return size;
    }
    
    public AudioFrame getSamples() {
        if (open.get()) {
            try {
                MediaFrame mediaFrame = demuxer.readFrame();
                
                if (mediaFrame == null)
                	return null;
                
                if (mediaFrame.getType() == MediaFrame.Type.AUDIO) {
                    AudioFrame audioFrame = (AudioFrame) mediaFrame;
                    return audioFrame;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    
}
