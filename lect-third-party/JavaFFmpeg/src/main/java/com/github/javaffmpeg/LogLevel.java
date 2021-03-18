package com.github.javaffmpeg;

import org.bytedeco.javacpp.avutil;

public enum LogLevel {

	QUIET   (avutil.AV_LOG_QUIET),
	/** Something went really wrong and we will crash now. */
	PANIC   (avutil.AV_LOG_PANIC),
	/** Something went wrong and recovery is not possible. */
	FATAL   (avutil.AV_LOG_FATAL),
	/** Something went wrong and cannot losslessly be recovered. */
	ERROR   (avutil.AV_LOG_ERROR),
	/** Something somehow does not look correct. */
	WARNING (avutil.AV_LOG_WARNING),
	INFO    (avutil.AV_LOG_INFO),
	VERBOSE (avutil.AV_LOG_VERBOSE),
	/** Stuff which is only useful for libav* developers. */
	DEBUG   (avutil.AV_LOG_DEBUG);


	private final int id;


	private LogLevel(int id) {
		this.id = id;
	}

	public final int value() {
		return id;
	}

	public static LogLevel byId(int id) {
		for (LogLevel value : values()) {
			if (value.id == id)
				return value;
		}

		return null;
	}

}
