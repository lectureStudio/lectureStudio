package com.github.javaffmpeg;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.Loader;

import static org.bytedeco.javacpp.avcodec.avcodec_register_all;
import static org.bytedeco.javacpp.avdevice.avdevice_register_all;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.av_log_set_callback;

public class JavaFFmpeg {

	private final static Logger LOG = LogManager.getLogger(JavaFFmpeg.class.getName());
	
	/** Indicates whether the native libraries were loaded or not. */
	private static boolean loaded = false;

	/**
	 * FFmpeg log callback function. Must be kept in memory, otherwise the pointer
	 * is freed and causes a crash.
	 */
	private static LogCallback logCallback;


	public static void setLogCallback(LogCallback callback) {
		// TODO
		if (!OsInfo.isWindows())
			return;
		
		logCallback = callback;

		av_log_set_callback(logCallback);
	}

	public static void loadLibrary() {
		if (loaded)
			return;
		
		try {
			String platform = Loader.getPlatform();
			File nativeDir = new File("native/" + platform);

			Loader.setTempDir(nativeDir);

	        avcodec_register_all();
	        avdevice_register_all();
	        av_register_all();
	        
			setLogCallback(new LogCallback());
			
			loaded = true;
		}
		catch (Exception e) {
			LOG.error(e);
		}
	}
	
	public static File getDirectory() {
		return Loader.getTempDir();
	}

}
