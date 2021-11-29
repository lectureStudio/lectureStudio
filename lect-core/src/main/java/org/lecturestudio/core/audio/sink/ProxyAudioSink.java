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

package org.lecturestudio.core.audio.sink;

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * AudioSink proxy implementation which redirects all calls to a specified
 * sink.
 *
 * @author Alex Andres
 */
public class ProxyAudioSink implements AudioSink {

	private final AudioSink proxy;


	public ProxyAudioSink(AudioSink proxy) {
		this.proxy = proxy;
	}

	@Override
	public void open() throws IOException {
		proxy.open();
	}

	@Override
	public void reset() throws IOException {
		proxy.reset();
	}

	@Override
	public void close() throws IOException {
		proxy.close();
	}

	@Override
	public int write(byte[] data, int offset, int length) throws IOException {
		return proxy.write(data, offset, length);
	}

	@Override
	public AudioFormat getAudioFormat() {
		return proxy.getAudioFormat();
	}

	@Override
	public void setAudioFormat(AudioFormat format) {
		proxy.setAudioFormat(format);
	}
}
