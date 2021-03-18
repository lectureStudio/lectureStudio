package com.github.javaffmpeg;

public class MuxerTest {

	public static void main(String[] args) throws Exception {
		MuxerTest t = new MuxerTest();
		t.testTranscoding();
	}

	public void testTranscoding() throws Exception {
		CodecID videoID = CodecID.H264;
		CodecID audioID = CodecID.MP3;
		
		Options videoOptions = new Options();
		Options audioOptions = new Options();

		Muxer muxer = new Muxer("src/test/resources/out.avi");
		muxer.setVideoCodec(Codec.getEncoderById(videoID));
		muxer.setAudioCodec(Codec.getEncoderById(audioID));
		muxer.setImageWidth(1280);
		muxer.setImageHeight(720);
		muxer.setPixelFormat(PixelFormat.YUV420P);
		muxer.setVideoBitrate(2000000);
		muxer.setFramerate(25);
		muxer.setAudioBitrate(128000);
		muxer.setSampleRate(22050);
		muxer.setAudioChannels(2);
		
		if (videoID == CodecID.VP9) {
			videoOptions.put("profile", "0");
		}

		if (audioID == CodecID.OPUS) {
			audioOptions.put("strict", "experimental");
			muxer.setAudioQuality(0);	// OPUS: Quality-based encoding not supported
		}
		
		muxer.setVideoOptions(videoOptions);
		muxer.setAudioOptions(audioOptions);

		muxer.open();

		Demuxer demuxer = new Demuxer();
		demuxer.open("src/test/resources/bunny.mov");

		MediaFrame mediaFrame;
		while ((mediaFrame = demuxer.readFrame()) != null) {
			if (mediaFrame.getType() == MediaFrame.Type.VIDEO) {
				VideoFrame frame = (VideoFrame) mediaFrame;
				muxer.addImage(frame);
			}
			if (mediaFrame.getType() == MediaFrame.Type.AUDIO) {
				AudioFrame frame = (AudioFrame) mediaFrame;
				muxer.addSamples(frame);
			}
		}

		demuxer.close();
		muxer.close();
	}
	
}