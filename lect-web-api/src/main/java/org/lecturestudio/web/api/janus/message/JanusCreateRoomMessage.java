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

package org.lecturestudio.web.api.janus.message;

import java.util.List;

/**
 * Request container used to create a new video room.
 *
 * @author Alex Andres
 */
public class JanusCreateRoomMessage extends JanusRoomRequest {

	private Integer publishers;

	private Integer bitrate;

	private Boolean permanent;

	private Boolean is_private;

	private Boolean audiolevel_event;

	private Integer audio_active_packets;

	private Integer audio_level_average;

	private Boolean notify_joining;

	private Boolean record;

	private String description;

	private String secret;

	private String pin;

	private List<String> allowed;


	/**
	 * Create a new {@code JanusCreateRoomRequest}.
	 */
	public JanusCreateRoomMessage() {
		setRequestType(JanusRoomRequestType.CREATE);
	}

	/**
	 * Get the maximum number of concurrent senders.
	 *
	 * @return The maximum number senders.
	 */
	public int getPublishers() {
		return publishers;
	}

	/**
	 * Set the maximum number of concurrent senders, e.g. 6 for a video
	 * conference or 1 for a webinar.
	 *
	 * @param publishers The maximum number senders.
	 */
	public void setPublishers(int publishers) {
		this.publishers = publishers;
	}

	/**
	 * Get the maximum video bitrate for senders.
	 *
	 * @return The maximum video bitrate for senders.
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * Get the maximum video bitrate for senders, e.g. 128000.
	 *
	 * @param bitrate The maximum video bitrate for senders.
	 */
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	/**
	 * Whether the room should be saved in the config file.
	 *
	 * @return True to save the room.
	 */
	public boolean isPermanent() {
		return permanent;
	}

	/**
	 * Set whether the room should be saved in the config file. {@code False} by
	 * default.
	 *
	 * @param permanent True to save the room.
	 */
	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	/**
	 * Get the pretty description of the room.
	 *
	 * @return The pretty description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the pretty description of the room. This setting is optional.
	 *
	 * @param description The pretty description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the password required to edit/destroy the room.
	 *
	 * @return The required password to edit/destroy the room.
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Set the password required to edit/destroy the room. This setting is
	 * optional.
	 *
	 * @param secret The required password to edit/destroy the room.
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * Get the password required to join the room.
	 *
	 * @return The password of the room.
	 */
	public String getPin() {
		return pin;
	}

	/**
	 * Set the password required to join the room. This setting is optional.
	 *
	 * @param pin The password of the room.
	 */
	public void setPin(String pin) {
		this.pin = pin;
	}

	/**
	 * Get whether emitting an event to other users is enabled or not, default=false.
	 *
	 * @return True to emit the audio level event.
	 */
	public Boolean getAudioLevelEvent() {
		return audiolevel_event;
	}

	/**
	 * Set whether to emit an event to other users or not.
	 *
	 * @param enableEvent True to emit the audio level event.
	 */
	public void setAudioLevelEvent(Boolean enableEvent) {
		audiolevel_event = enableEvent;
	}

	/**
	 * Get the number of packets to consider a user active, default=100, 2 seconds.
	 *
	 * @return The number of packets to consider a user active.
	 */
	public Integer getAudioActivePackets() {
		return audio_active_packets;
	}

	/**
	 * Set the number of packets to consider a user active.
	 *
	 * @param packets The number of packets to consider a user active.
	 */
	public void setAudioActivePackets(Integer packets) {
		audio_active_packets = packets;
	}

	/**
	 * Get the average value of audio level to consider a user active, 127=muted, 0='too loud', default=25.
	 *
	 * @return The average value of audio level to consider a user active.
	 */
	public Integer getAudioLevelAverage() {
		return audio_level_average;
	}

	/**
	 * Set the average value of audio level to consider a user active.
	 *
	 * @param levelAverage The average audio level to consider a user active.
	 */
	public void setAudioLevelAverage(Integer levelAverage) {
		audio_level_average = levelAverage;
	}

	/**
	 * Whether the room should appear in a room list request.
	 *
	 * @return True to hide the room in the room listing.
	 */
	public boolean isPrivate() {
		return is_private;
	}

	/**
	 * Set whether the room should appear in a room list request.
	 *
	 * @param isPrivate True to hide the room in the room listing.
	 */
	public void setIsPrivate(boolean isPrivate) {
		this.is_private = isPrivate;
	}

	/**
	 * Check whether this room should be recorded.
	 *
	 * @return True if this room should be recorded.
	 */
	public boolean isRecord() {
		return record;
	}

	/**
	 * Set whether this room should be recorded.
	 *
	 * @param record True if this room should be recorded.
	 */
	public void setRecord(boolean record) {
		this.record = record;
	}

	/**
	 * Get a list of string tokens users can use to join the room.
	 *
	 * @return A list of string tokens that allow to join the room.
	 */
	public List<String> getAllowed() {
		return allowed;
	}

	/**
	 * Set a list of string tokens users can use to join the room. This setting
	 * is optional.
	 *
	 * @param allowed A list of string tokens that allow to join the room.
	 */
	public void setAllowed(List<String> allowed) {
		this.allowed = allowed;
	}

	/**
	 * Check whether to notify all participants when a new participant joins the
	 * room.
	 *
	 * @return True to notify.
	 */
	public Boolean getNotifyJoining() {
		return notify_joining;
	}

	/**
	 * Set whether to notify all participants when a new participant joins the
	 * room.
	 *
	 * @param notify True to notify.
	 */
	public void setNotifyJoining(Boolean notify) {
		notify_joining = notify;
	}
}
