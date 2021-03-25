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

package org.lecturestudio.media.track.control;

import org.lecturestudio.core.model.Interval;

/**
 * Media track controls are used to modify the content of {@link
 * org.lecturestudio.media.track.MediaTrack}s within a given time interval.
 * Multiple {@code MediaTrackControl}s may be added to a {@code MediaTrack} in
 * order to modify various sections of the track.
 *
 * @author Alex Andres
 */
public interface MediaTrackControl {

	/**
	 * Adds a listener that is notified when parameters of this media track
	 * control change.
	 *
	 * @param listener The listener to add.
	 */
	void addChangeListener(Runnable listener);

	/**
	 * Removes a listener from this media track control.
	 *
	 * @param listener The listener to remove.
	 */
	void removeChangeListener(Runnable listener);

	/**
	 * @return The time interval in which this control modifies the content.
	 */
	Interval<Double> getInterval();

}
