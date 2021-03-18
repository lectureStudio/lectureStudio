package com.github.javaffmpeg;

import java.awt.image.BufferedImage;

public class DemuxerTest {

	public static void main(String[] args) throws Exception {
		JavaFFmpeg.loadLibrary();
		
		DemuxerTest t = new DemuxerTest();
		t.testFileInput();
//		t.testStreamInput();
	}

	public void testFileInput() throws Exception {
		String video = "src/test/resources/bunny.mov";

		CanvasFrame frame = new CanvasFrame("Camera Test");
		frame.setSize(640, 480);

		Demuxer demuxer = new Demuxer();
		demuxer.open(video);
			
		MediaFrame mediaFrame;
		while ((mediaFrame = demuxer.readFrame()) != null) {
			if (mediaFrame.getType() == MediaFrame.Type.VIDEO) {
				VideoFrame videoFrame = (VideoFrame) mediaFrame;

				frame.showImage(Image.createImage(videoFrame.getData(), videoFrame.getWidth(), videoFrame.getHeight(),
				        BufferedImage.TYPE_3BYTE_BGR));

				Thread.sleep((long) (1000 / (demuxer.getFrameRate())));
			}
			if (mediaFrame.getType() == MediaFrame.Type.AUDIO) {
				//AudioFrame audioFrame = (AudioFrame) mediaFrame;
				//Audio.getAudio16(audioFrame);
			}
		}
			
		demuxer.close();
		frame.dispose();
	}
	
	public void testStreamInput() throws Exception {
		RandomAccessStream stream = new RandomAccessFileStream("src/test/resources/Louna.wav", "r");
		
		Muxer muxer = new Muxer("src/test/resources/out.mp3");
		muxer.setAudioCodec(Codec.getEncoderById(CodecID.MP3));
		muxer.setAudioBitrate(128000);
		muxer.setSampleRate(44100);
		muxer.setAudioChannels(2);
		muxer.open();
		
		
		Demuxer demuxer = new Demuxer();
		demuxer.open(stream);
		
		MediaFrame mediaFrame;
		while ((mediaFrame = demuxer.readFrame()) != null) {
			if (mediaFrame.getType() == MediaFrame.Type.VIDEO) {
				//VideoFrame videoFrame = (VideoFrame) mediaFrame;
				
			}
			if (mediaFrame.getType() == MediaFrame.Type.AUDIO) {
				AudioFrame audioFrame = (AudioFrame) mediaFrame;
				//Audio.getAudio16(audioFrame);
				
				muxer.addSamples(audioFrame);
			}
		}
		
		demuxer.close();
		muxer.close();
	}
	
}