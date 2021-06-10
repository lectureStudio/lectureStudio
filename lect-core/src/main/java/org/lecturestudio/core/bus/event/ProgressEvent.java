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
package org.lecturestudio.core.bus.event;

public class ProgressEvent extends BusEvent {

	/** Enum with the {@link ProgressEvent} states. */
	private enum State { STARTED, RUNNING, FINISHED }

	/** The {@link State} of the {@link ProgressEvent}. */
	private State state;

	/** The progress of the {@link ProgressEvent}. */
	private float progress;


	/**
	 * Create the {@link ProgressEvent}. (Calls {@link #setProgress(float)} with 0 as parameter)
	 */
	public ProgressEvent() {
		setProgress(0);
	}

	/**
	 * Create the {@link ProgressEvent} with specified progress.
	 * (Calls {@link #setProgress(float)} with progress as parameter)
	 *
	 * @param progress The progress.
	 */
	public ProgressEvent(float progress) {
		setProgress(progress);
	}

	/**
	 * Indicates whether the {@link ProgressEvent} is started.
	 *
	 * @return {@code true} if the {@link #state} equals {@code State.STARTED}, otherwise {@code false}.
	 */
	public boolean started() {
		return state == State.STARTED;
	}

	/**
	 * Indicates whether the {@link ProgressEvent} is running.
	 *
	 * @return {@code true} if the {@link #state} equals {@code State.RUNNING}, otherwise {@code false}.
	 */
	public boolean running() {
		return state == State.RUNNING;
	}

	/**
	 * Indicates whether the {@link ProgressEvent} is finished.
	 *
	 * @return {@code true} if the {@link #state} equals {@code State.FINISHED}, otherwise {@code false}.
	 */
	public boolean finished() {
		return state == State.FINISHED;
	}

	/**
	 * Get the progress.
	 *
	 * @return The progress.
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * Set the {@link #progress} and update {@link #state}.
	 *
	 * @param progress The new progress.
	 */
	public void setProgress(float progress) {
		this.progress = progress;
		
		if (progress <= 0) {
			this.state = State.STARTED;
		}
		else if (progress > 0 && progress < 1) {
			this.state = State.RUNNING;
		}
		else if (progress >= 1) {
			this.state = State.FINISHED;
		}
	}
	
}
