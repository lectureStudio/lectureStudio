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

package org.lecturestudio.broadcast.config;

import java.util.Objects;

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.StringProperty;

/**
 * A broadcast profile describes connection parameters required to establish a
 * connection to a broadcast server.
 *
 * @author Alex Andres
 */
public class BroadcastProfile {

	private final StringProperty name = new StringProperty();

	private final StringProperty broadcastAddress = new StringProperty();

	private final IntegerProperty broadcastPort = new IntegerProperty();

	private final IntegerProperty broadcastTlsPort = new IntegerProperty();


	/**
	 * Get the name of this profile. The name represents a short human-readable
	 * text to be used by UI frameworks to describe this profile.
	 *
	 * @return The profile name.
	 */
	public String getName() {
		return name.get();
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	public StringProperty nameProperty() {
		return name;
	}

	/**
	 * Get the address of the broadcast server.
	 *
	 * @return The broadcast server address.
	 */
	public String getBroadcastAddress() {
		return broadcastAddress.get();
	}

	/**
	 * @param address the broadcastAddress to set
	 */
	public void setBroadcastAddress(String address) {
		this.broadcastAddress.set(address);
	}

	public StringProperty broadcastAddressProperty() {
		return broadcastAddress;
	}

	/**
	 * Get the service port of the broadcast server.
	 *
	 * @return The service port.
	 */
	public Integer getBroadcastPort() {
		return broadcastPort.get();
	}

	/**
	 * @param port the broadcastPort to set
	 */
	public void setBroadcastPort(int port) {
		this.broadcastPort.set(port);
	}

	public IntegerProperty broadcastPortProperty() {
		return broadcastPort;
	}

	/**
	 * Get the service TLS port of the broadcast server.
	 *
	 * @return The service TLS port.
	 */
	public Integer getBroadcastTlsPort() {
		return broadcastTlsPort.get();
	}

	/**
	 * @param tlsPort the TLS port to set
	 */
	public void setBroadcastTlsPort(int tlsPort) {
		this.broadcastTlsPort.set(tlsPort);
	}

	public IntegerProperty broadcastTlsPortProperty() {
		return broadcastTlsPort;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BroadcastProfile profile = (BroadcastProfile) o;

		return Objects.equals(getName(), profile.getName())
				&& Objects.equals(getBroadcastAddress(), profile.getBroadcastAddress())
				&& Objects.equals(getBroadcastPort(), profile.getBroadcastPort())
				&& Objects.equals(getBroadcastTlsPort(), profile.getBroadcastTlsPort());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getBroadcastAddress(),
				getBroadcastPort(), getBroadcastTlsPort());
	}
}
