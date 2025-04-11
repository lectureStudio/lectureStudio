/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.model;

import org.lecturestudio.core.beans.BooleanProperty;

/**
 * Observer class that tracks manual state changes for recording and microphone functionality.
 * This class provides properties and methods to monitor and control the recording and microphone states
 * in the presenter application.
 *
 * @author Alex Andres
 */
public class ManualStateObserver {

	/** Property that indicates whether a recording has been started. */
	private final BooleanProperty recordingStarted = new BooleanProperty();

	/** Property that indicates whether the microphone is currently active. */
	private final BooleanProperty microphoneActive = new BooleanProperty();


	/**
	 * Sets the recording state.
	 *
	 * @param started true if recording has started, false otherwise.
	 */
	public void setRecordingStarted(boolean started) {
		recordingStarted.set(started);
	}

	/**
	 * Gets the current recording state.
	 *
	 * @return true if recording has started, false otherwise.
	 */
	public boolean getRecordingStarted() {
		return recordingStarted.get();
	}

	/**
	 * Gets the property object for the recording state.
	 *
	 * @return the BooleanProperty representing the recording state.
	 */
	public BooleanProperty recordingStartedProperty() {
		return recordingStarted;
	}

	/**
	 * Sets the microphone state.
	 *
	 * @param started true if microphone is active, false otherwise.
	 */
	public void setMicrophoneActive(boolean started) {
		microphoneActive.set(started);
	}

	/**
	 * Gets the current microphone state.
	 *
	 * @return true if microphone is active, false otherwise.
	 */
	public boolean getMicrophoneActive() {
		return microphoneActive.get();
	}

	/**
	 * Gets the property object for the microphone state.
	 *
	 * @return the BooleanProperty representing the microphone state.
	 */
	public BooleanProperty microphoneActiveProperty() {
		return microphoneActive;
	}
}
