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

package org.lecturestudio.editor.api.view.model;

import java.util.Objects;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;

public class PageEvent {

	private final PlaybackAction action;

	private final Time time;

	private final int pageNumber;


	public PageEvent(PlaybackAction action, int pageNumber) {
		this.action = action;
		this.pageNumber = pageNumber;
		this.time = new Time(action.getTimestamp());
	}

	public PlaybackAction getPlaybackAction() {
		return action;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public ActionType getActionType() {
		return action.getType();
	}

	public Time getTime() {
		return time;
	}

	@Override
	public int hashCode() {
		return Objects.hash(action);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		PageEvent other = (PageEvent) obj;

		return Objects.equals(action, other.action);
	}

}
