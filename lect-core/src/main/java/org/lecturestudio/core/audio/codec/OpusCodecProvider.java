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

package org.lecturestudio.core.audio.codec;

import com.github.javaffmpeg.CodecID;

import org.lecturestudio.core.audio.codec.ffmpeg.FFmpegAudioDecoder;
import org.lecturestudio.core.audio.codec.ffmpeg.FFmpegAudioEncoder;
import org.lecturestudio.core.audio.codec.ffmpeg.FFmpegRtpDepacketizer;
import org.lecturestudio.core.audio.codec.ffmpeg.FFmpegRtpPacketizer;
import org.lecturestudio.core.net.rtp.RtpDepacketizer;
import org.lecturestudio.core.net.rtp.RtpPacketizer;

/**
 * OPUS audio codec provider implementation.
 *
 * @link http://opus-codec.org
 *
 * @author Alex Andres
 */
public class OpusCodecProvider implements AudioCodecProvider {

	@Override
	public AudioEncoder getAudioEncoder() {
		return new FFmpegAudioEncoder(CodecID.OPUS);
	}

	@Override
	public AudioDecoder getAudioDecoder() {
		return new FFmpegAudioDecoder(CodecID.OPUS);
	}

	@Override
	public RtpPacketizer getRtpPacketizer() {
		return new FFmpegRtpPacketizer();
	}

	@Override
	public RtpDepacketizer getRtpDepacketizer() {
		return new FFmpegRtpDepacketizer();
	}

	@Override
	public String getProviderName() {
		return "OPUS";
	}

}
