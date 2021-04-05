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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.lecturestudio.core.ExecutableBase;

public abstract class RecordingExport extends ExecutableBase {

	private final List<Consumer<RecordingRenderProgressEvent>> progressListeners = new ArrayList<>();

	private final List<Consumer<RecordingRenderState>> stateListeners = new ArrayList<>();

	protected CompletableFuture<Void> completableFuture;


	public CompletableFuture<Void> getCompletableFuture() {
		return completableFuture;
	}

	public void addRenderProgressListener(Consumer<RecordingRenderProgressEvent> listener) {
		progressListeners.add(listener);
	}

	public void removeRenderProgressListener(Consumer<RecordingRenderProgressEvent> listener) {
		progressListeners.remove(listener);
	}

	public void addRenderStateListener(Consumer<RecordingRenderState> listener) {
		stateListeners.add(listener);
	}

	public void removeRenderStateListener(Consumer<RecordingRenderState> listener) {
		stateListeners.remove(listener);
	}

	protected void onRenderProgress(RecordingRenderProgressEvent event) {
		for (var listener : progressListeners) {
			listener.accept(event);
		}
	}

	protected void onRenderState(RecordingRenderState state) {
		for (var listener : stateListeners) {
			listener.accept(state);
		}
	}
}
