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

import org.lecturestudio.core.net.rtp.RtpDepacketizer;
import org.lecturestudio.core.net.rtp.RtpPacketizer;
import org.lecturestudio.core.spi.ServiceProvider;

/**
 * Common interface to provide a consistent mechanism for audio codec providers.
 * As the name already implies, an audio codec provider provides audio codecs
 * and packetizers that form audio data into packets according to a specific
 * protocol.
 *
 * @author Alex Andres
 */
public interface AudioCodecProvider extends ServiceProvider {

	/**
	 * Get the audio encoder.
	 *
	 * @return the audio encoder.
	 */
	AudioEncoder getAudioEncoder();

	/**
	 * Get the audio decoder.
	 *
	 * @return the audio decoder.
	 */
	AudioDecoder getAudioDecoder();

	/**
	 * Get the RTP packetizer.
	 *
	 * @return the RTP packetizer.
	 */
	RtpPacketizer getRtpPacketizer();

	/**
	 * Get the RTP depacketizer.
	 *
	 * @return the RTP depacketizer.
	 */
	RtpDepacketizer getRtpDepacketizer();

}
