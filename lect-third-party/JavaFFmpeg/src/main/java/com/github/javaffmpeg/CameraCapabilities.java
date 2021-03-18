package com.github.javaffmpeg;

import org.bytedeco.javacpp.Pointer;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class CameraCapabilities {

    private static Pattern resPattern = Pattern.compile("(\\d{1,4})x(\\d{1,4})");

    private static Pattern fpsPattern = Pattern.compile("max .* fps=(\\d{1,4})");


	public static CameraFormat[] probeResolutions(String format, String option, String camera, String optionValue) {
		JavaFFmpeg.loadLibrary();
		
		final TreeSet<CameraFormat> set = new TreeSet<CameraFormat>();

		LogCallback callback = new LogCallback() {
			@Override
			public void call(Pointer source, int level, String formatStr, Pointer params) {
				byte[] bytes = new byte[1024];
                // fill the log message with parameters
				av_log_format_line(source, level, formatStr, params, bytes, bytes.length, new int[] {0});

                // parse one log line
				String message = new String(bytes).trim();
				Matcher resMatcher = resPattern.matcher(message);
				
                CameraFormat format = new CameraFormat();

                // get the maximum resolution
				while (resMatcher.find()) {
                    String[] parts = resMatcher.group(0).split("x");

                    format.setWidth(Integer.parseInt(parts[0]));
                    format.setHeight(Integer.parseInt(parts[1]));
                }

                // get maximum frames per second
                resMatcher = fpsPattern.matcher(message);

                if (resMatcher.find()) {
                    format.setMaxFPS(Integer.parseInt(resMatcher.group(1)));
                }

                if (format.isValid()) {
                    if (!set.add(format)) {
                        // if format already existing, then check for current max fps
                        Iterator<CameraFormat> iter = set.iterator();
                        while (iter.hasNext()) {
                            CameraFormat f = iter.next();
                            // if same resolution but higher fps, then replace format
                            if (f.compareTo(format) == 0 && f.getMaxFPS() < format.getMaxFPS()) {
                                iter.remove();
                                set.add(format);
                                break;
                            }
                        }
                    }
                }
			}
		};
		
		// set the new resolution parser callback
		JavaFFmpeg.setLogCallback(callback);

		AVFormatContext context = new AVFormatContext(null);
		AVDictionary dict = new AVDictionary(null);
		av_dict_set(dict, option, optionValue, 0);

		AVInputFormat inputFormat = av_find_input_format(format);
		avformat_open_input(context, camera, inputFormat, dict);

		if (context != null && !context.isNull())
			avformat_close_input(context);

		// restore to default log callback
		JavaFFmpeg.setLogCallback(new LogCallback());
		
		return set.toArray(new CameraFormat[0]);
	}

}
