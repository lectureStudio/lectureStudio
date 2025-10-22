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

package org.lecturestudio.editor.api.recording;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableBase;

/**
 * Abstract class for recording export operations. Provides functionality for monitoring
 * render progress and state changes during the export process. Extends ExecutableBase to
 * leverage execution lifecycle management.
 *
 * @author Alex Andres
 */
public abstract class RecordingExport extends ExecutableBase {

	/** Logger for this class. */
	protected static final Logger LOG = LogManager.getLogger(RecordingExport.class);

	/** List of listeners to be notified of recording rendering progress events. */
	private final List<Consumer<RecordingRenderProgressEvent>> progressListeners = new ArrayList<>();

	/** List of listeners to be notified of recording rendering state changes. */
	private final List<Consumer<RecordingRenderState>> stateListeners = new ArrayList<>();


	/**
	 * Adds a listener to be notified of recording rendering progress events.
	 *
	 * @param listener The consumer that will receive progress events.
	 */
	public void addRenderProgressListener(Consumer<RecordingRenderProgressEvent> listener) {
		progressListeners.add(listener);
	}

	/**
	 * Removes a previously added rendering progress listener.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeRenderProgressListener(Consumer<RecordingRenderProgressEvent> listener) {
		progressListeners.remove(listener);
	}

	/**
	 * Adds a listener to be notified of recording rendering state changes.
	 *
	 * @param listener The consumer that will receive state change events.
	 */
	public void addRenderStateListener(Consumer<RecordingRenderState> listener) {
		stateListeners.add(listener);
	}

	/**
	 * Removes a previously added rendering state listener.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeRenderStateListener(Consumer<RecordingRenderState> listener) {
		stateListeners.remove(listener);
	}

	/**
	 * Notifies all registered progress listeners about a render progress event.
	 *
	 * @param event The progress event to broadcast to listeners.
	 */
	public void onRenderProgress(RecordingRenderProgressEvent event) {
		for (var listener : progressListeners) {
			listener.accept(event);
		}
	}

	/**
	 * Notifies all registered state listeners about a render state change.
	 *
	 * @param state The new render state to broadcast to listeners.
	 */
	protected void onRenderState(RecordingRenderState state) {
		for (var listener : stateListeners) {
			listener.accept(state);
		}
	}
}
