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

package org.lecturestudio.web.api.client;

import static java.util.Objects.nonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

/**
 * Client connection failover handler.
 *
 * @author Alex Andres
 */
public class ClientFailover extends ExecutableBase {

	private static final int DELAY_S = 10;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private final List<Executable> executables = new CopyOnWriteArrayList<>();

	private ScheduledFuture<?> future;


	public void addExecutable(Executable executable) {
		executables.add(executable);
	}

	public void removeExecutable(Executable executable) {
		executables.remove(executable);
	}

	@Override
	protected void initInternal() throws ExecutableException {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		scheduler.execute(() -> {
			// Stop components to start from a clean state again.
			for (Executable executable : executables) {
				if (!executable.started()) {
					continue;
				}

				try {
					executable.stop();

					System.out.println("stopped: " + executable.getClass().getSimpleName());
				}
				catch (ExecutableException e) {
					e.printStackTrace();
				}
			}

			future = scheduler.scheduleAtFixedRate(this::runTasks, DELAY_S,
					DELAY_S, TimeUnit.SECONDS);
		});
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		cancel();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		executables.clear();
	}

	private void runTasks() {
		int count = 0;

		for (Executable executable : executables) {
			System.out.println("starting: " + executable.getClass().getSimpleName());

			try {
				executable.start();

				count++;
			}
			catch (Throwable ignore) {
				try {
					// Release potentially allocated resources.
					executable.stop();
				}
				catch (ExecutableException e) {
					// Ignore
				}

				break;
			}
		}

		if (count == executables.size()) {
			cancel();
		}
	}

	private void cancel() {
		if (nonNull(future) && !future.isCancelled()) {
			future.cancel(false);
		}
	}
}
