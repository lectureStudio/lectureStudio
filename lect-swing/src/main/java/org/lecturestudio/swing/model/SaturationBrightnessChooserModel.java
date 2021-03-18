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

package org.lecturestudio.swing.model;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class SaturationBrightnessChooserModel {

	private EventListenerList listenerList = new EventListenerList();

	private float hue = 0;

	private float saturation = 0;

	private float brightness = 1;


	public void setHue(float value) {
		if (value != this.hue) {
			this.hue = value;
			fireStateChanged();
		}
	}

	public float getHue() {
		return hue;
	}

	public void setSaturation(float value) {
		if (value != this.saturation) {
			this.saturation = value;
			fireStateChanged();
		}
	}

	public float getSaturation() {
		return saturation;
	}

	public void setBrightness(float value) {
		if (value != this.brightness) {
			this.brightness = value;
			fireStateChanged();
		}
	}

	public float getBrightness() {
		return brightness;
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	protected void fireStateChanged() {
		ChangeEvent event = new ChangeEvent(this);
		ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);

		for (ChangeListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

}
