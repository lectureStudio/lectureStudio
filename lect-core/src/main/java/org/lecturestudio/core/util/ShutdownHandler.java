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

package org.lecturestudio.core.util;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ShutdownHandler {

	private final static Logger LOG = LogManager.getLogger(ShutdownHandler.class);

	private final Lock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();


	/**
	 * Executes specific code during the shutdown procedure, or throws an
	 * exception if unable to do so.
	 *
	 * @return true to execute next ShutdownHandler, false to stop the shutdown
	 *         procedure.
	 * 
	 * @throws Exception if unable to execute code.
	 */
	abstract public boolean execute() throws Exception;


	protected void executeAndWait(Runnable runnable) {
		lock.lock();

		try {
			runnable.run();

			condition.await();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			lock.unlock();
		}
	}

	protected void resume() {
		lock.lock();

		try {
			condition.signal();
		}
		finally {
			lock.unlock();
		}
	}

	final protected void logException(Throwable throwable, String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}
}
