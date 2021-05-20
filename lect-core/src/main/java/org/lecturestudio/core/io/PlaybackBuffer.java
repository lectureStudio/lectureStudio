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

package org.lecturestudio.core.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link PlaybackBuffer} is used by media players to play the enqueued media data
 * encapsulated in {@link PlaybackData}.
 *
 * @param <T> The type of the buffer values.
 *
 * @author Alex Andres
 */
public class PlaybackBuffer<T> {

	/** The playback media data queue. */
	private final BlockingQueue<PlaybackData<T>> playbackQueue;


	/**
	 * Create a new {@link PlaybackBuffer} instance.
	 */
	public PlaybackBuffer() {
		this.playbackQueue = new LinkedBlockingQueue<>();
	}

	/**
	 * Enqueue the specified media data for playback.
	 *
	 * @param data The media data to be processed.
	 */
	public void put(PlaybackData<T> data) {
		if (data == null) {
			return;
		}

		playbackQueue.offer(data);
	}

	/**
	 * Retrieves, but does not remove, the head of the playback buffer queue.
	 *
	 * @return The head of the playback buffer queue.
	 */
	public PlaybackData<T> peek() {
		return playbackQueue.peek();
	}

	/**
	 * Retrieves and removes the head of the playback buffer queue, waiting if
	 * necessary until media data becomes available.
	 *
	 * @return The head of the playback buffer queue.
	 */
	public PlaybackData<T> take() {
		try {
			return playbackQueue.take();
		}
		catch (InterruptedException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Removes all of the media data from the playback buffer queue.
	 */
	public void reset() {
		playbackQueue.clear();
	}

}
