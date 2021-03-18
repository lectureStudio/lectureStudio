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

package org.lecturestudio.core.render;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RenderThread extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RenderThread.class);

	private AtomicBoolean running;

	private Thread thread;

	private BlockingDeque<RenderThreadTask> queue;


	public void onTask(final RenderThreadTask event) {
		if (!started()) {
			return;
		}

		if (!queue.offer(event)) {
			// The queue is full. Guarantee the last element is always processed.
			// Replace last element.
			queue.pollLast();
			queue.offer(event);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		queue = new LinkedBlockingDeque<>(5);
		running = new AtomicBoolean(false);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (running.compareAndSet(false, true)) {
			thread = new Thread(this::renderLoop);
			thread.start();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		running.set(false);
		queue.clear();
		onTask(() -> {});
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		queue.clear();
	}

	private void renderLoop() {
		while (running.get()) {
			try {
				RenderThreadTask task = queue.take();
				task.render();
			}
			catch (Exception e) {
				LOG.warn("Rendering failed.", e);
			}
		}
	}

}
