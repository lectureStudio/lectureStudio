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

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.model.Interval;

/**
 * The MediaTrackControl base class mainly provides convenience methods for
 * specific implementations.
 *
 * @author Alex Andres
 */
public abstract class MediaTrackControlBase implements MediaTrackControl {

	private final List<Runnable> listeners = new ArrayList<>();

	private final Interval<Double> interval = new Interval<>();

	private double start;

	private double end;


	@Override
	public void addChangeListener(Runnable listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(Runnable listener) {
		listeners.remove(listener);
	}

	@Override
	public void setStartTime(double value) {
		// Convert -0.0 to +0.0.
		if (value == 0.0) {
			value = 0.0;
		}

		if (Double.compare(this.start, value) != 0) {
			this.start = value;

			interval.set(start, end);

			fireControlChange();
		}
	}

	@Override
	public void setEndTime(double value) {
		// Convert -0.0 to +0.0.
		if (value == 0.0) {
			value = 0.0;
		}

		if (Double.compare(this.end, value) != 0) {
			this.end = value;

			interval.set(start, end);

			fireControlChange();
		}
	}

	@Override
	public Interval<Double> getInterval() {
		return interval;
	}

	/**
	 * Notify listeners that a change within this control has occurred.
	 */
	protected void fireControlChange() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}
}
