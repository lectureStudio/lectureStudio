package com.github.javaffmpeg;

public class AudioInputTest {

	static {
		JavaFFmpeg.loadLibrary();
	}
	
	public static void main(String[] args) {
		AudioInputTest t = new AudioInputTest();
		t.test();
	}

	private void test() {
		try {
			String name = "Internal Microphone (Conexant 2";
        	String input = "audio=" + name;
        	int sampleRate = 44100;
        	int channels = 1;
        	int msBuffer = 1000;
        	int frameSize = 2;
        	int bufferSize = (sampleRate * frameSize * channels * msBuffer) / (channels * 1000);
        	
        	Muxer muxer = new Muxer("muxer-out.wav");
			muxer.setAudioCodec(Codec.getEncoderById(CodecID.PCM_S16LE));
			muxer.setAudioChannels(channels);
			muxer.setChannelLayout(ChannelLayout.byChannelCount(channels));
			muxer.setSampleRate(sampleRate);
			muxer.setSampleFormat(SampleFormat.S16);
			muxer.open();
        	
        	AudioFormat format = new AudioFormat(SampleFormat.S16, ChannelLayout.MONO, channels, sampleRate);
			FFmpegAudioInputDevice device = new FFmpegAudioInputDevice(name, input, "dshow");
			device.setBufferMilliseconds(msBuffer);
			device.open(format);
			
			int i = 0;
			int runTime = 10;	// seconds
			
			while (device.isOpen()) {
				AudioFrame samples = device.getSamples();
				MediaPacket[] packets = muxer.addSamples(samples);
				
				for (MediaPacket packet : packets) {
					packet.clear();
				}
				samples.clear();
				
				// run for specified time in seconds
				if (i == runTime)
					device.close();
				
				i += (1000 / msBuffer);
			}
			
			muxer.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}