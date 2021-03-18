package com.github.javaffmpeg;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for codecs.
 */
public class CodecTest extends TestCase {

	static {
		JavaFFmpeg.loadLibrary();
	}

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public CodecTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(CodecTest.class);
	}

	/**
	 * Test codec capabilities.
	 */
	public void testCodec() throws JavaFFmpegException {
		Codec.getDecoderById(CodecID.MJPEG);
		Codec.getDecoderById(CodecID.RAWVIDEO);
		Codec.getDecoderById(CodecID.VORBIS);
		Codec.getDecoderById(CodecID.OPUS);
		Codec.getDecoderById(CodecID.MP3);
		Codec.getDecoderById(CodecID.H264);
		Codec.getDecoderById(CodecID.HEVC);
		Codec.getDecoderById(CodecID.VP8);
		Codec.getDecoderById(CodecID.VP9);
		
		Codec.getEncoderById(CodecID.RAWVIDEO);
		Codec.getEncoderById(CodecID.VORBIS);
		Codec.getEncoderById(CodecID.OPUS);
		Codec.getEncoderById(CodecID.MP3);
		Codec.getEncoderById(CodecID.H264);
		Codec.getEncoderById(CodecID.HEVC);
		Codec.getEncoderById(CodecID.VP8);
		Codec.getEncoderById(CodecID.VP9);
	}

}
