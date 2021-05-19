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

package org.lecturestudio.presenter.api.net;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.broadcast.Broadcaster;
import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.broadcast.config.Configuration;
import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;

@Singleton
public class LocalBroadcaster implements Executable {

	private final ApplicationContext context;
	
	/** The counter of attempts to start the local broadcaster. */
	private final AtomicInteger startCount = new AtomicInteger(0);
	
	/** The current state of this component. */
    private ExecutableState state = ExecutableState.Created;
	
    /** The broadcaster component. */
	private Broadcaster broadcaster;


	@Inject
	public LocalBroadcaster(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public final synchronized void init() throws ExecutableException {
		PresenterConfiguration pConfig = (PresenterConfiguration) context.getConfiguration();
		BroadcastProfile bcastProfile = pConfig.getNetworkConfig().getBroadcastProfile();

		Configuration config = new Configuration();
		config.port = bcastProfile.getBroadcastPort();
		config.tlsPort = bcastProfile.getBroadcastTlsPort();

		broadcaster = new Broadcaster(config);
		broadcaster.init();

		setState(ExecutableState.Initialized);
	}

	@Override
	public final synchronized void start() throws ExecutableException {
		if (state == ExecutableState.Started) {
            startCount.incrementAndGet();
            return;
        }
		
		if (state == ExecutableState.Created || state == ExecutableState.Destroyed) {
            init();
        }
		
		broadcaster.start();
		
		startCount.incrementAndGet();
		
		setState(ExecutableState.Started);
	}

	@Override
	public final synchronized void stop() throws ExecutableException {
		if (state != ExecutableState.Started) {
            return;
        }
		if (startCount.decrementAndGet() > 0) {
			return;
		}
		
		broadcaster.stop();
		
		setState(ExecutableState.Stopped);
	}
	
	@Override
	public void suspend() {
		
	}

	@Override
	public final synchronized void destroy() throws ExecutableException {
		if (state != ExecutableState.Stopped) {
            return;
        }
		
		broadcaster.destroy();
		
		setState(ExecutableState.Destroyed);
	}

	@Override
	public ExecutableState getState() {
		return state;
	}
	
	/**
	 * Update the component state.
	 *
	 * @param state The new state for this component.
	 */
	private synchronized void setState(ExecutableState state) {
		this.state = state;
	}

}
