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

import static java.util.Objects.nonNull;

import java.util.Objects;

import org.lecturestudio.core.codec.CodecID;

public class VideoFormat {

	private final CodecID videoCodecID;
	private final CodecID audioCodecID;

	private final String format;


	public VideoFormat(CodecID audioCodecID, String format) {
		this(null, audioCodecID, format);
	}

	public VideoFormat(CodecID videoCodecID, CodecID audioCodecID, String format) {
		this.videoCodecID = videoCodecID;
		this.audioCodecID = audioCodecID;
		this.format = format;
	}

	public CodecID getVideoCodecID() {
		return videoCodecID;
	}

	public CodecID getAudioCodecID() {
		return audioCodecID;
	}

	public String getOutputFormat() {
		return format;
	}

	@Override
	public VideoFormat clone() {
		VideoFormat clone;

		try {
			clone = (VideoFormat) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}

		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VideoFormat that = (VideoFormat) o;

		return videoCodecID == that.videoCodecID &&
				audioCodecID == that.audioCodecID &&
				Objects.equals(format, that.format);
	}

	@Override
	public int hashCode() {
		return Objects.hash(videoCodecID, audioCodecID, format);
	}

	@Override
	public String toString() {
		if (nonNull(videoCodecID) && nonNull(audioCodecID)) {
			return String.format("%s: %s + %s", format.toUpperCase(), videoCodecID.name(), audioCodecID.name());
		}
		if (nonNull(videoCodecID)) {
			return String.format("%s: %s", format.toUpperCase(), videoCodecID.name());
		}
		if (nonNull(audioCodecID)) {
			return String.format("%s: %s", format.toUpperCase(), audioCodecID.name());
		}
		return "Empty video format";
	}
}
