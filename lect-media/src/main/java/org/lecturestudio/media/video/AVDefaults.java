/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.media.video;

import org.lecturestudio.core.codec.CodecID;
import org.lecturestudio.core.geometry.Dimension2D;

public class AVDefaults {

	public static VideoFormat[] PLR_OUTPUT_PROFILES = {
			new VideoFormat(CodecID.MP3, "mp3"),
			new VideoFormat(CodecID.VORBIS, "ogg"),
			new VideoFormat(CodecID.WAV, "wav")
	};

	public static VideoFormat[] VIDEO_FORMATS = {
			new VideoFormat(CodecID.H264, CodecID.AAC, "mp4"),
			new VideoFormat(CodecID.H264_NVIDIA, CodecID.AAC, "mp4"),
			new VideoFormat(CodecID.H264, CodecID.MP3, "avi"),
			new VideoFormat(CodecID.H264, CodecID.MP3, "mp4"),
			new VideoFormat(CodecID.VP9, CodecID.OPUS, "webm")
	};

	public static Integer[] FRAME_RATES = {
			10, 15, 20, 25, 30
	};

	public static Integer[] AUDIO_BITRATES = {
			12, 16, 20, 24, 32, 48, 64, 96, 128, 192
	};

	public static Integer[] SAMPLE_RATES = {
			8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 96000
	};

	public static Integer[] OPUS_SAMPLE_RATES = {
			8000, 12000, 16000, 24000, 32000, 48000
	};

	public static Dimension2D[] PICTURE_4_3_FORMAT = {
			new Dimension2D(480, 360),
			new Dimension2D(640, 480),
			new Dimension2D(960, 720),
			new Dimension2D(1024, 768),
			new Dimension2D(1280, 960),
			new Dimension2D(1400, 1050),
			new Dimension2D(1600, 1200),
			new Dimension2D(1920, 1440),
			new Dimension2D(2560, 1920)
	};

	public static Dimension2D[] PICTURE_16_9_FORMAT = {
			new Dimension2D(480, 272),
			new Dimension2D(640, 360),
			new Dimension2D(960, 540),
			new Dimension2D(1024, 576),
			new Dimension2D(1280, 720),
			new Dimension2D(1366, 768),
			new Dimension2D(1600, 900),
			new Dimension2D(1920, 1080),
			new Dimension2D(2560, 1440)
	};

	public static Dimension2D[] PICTURE_16_10_FORMAT = {
			new Dimension2D(480, 300),
			new Dimension2D(640, 400),
			new Dimension2D(960, 600),
			new Dimension2D(1120, 700),
			new Dimension2D(1280, 800),
			new Dimension2D(1440, 900),
			new Dimension2D(1680, 1050),
			new Dimension2D(1920, 1200),
			new Dimension2D(2560, 1600)
	};

	public static Dimension2D[] getDimensions(double width, double height) {
		double ratio = width / height;

		// Allow some ratio margin, since not all slides have the exact ratio.
		if (Double.compare(ratio, 1.0) < 0) {
			int nearestH = nearest(height, 10);
			int nearestW = (int) (nearestH * ratio);
			int inc = 100;

			Dimension2D[] formats = new Dimension2D[9];
			formats[0] = new Dimension2D(nearestW, nearestH);

			for (int i = 1; i < formats.length; i++) {
				int incStep = inc * i;
				int w = nearest(nearestW + incStep * ratio, 10);
				int h = nearestH + incStep;

				formats[i] = new Dimension2D(w, h);
			}

			return formats;
		}
		else if (Double.compare(ratio, 1.5) < 0) {
			return PICTURE_4_3_FORMAT;
		}
		else if (Double.compare(ratio, 1.7) < 0) {
			return PICTURE_16_10_FORMAT;
		}
		else if (Double.compare(ratio, 1.9) < 0) {
			return PICTURE_16_9_FORMAT;
		}

		return null;
	}

	private static int nearest(double i, int v) {
		return (int) (Math.round(i / v) * v);
	}

	private AVDefaults() {}
}
