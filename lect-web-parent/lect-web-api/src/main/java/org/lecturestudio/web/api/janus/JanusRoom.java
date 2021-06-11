/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.janus;

import java.math.BigInteger;
import java.util.List;

import javax.json.bind.annotation.JsonbTypeAdapter;

import org.lecturestudio.web.api.janus.json.CommaSeparatedListAdapter;

/**
 * A room created on the Janus WebRTC server that participants can join for
 * conferences or webinars.
 *
 * @author Alex Andres
 *
 * @apiNote https://janus.conf.meetecho.com/docs/videoroom.html
 */
public class JanusRoom {

	private BigInteger room;

	private String description;

	private boolean pin_required;

	private int max_publishers;

	private int bitrate;

	private boolean bitrate_cap;

	private int fir_freq;

	@JsonbTypeAdapter(CommaSeparatedListAdapter.class)
	private List<String> audiocodec;

	@JsonbTypeAdapter(CommaSeparatedListAdapter.class)
	private List<String> videocodec;

	private boolean record;

	private String record_dir;

	private boolean lock_record;

	private int num_participants;


	/**
	 * Get the unique numeric room ID.
	 *
	 * @return The unique room ID.
	 */
	public BigInteger getRoomId() {
		return room;
	}

	/**
	 * Get the user friendly room description.
	 *
	 * @return The room description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Check whether a PIN is required to join this room.
	 *
	 * @return True if a PIN is required to join this room.
	 */
	public boolean isPinRequired() {
		return pin_required;
	}

	/**
	 * Get how many publishers can publish via WebRTC at the same time.
	 *
	 * @return The maximum publishers.
	 */
	public int getMaxPublishers() {
		return max_publishers;
	}

	/**
	 * Get bitrate cap that should be forced (via Receiver Estimated Maximum
	 * Bitrate) on all publishers by default.
	 *
	 * @return The bitrate cap forced on all publishers.
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * Check whether the bitrate cap should act as a limit to dynamic bitrate
	 * changes by publishers.
	 *
	 * @return True if bitrate cap is enabled.
	 */
	public boolean isBitrateCap() {
		return bitrate_cap;
	}

	/**
	 * Get how often a keyframe request is sent via Full Intra Request (FIR),
	 * Picture Loss Indication (PLI) to active publishers.
	 *
	 * @return The frequency of keyframe requests.
	 */
	public int getFirFreq() {
		return fir_freq;
	}

	/**
	 * Get a list of allowed audio codecs.
	 *
	 * @return All allowed audio codecs in this room.
	 */
	public List<String> getAllowedAudioCodecs() {
		return audiocodec;
	}

	/**
	 * Get a list of allowed video codecs.
	 *
	 * @return All allowed video codecs in this room.
	 */
	public List<String> getAllowedVideoCodecs() {
		return videocodec;
	}

	/**
	 * Check whether this room is being recorded.
	 *
	 * @return True if this room is being recorded.
	 */
	public boolean isRecorded() {
		return record;
	}

	/**
	 * Get the path where the .mjr files are being saved. This is only valid if
	 * this room is being recorded.
	 *
	 * @return The path of recording files for this room.
	 */
	public String getRecordingDir() {
		return record_dir;
	}

	/**
	 * Check whether the room recording state can only be changed providing the
	 * secret.
	 *
	 * @return True if a secret is required to change the recording state.
	 */
	public boolean isRecordingLocked() {
		return lock_record;
	}

	/**
	 * Get the maximum count of publishers for this room, active or not.
	 *
	 * @return The maximum count of publishers.
	 */
	public int getPublisherCount() {
		return num_participants;
	}
}
