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

import static java.util.Objects.nonNull;

import java.util.LinkedHashSet;

import org.lecturestudio.swing.view.ParticipantView;

/**
 * A collection class that manages participant views, maintaining both a complete set
 * of participants and a subset of active participants.
 * <p>
 * This class implements a mechanism to limit the number of simultaneously active participants, using a LIFO
 * (Last-In First-Out) behavior when managing active status.
 *
 * @author Alex Andres
 */
public class ParticipantViewCollection {

	/**
	 * Collection of currently active participants. This set maintains the order of activation and is limited in size by
	 * {@code maxActiveParticipants}.
	 */
	private final LinkedHashSet<ParticipantView> activeParticipants;

	/**
	 * Collection of all participants, regardless of their active status.
	 */
	private final LinkedHashSet<ParticipantView> participants;

	/**
	 * The maximum number of participants that can be active simultaneously.
	 */
	private int maxActiveParticipants;


	/**
	 * Constructs a new ParticipantViewCollection with the specified maximum number of active participants.
	 *
	 * @param maxActive The maximum number of participants that can be active at one time.
	 */
	public ParticipantViewCollection(int maxActive) {
		activeParticipants = new LinkedHashSet<>();
		participants = new LinkedHashSet<>();
		maxActiveParticipants = maxActive;
	}

	/**
	 * Sets the maximum number of participants that can be active simultaneously.
	 *
	 * @param maxActive The maximum number of active participants.
	 */
	public void setMaxActiveParticipants(int maxActive) {
		maxActiveParticipants = maxActive;
	}

	/**
	 * Adds a participant to the collection of all participants.
	 *
	 * @param participant The participant to add.
	 */
	public void addParticipant(ParticipantView participant) {
		participants.add(participant);
	}

	/**
	 * Removes a participant from both the general collection and the active participants' collection.
	 *
	 * @param participant The participant to remove.
	 */
	public void removeParticipant(ParticipantView participant) {
		participants.remove(participant);
		activeParticipants.remove(participant);

		if (activeParticipants.size() < maxActiveParticipants) {
			ParticipantView mostRecentTalker = findMostRecentTalker();
			if (nonNull(mostRecentTalker)) {
				setActiveParticipant(mostRecentTalker);
			}
		}
	}

	/**
	 * Returns the collection of currently active participants.
	 *
	 * @return A LinkedHashSet containing the active participants.
	 */
	public LinkedHashSet<ParticipantView> getActiveParticipants() {
		return activeParticipants;
	}

	/**
	 * Returns the collection of all participants.
	 *
	 * @return A LinkedHashSet containing all participants.
	 */
	public LinkedHashSet<ParticipantView> getParticipants() {
		return participants;
	}

	/**
	 * Sets a participant as active using LIFO (Last-In First-Out) behavior.
	 * If the maximum number of active participants is reached, the most recently
	 * added participant will be removed before adding the new one.
	 *
	 * @param participant The participant to set as active.
	 */
	public void setActiveParticipant(ParticipantView participant) {
		// If the participant is already active, do nothing to keep the order.
		if (activeParticipants.contains(participant)) {
			return;
		}
		else if (activeParticipants.size() >= maxActiveParticipants) {
			// Find the most recently added participant (last element).
			ParticipantView mostRecent = null;
			for (ParticipantView p : activeParticipants) {
				mostRecent = p;
			}

			// Remove the most recently added participant.
			if (mostRecent != null) {
				activeParticipants.remove(mostRecent);
			}
		}

		// Add the new participant (which will now be the most recent).
		activeParticipants.add(participant);
	}

	/**
	 * Clears all active participants from the collection. This method removes all participants from the active
	 * participants set without affecting the main participants' collection.
	 */
	public void clearActiveParticipants() {
		activeParticipants.clear();
	}

	/**
	 * Finds the participant with the most recent talking activity from the collection.
	 *
	 * @return The participant with the most recent talking activity, or null if no participants exist.
	 */
	private ParticipantView findMostRecentTalker() {
		if (participants.isEmpty()) {
			return null;
		}

		ParticipantView mostRecentTalker = null;
		long highestTimestamp = Long.MIN_VALUE;

		for (ParticipantView participant : participants) {
			long talkTimestamp = participant.getParticipantContext().getTalkingActivityTimestamp();

			if (talkTimestamp > highestTimestamp) {
				highestTimestamp = talkTimestamp;
				mostRecentTalker = participant;
			}
		}

		return mostRecentTalker;
	}
}
