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

package org.lecturestudio.presenter.api.model;

import java.util.LinkedHashSet;

import org.lecturestudio.swing.view.PeerView;

public class ParticipantViewCollection {

	private LinkedHashSet<PeerView> activeParticipants;

	private LinkedHashSet<PeerView> participants;

	private int maxActiveParticipants;


	public ParticipantViewCollection(int maxActive) {
		activeParticipants = new LinkedHashSet<>();
		participants = new LinkedHashSet<>();
		maxActiveParticipants = maxActive;
	}

	public void setMaxActiveParticipants(int maxActive) {
		maxActiveParticipants = maxActive;
	}

	public void addParticipant(PeerView participant) {
		participants.add(participant);
	}

	public void removeParticipant(PeerView participant) {
		participants.remove(participant);
		activeParticipants.remove(participant);
	}

	public LinkedHashSet<PeerView> getActiveParticipants() {
		return activeParticipants;
	}

	public LinkedHashSet<PeerView> getParticipants() {
		return participants;
	}

	/**
	 * Sets a participant as active using LIFO (Last-In First-Out) behavior.
	 * If the maximum number of active participants is reached, the most recently
	 * added participant will be removed before adding the new one.
	 *
	 * @param participant The participant to set as active.
	 */
	public void setActiveParticipant(PeerView participant) {
		// If the participant is already active, do nothing to keep the order.
		if (activeParticipants.contains(participant)) {
			return;
		}
		else if (activeParticipants.size() >= maxActiveParticipants) {
			// Find the most recently added participant (last element).
			PeerView mostRecent = null;
			for (PeerView p : activeParticipants) {
				mostRecent = p;
			}

			// Remove the most recently added participant.
			if (mostRecent != null) {
				activeParticipants.remove(mostRecent);

				// Add the most recently removed participant back to the list of participants.
				participants.add(mostRecent);
			}
		}

		// Add the new participant (which will now be the most recent).
		activeParticipants.add(participant);

		participants.remove(participant);
	}
}
