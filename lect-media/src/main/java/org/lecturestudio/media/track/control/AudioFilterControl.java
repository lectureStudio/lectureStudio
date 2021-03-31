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

package org.lecturestudio.media.track.control;

import org.lecturestudio.core.audio.filter.AudioFilter;

/**
 * This audio track control filters audio samples based on the implementation of
 * the {@link AudioFilter}.
 *
 * @param <T> The type of the audio filter implementation.
 *
 * @author Alex Andres
 */
public abstract class AudioFilterControl<T extends AudioFilter> extends MediaTrackControlBase {

	private final T audioFilter;


	protected AudioFilterControl(T filter) {
		this.audioFilter = filter;
	}

	/**
	 * @return The audio filter that processes the audio samples.
	 */
	public T getAudioFilter() {
		return audioFilter;
	}
}
