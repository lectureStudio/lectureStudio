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

import static java.util.Objects.nonNull;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import dev.onvoid.webrtc.media.video.VideoFrame;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;

/**
 * Context class that maintains the state of a Janus participant's media streams.
 * This class provides properties to track whether audio and video streams are active
 * and supports property change listeners for reactive UI updates.
 *
 * @author Alex Andres
 */
public class JanusParticipantContext {

	/** The unique identifier of the participant in the Janus system. */
	private final ObjectProperty<BigInteger> peerId;

	/** Property indicating whether audio stream is active. */
	private final BooleanProperty audioActive;

	/** Property indicating whether video stream is active. */
	private final BooleanProperty videoActive;

	/** Property indicating whether screen sharing is active. */
	private final BooleanProperty screenActive;

	/** Property for the display name of the participant. */
	private final StringProperty displayName;

	/** The unique request identifier associated with this participant's session. */
	private UUID requestId;

	/** Consumer for processing video frames received from the participant. */
	private Consumer<VideoFrame> videoFrameConsumer;

	/** Consumer for processing talking state changes of the participant. */
	private Consumer<Boolean> talkingActivityConsumer;

	/** The timestamp (in nanoseconds) of the last detected talking activity for this participant. */
	private long lastTalkingActivityTimestamp;


	/**
	 * Creates a new JanusParticipantContext with default values.
	 * By default, all media streams are inactive.
	 */
	public JanusParticipantContext() {
		peerId = new ObjectProperty<>();
		audioActive = new BooleanProperty(false);
		videoActive = new BooleanProperty(false);
		screenActive = new BooleanProperty(false);
		displayName = new StringProperty("");
		talkingActivityConsumer = talking -> {
			if (talking) {
				lastTalkingActivityTimestamp = System.nanoTime();
			}
		};
	}

	/**
	 * Gets the unique identifier of the participant.
	 *
	 * @return The peer ID.
	 */
	public BigInteger getPeerId() {
		return peerId.get();
	}

	/**
	 * Sets the unique identifier of the participant.
	 *
	 * @param peerId The peer ID to set.
	 */
	public void setPeerId(BigInteger peerId) {
		this.peerId.set(peerId);
	}

	/**
	 * Gets the peer ID property for binding.
	 *
	 * @return The peer ID property.
	 */
	public ObjectProperty<BigInteger> peerIdProperty() {
		return peerId;
	}

	/**
	 * Gets the unique request identifier of the participant.
	 *
	 * @return The request ID.
	 */
	public UUID getRequestId() {
		return requestId;
	}

	/**
	 * Sets the unique request identifier of the participant.
	 *
	 * @param requestId The request ID to set.
	 */
	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	/**
	 * Gets whether the audio stream is active.
	 *
	 * @return True if audio stream is active, false otherwise.
	 */
	public boolean isAudioActive() {
		return audioActive.get();
	}

	/**
	 * Sets whether the audio stream is active.
	 *
	 * @param active True to set audio stream active, false to set inactive.
	 */
	public void setAudioActive(boolean active) {
		this.audioActive.set(active);
	}

	/**
	 * Gets the audio active property for binding.
	 *
	 * @return The audio active property.
	 */
	public BooleanProperty audioActiveProperty() {
		return audioActive;
	}

	/**
	 * Gets whether the video stream is active.
	 *
	 * @return True if video stream is active, false otherwise.
	 */
	public boolean isVideoActive() {
		return videoActive.get();
	}

	/**
	 * Sets whether the video stream is active.
	 *
	 * @param active True to set video stream active, false to set inactive.
	 */
	public void setVideoActive(boolean active) {
		this.videoActive.set(active);
	}

	/**
	 * Gets the video active property for binding.
	 *
	 * @return The video active property.
	 */
	public BooleanProperty videoActiveProperty() {
		return videoActive;
	}

	/**
	 * Gets whether screen sharing is active.
	 *
	 * @return True if screen sharing is active, false otherwise.
	 */
	public boolean isScreenActive() {
		return screenActive.get();
	}

	/**
	 * Sets whether screen sharing is active.
	 *
	 * @param active True to set screen sharing active, false to set inactive.
	 */
	public void setScreenActive(boolean active) {
		this.screenActive.set(active);
	}

	/**
	 * Gets the screen active property for binding.
	 *
	 * @return The screen active property.
	 */
	public BooleanProperty screenActiveProperty() {
		return screenActive;
	}

	/**
	 * Gets the display name of the participant.
	 *
	 * @return The display name.
	 */
	public String getDisplayName() {
		return displayName.get();
	}

	/**
	 * Sets the display name of the participant.
	 *
	 * @param name The display name to set.
	 */
	public void setDisplayName(String name) {
		this.displayName.set(name);
	}

	/**
	 * Gets the display name property for binding.
	 *
	 * @return The display name property.
	 */
	public StringProperty displayNameProperty() {
		return displayName;
	}

	/**
	 * Gets the consumer for received video frames.
	 *
	 * @return The consumer that processes incoming video frames.
	 */
	public Consumer<VideoFrame> getVideoFrameConsumer() {
		return videoFrameConsumer;
	}

	/**
	 * Sets the consumer for received video frames. This consumer is called whenever a new video frame is received from
	 * this participant.
	 *
	 * @param consumer The consumer that will process incoming video frames.
	 */
	public void setVideoFrameConsumer(Consumer<VideoFrame> consumer) {
		videoFrameConsumer = consumer;
	}

	/**
	 * Processes a received video frame by passing it to the registered video frame consumer.
	 * If no consumer is registered, this method does nothing.
	 *
	 * @param frame The video frame to process.
	 */
	public void setVideoFrame(VideoFrame frame) {
		if (nonNull(videoFrameConsumer)) {
			videoFrameConsumer.accept(frame);
		}
	}

	/**
	 * Gets the consumer for talking state changes.
	 *
	 * @return The consumer that processes talking state changes.
	 */
	public Consumer<Boolean> getTalkingActivityConsumer() {
		return talkingActivityConsumer;
	}

	/**
	 * Sets the consumer for talking state changes. This consumer is called when the participant's talking state
	 * changes.
	 *
	 * @param consumer The consumer that will process talking state changes.
	 */
	public void setTalkingActivityConsumer(Consumer<Boolean> consumer) {
		talkingActivityConsumer = talkingActivityConsumer.andThen(consumer);
	}

	/**
	 * Sets the talking state of the participant and notifies the registered consumer.
	 * This method triggers the talking activity consumer if one has been set.
	 *
	 * @param talking True if the participant is currently talking, false otherwise.
	 */
	public void setTalking(boolean talking) {
		if (nonNull(talkingActivityConsumer)) {
			talkingActivityConsumer.accept(talking);
		}
	}

	/**
	 * Gets the timestamp of the last detected talking activity for this participant.
	 *
	 * @return The timestamp in nanoseconds when the participant was last talking.
	 */
	public long getTalkingActivityTimestamp() {
		return lastTalkingActivityTimestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JanusParticipantContext that = (JanusParticipantContext) o;

		return Objects.equals(peerId, that.peerId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(peerId);
	}

	@Override
	public String toString() {
		return "JanusParticipantContext{" + "peerId=" + peerId +
				", requestId=" + requestId +
				", audioActive=" + audioActive +
				", videoActive=" + videoActive +
				", screenActive=" + screenActive +
				", displayName=" + displayName +
				'}';
	}
}