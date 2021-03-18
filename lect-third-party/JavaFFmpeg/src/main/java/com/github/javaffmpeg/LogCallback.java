package com.github.javaffmpeg;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.avutil.Callback_Pointer_int_String_Pointer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.bytedeco.javacpp.avutil.av_log_format_line;

public class LogCallback extends Callback_Pointer_int_String_Pointer {

	private final static Logger LOGGER = LogManager.getLogger(LogCallback.class.getName());

	private boolean printPrefix = true;

	private LogLevel logLevel = LogLevel.INFO;


	@Override
	public void call(Pointer source, int level, String formatStr, Pointer params) {
		if (logLevel.value() < level)
			return;

		byte[] bytes = new byte[1024];
		int prefix = printPrefix ? 1 : 0;

		av_log_format_line(source, level, formatStr, params, bytes, bytes.length, new int[] {prefix});

		String message = new String(bytes).trim();

		switch (logLevel) {
			case PANIC:
			case FATAL:
			case ERROR:
				LOGGER.error(message);  break;
			case WARNING:
				LOGGER.warn(message);   break;
			case INFO:
				LOGGER.info(message);   break;
			case VERBOSE:
			case DEBUG:
				LOGGER.debug(message);  break;
			default:
				break;
		}
	}

	public void setPrintPrefix(boolean printPrefix) {
		this.printPrefix = printPrefix;
	}

	public void setLogLevel(LogLevel level) {
		this.logLevel = level;
	}

}
