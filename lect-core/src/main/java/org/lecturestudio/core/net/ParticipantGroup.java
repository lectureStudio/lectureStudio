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

package org.lecturestudio.core.net;

import org.lecturestudio.core.net.bus.NetworkBus;
import org.lecturestudio.core.net.bus.event.ParticipantJoinEvent;
import org.lecturestudio.core.net.bus.event.ParticipantLeaveEvent;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class maintains {@link Participant}s that should receive media streams.
 * 
 * @author Alex Andres
 * 
 */
public class ParticipantGroup {

	/**
	 * The participants.
	 */
	private final ConcurrentMap<Integer, Participant> participants = new ConcurrentHashMap<>();


	/**
	 * Adds a participant to the group.
	 * 
	 * @param p new participant
	 */
	public void add(Participant p) {
		if (contains(p)) {
			return;
		}

		Integer id = Integer.valueOf(System.identityHashCode(p));

		// associate the participant with a unique ID
		while (true) {
			// check if participant successfully added
			if (participants.putIfAbsent(id, p) == null) {
				NetworkBus.post(new ParticipantJoinEvent(p));
				return;
			}
			else {
				// ID duplicate, try another one
				id = Integer.valueOf(id.intValue() + 1);
			}
		}
	}

	/**
	 * Returns a participant that has the provided IP address.
	 * 
	 * @param address
	 *            the IP address
	 * @return a participant or null if none found
	 */
	public Participant getByIpAddress(String address) {
		for (Integer id : participants.keySet()) {
			Participant p = participants.get(id);

			if (p.getIpAddress().equals(address)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Removes a participant from the group that has the specified IP address.
	 * 
	 * @param address
	 *            the IP address
	 */
	public void remove(InetAddress address) {
		for (Integer id : participants.keySet()) {
			Participant p = participants.get(id);

			if (p.getIpAddress().equals(address.getHostAddress())) {
				participants.remove(id);
				NetworkBus.post(new ParticipantLeaveEvent(p));
				return;
			}
		}
	}

	/**
	 * Checks if a participant exists in the group.
	 * 
	 * @param p
	 *            a participant
	 * @return true if the provided participant exists in the group
	 */
	public boolean contains(Participant p) {
		return participants.containsValue(p);
	}

	/**
	 * @return true if this group contains participants.
	 */
	public boolean hasParticipants() {
		return !participants.isEmpty();
	}

	/**
	 * @return all participants of this group.
	 */
	public List<Participant> getParticipants() {
		return Collections.synchronizedList(new ArrayList<Participant>(participants.values()));
	}

	/**
	 * @return all participants of this group represented by an iterator.
	 */
	public Iterator<Participant> iterator() {
		return participants.values().iterator();
	}

	/**
	 * Removes all participants in this group.
	 */
	public void clear() {
		participants.clear();
	}

	/**
	 * @return the number of participants in this group.
	 */
	public int size() {
		return participants.size();
	}

}
