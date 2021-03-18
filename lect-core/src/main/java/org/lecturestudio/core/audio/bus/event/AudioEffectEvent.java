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

package org.lecturestudio.core.audio.bus.event;

import org.lecturestudio.core.audio.effect.AudioEffect;
import org.lecturestudio.core.bus.event.BusEvent;
import org.lecturestudio.core.io.DynamicInputStream;

/**
 * This event is published when an {@link AudioEffect} state transition occurs
 * or the AudioEffect has made progress while processing.
 *
 * @author Alex Andres
 */
public class AudioEffectEvent extends BusEvent {

	/**
	 * The AudioEffect state type.
	 */
	public enum State {

		/** The AudioEffect has been initialized. */
		Initialized,

		/** The AudioEffect has been terminated and dropped all processed data. */
		Terminated,

		/** The AudioEffect has successfully finished the processing. */
		Finished,

		/** The AudioEffect has made progress while processing. */
		Progress,

		/** The event contains the processed result of the AudioEffect. */
		Result
	}


	/** The event ID. */
	private String id;

	/** The AudioEffect state. */
	private State state;

	/** The AudioEffect progress. */
	private float progress;

	/** The audio input stream that contains audio data to be processed. */
	private DynamicInputStream stream;


	/**
	 * Create an AudioEffectEvent instance with the specified ID and AudioEffect
	 * state.
	 *
	 * @param id    The event ID.
	 * @param state The AudioEffect state.
	 */
	public AudioEffectEvent(String id, State state) {
		this.id = id;
		this.state = state;
	}

	/**
	 * Get the event ID.
	 *
	 * @return the event ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the AudioEffect state.
	 *
	 * @param state The AudioEffect state.
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Get the AudioEffect state.
	 *
	 * @return the AudioEffect state.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Set the AudioEffect processing progress. The value must be in the range
	 * of [0,1].
	 *
	 * @param progress The progress of the AudioEffect.
	 */
	public void setProgress(float progress) {
		this.progress = progress;
	}

	/**
	 * Get the AudioEffect processing progress. The value is in the range of
	 * [0,1].
	 *
	 * @return AudioEffect progress.
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * Get the audio input stream that contains audio data to be processed.
	 *
	 * @return the audio input stream.
	 */
	public DynamicInputStream getInputStream() {
		return stream;
	}

	/**
	 * Set the audio input stream that contains audio data to be processed.
	 *
	 * @param stream The audio input stream to be processed.
	 */
	public void setInputStream(DynamicInputStream stream) {
		this.stream = stream;
	}

}
