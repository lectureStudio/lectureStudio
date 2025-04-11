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

package org.lecturestudio.media.audio;

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.sink.AudioSink;

/**
 * An abstract base implementation of the AudioSink interface.
 * This class provides empty implementations of the interface methods that must be overridden by concrete subclasses.
 *
 * @see org.lecturestudio.core.audio.sink.AudioSink
 *
 * @author Alex Andres
 */
public abstract class DefaultAudioSink implements AudioSink {

	@Override
	public void open() throws IOException {

	}

	@Override
	public void reset() throws IOException {

	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void setAudioFormat(AudioFormat format) {

	}
}
