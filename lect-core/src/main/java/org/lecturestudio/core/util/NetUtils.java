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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Network utilities to help to access properties of network interfaces.
 * 
 * @author Alex Andres
 */
public final class NetUtils {

	private NetUtils() {
		
	}
	
	/**
	 * Get all available network interfaces which are up and not loopback.
	 * 
	 * @return a list of network interfaces.
	 */
	public static List<NetworkInterface> getNetworkInterfaces() {
		List<NetworkInterface> interfaces = new ArrayList<>();
		
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

			for (NetworkInterface netint : Collections.list(nets)) {
				if (netint.isUp() && !netint.isLoopback()) {
					interfaces.add(netint);
				}
			}
		}
		catch (SocketException e) {
			// Ignore
		}

		return interfaces;
	}
	
	/**
	 * Get the Internet address of the specified network interface name.
	 * 
	 * @param interfaceName the name of the network interface.
	 * @param inetClass Internet address class (Inet4Address or Inet6Address).
	 * 
	 * @return the Internet address as one of the two types.
	 * 
	 * @throws SocketException if no interface for the specified name is available.
	 */
	public static String getHostAddress(String interfaceName, Class<? extends InetAddress> inetClass) throws SocketException {
		String hostAddress = null;
		NetworkInterface iface = NetworkInterface.getByName(interfaceName);
		Enumeration<InetAddress> addresses = iface.getInetAddresses();

		while (addresses.hasMoreElements()) {
			InetAddress inetAddress = addresses.nextElement();
			if (inetAddress != null && inetAddress.getClass().equals(inetClass)) {
				hostAddress = inetAddress.getHostAddress();
				break;
			}
		}

		return hostAddress;
	}
	
	public static boolean isLocalAddress(String address, Integer port) {
		return address == null || port == null || address.equals("0.0.0.0") || address.equals("127.0.0.1");
	}
	
}
