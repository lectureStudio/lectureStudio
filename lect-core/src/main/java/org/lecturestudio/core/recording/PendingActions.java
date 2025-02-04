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

package org.lecturestudio.core.recording;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.PlaybackAction;

public class PendingActions {

	private Map<Page, List<PlaybackAction>> pendingActions;

	private Page pendingPage;


	public void initialize() {
		pendingActions = new ConcurrentHashMap<>();
	}

	public void addPendingAction(PlaybackAction action) {
		List<PlaybackAction> list = getPendingActions(pendingPage);

		if (isNull(list)) {
			list = new ArrayList<>();

			pendingActions.put(pendingPage, list);
		}

		list.add(action);
	}

	public List<PlaybackAction> getPendingActions(Page page) {
		return pendingActions.get(page);
	}

	public Map<Page, List<PlaybackAction>> getAllPendingActions() {
		return pendingActions;
	}

	public void clear() {
		pendingPage = null;
		pendingActions = null;
	}

	public void clearPendingActions(Page page) {
		List<PlaybackAction> actions = pendingActions.remove(page);

		if (isNull(actions)) {
			// Page not found for removal. May be due to replaced document.
			// Find by common document.
			for (Page p : pendingActions.keySet()) {
				if (page.getDocument().getName().equals(p.getDocument().getName())) {
					pendingActions.remove(p);
					break;
				}
			}
		}

		if (page == pendingPage) {
			pendingPage = null;
		}
	}

	public boolean hasPendingActions(Page page) {
		List<PlaybackAction> actions = getPendingActions(page);
		return nonNull(actions) && !actions.isEmpty();
	}

	public Page getPendingPage() {
		return pendingPage;
	}

	public void setPendingPage(Page page) {
		if (page == pendingPage) {
			return;
		}

		pendingPage = page;

		if (isNull(page)) {
			return;
		}

		if (!pendingActions.containsKey(page)) {
			pendingActions.put(page, new ArrayList<>());
		}
	}
}
