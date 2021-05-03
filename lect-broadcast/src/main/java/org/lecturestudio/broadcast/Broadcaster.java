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

package org.lecturestudio.broadcast;

import org.lecturestudio.broadcast.config.Configuration;
import org.lecturestudio.broadcast.server.QuarkusServer;
import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

public class Broadcaster extends ExecutableBase {

	private final Configuration config;

	private Executable appServer;


	public Broadcaster(Configuration config) {
		this.config = config;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		appServer = new QuarkusServer(config);
		appServer.init();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		appServer.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		appServer.stop();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		appServer.destroy();
	}
}
