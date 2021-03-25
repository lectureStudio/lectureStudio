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

package org.lecturestudio.media.track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.media.track.control.MediaTrackControl;

public abstract class MediaTrackBase<T> implements MediaTrack<T> {

	private final List<Consumer<T>> listeners = new ArrayList<>();

	private final ObservableList<MediaTrackControl> controls = new ObservableArrayList<>();

	private T data;


	@Override
	public void addChangeListener(Consumer<T> consumer) {
		listeners.add(consumer);
	}

	@Override
	public void removeChangeListener(Consumer<T> consumer) {
		listeners.remove(consumer);
	}

	@Override
	public void addMediaTrackControl(MediaTrackControl control) {
		controls.add(control);
	}

	@Override
	public void removeMediaTrackControl(MediaTrackControl control) {
		controls.remove(control);
	}

	@Override
	public T getData() {
		return data;
	}

	@Override
	public void setData(T data) {
		this.data = data;

		notifyChange(data);
	}

	@Override
	public void dispose() {
		data = null;
	}

	public ObservableList<MediaTrackControl> getControls() {
		return controls;
	}

	protected void notifyChange(T data) {
		for (Consumer<T> listener : listeners) {
			listener.accept(data);
		}
	}
}
