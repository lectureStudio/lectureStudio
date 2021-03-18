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

package org.lecturestudio.web.api.connector.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;

/**
 * A Service is a group of one or more Connectors. The Connectors are completely
 * independent of each other and may share and provide the same content in
 * different ways, like providing media through different protocols.
 * 
 * @author Alex Andres
 */
public class Connectors extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(Connectors.class);

	/** The associated Connectors with this Service. */
	private final List<Connector> connectors = Collections.synchronizedList(new ArrayList<>());

	
	/**
	 * Create a new {@link Connectors}.
	 */
	public Connectors() {
	}

	/**
	 * @return
	 */
	public List<Connector> getConnectors() {
		return connectors;
	}
	
	/**
	 * Add a new Connector to the set, and associate it with this Service.
	 * 
	 * @param connector The Connector to be added.
	 */
	public void addConnector(Connector connector) {
		connectors.add(connector);

		if (getState() == ExecutableState.Started) {
			try {
				connector.start();
			}
			catch (ExecutableException e) {
				LOG.error("Failed to start connector " + connector, e);
			}
		}
	}

	/**
	 * Remove the specified Connector from this Service. The removed Connector
	 * will also be stopped.
	 * 
	 * @param connector The Connector to be removed.
	 */
	public void removeConnector(Connector connector) {
		if (connectors.remove(connector)) {
			if (connector.getState() == ExecutableState.Started) {
				try {
					connector.stop();
				}
				catch (ExecutableException e) {
					LOG.error("Failed to stop connector " + connector, e);
				}
			}
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		synchronized (connectors) {
			for (Connector connector : connectors) {
				try {
					connector.init();
				}
				catch (Exception e) {
					String message = "Failed to init connector " + connector;
					LOG.error(message, e);

					// Exit on failure.
					throw new ExecutableException(message);
				}
			}
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		synchronized (connectors) {
			for (Connector connector : connectors) {
				try {
					connector.start();
				}
				catch (Exception e) {
					String message = "Failed to start connector " + connector;
					LOG.error(message, e);

					// Exit on failure.
					throw new ExecutableException(message);
				}
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		synchronized (connectors) {
			for (Connector connector : connectors) {
				// Stop only started Connectors.
				if (connector.getState() != ExecutableState.Started) {
					continue;
				}

				try {
					connector.stop();
				}
				catch (Exception e) {
					LOG.error("Failed to stop connector " + connector, e);
				}
			}
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		synchronized (connectors) {
			for (Connector connector : connectors) {
				// Stop only started Connectors.
				if (connector.getState() == ExecutableState.Destroyed) {
					continue;
				}

				try {
					connector.destroy();
				}
				catch (Exception e) {
					LOG.error("Failed to destroy connector " + connector, e);
				}
			}
		}

		connectors.clear();
	}

}
