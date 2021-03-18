package com.github.javaffmpeg;

import static org.bytedeco.javacpp.avformat.av_find_input_format;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avformat.avformat_open_input;
import static org.bytedeco.javacpp.avutil.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;

import org.bytedeco.javacpp.avdevice;
import org.bytedeco.javacpp.avdevice.AVDeviceInfoList;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacpp.avformat.AVInputFormat;
import org.bytedeco.javacpp.avformat.AVOutputFormat;
import org.bytedeco.javacpp.avutil.AVDictionary;

/**
 * Unit test for cameras.
 */
public class CameraProbeTest extends TestCase {

	static {
		JavaFFmpeg.loadLibrary();
	}

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public CameraProbeTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(CameraProbeTest.class);
	}

	/**
	 * Test camera capabilities.
	 */
	public void testCameraFormats() throws JavaFFmpegException {
		String format = "dshow";
		
		AVInputFormat inputFormat = av_find_input_format(format);
		AVFormatContext context = avformat.avformat_alloc_context();
		
		avformat.avformat_open_input(context, format, inputFormat, null);
		
		AVDeviceInfoList device_list = new AVDeviceInfoList(null);
		

		if (context != null)
		{
			avdevice.avdevice_list_devices(context, device_list);
			
			System.out.println(device_list.nb_devices());
			
			avformat_close_input(context);
		}
	}

}
