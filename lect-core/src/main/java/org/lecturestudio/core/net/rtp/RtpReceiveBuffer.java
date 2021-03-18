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

package org.lecturestudio.core.net.rtp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * The {@code RtpReceiveBuffer} holds {@link RtpPacket}s for re-ordering
 * purposes. Packets that arrive too late are dropped.
 * 
 * @author Alex Andres
 */
public class RtpReceiveBuffer {

	/** Decoded data queue used for buffering and re-ordering packets. */
	private final PriorityBlockingQueue<RtpReceiveBufferNode> queue;

	/** The buffer size. */
	private final int bufferSize;

	/** Last flushed node. */
	private RtpReceiveBufferNode lastNode;


	/**
	 * Creates a new {@link RtpReceiveBuffer} with specified size.
	 * 
	 * @param size the buffer size
	 */
	public RtpReceiveBuffer(int size) {
		this.bufferSize = size;
		this.queue = new PriorityBlockingQueue<>();
	}

	/**
	 * Adds a new node to the buffer.
	 * 
	 * @param node new node
	 */
	public void addNode(RtpReceiveBufferNode node) {
		if (lastNode != null) {
			// If arrived too late, drop it.
			if (node.compareTo(lastNode) <= 0) {
				return;
			}
		}

		// Check for duplicates and buffer space.
		if (!queue.contains(node) && queue.size() < bufferSize) {
			queue.add(node);
		}
	}

	/**
	 * Flushes the buffer and returns the flushed nodes.
	 * 
	 * @return flushed nodes
	 */
	public List<RtpReceiveBufferNode> flush() {
		if (queue.isEmpty())
			return null;

		int size = queue.size();
		List<RtpReceiveBufferNode> list = new ArrayList<>(size);
		queue.drainTo(list);

		lastNode = list.get(size - 1);

		return list;
	}

}
