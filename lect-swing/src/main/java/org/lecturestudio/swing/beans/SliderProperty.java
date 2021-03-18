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

package org.lecturestudio.swing.beans;

import javax.swing.JSlider;

import org.lecturestudio.core.beans.IntegerProperty;

public class SliderProperty extends IntegerProperty {

	private final JSlider slider;


	public SliderProperty(JSlider slider) {
		this.slider = slider;
		this.slider.addChangeListener(e -> {
			super.set(slider.getValue());
		});
	}

	@Override
	public Integer get() {
		return slider.getValue();
	}

	@Override
	public void set(Integer value) {
		slider.setValue(value);

		super.set(value);
	}
}
