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

package org.lecturestudio.web.api.stream;

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.web.api.model.ScreenSource;

public class StreamScreenContext {

	private final ObjectProperty<ScreenSource> screenSource;

	private final IntegerProperty frameRate;


	public StreamScreenContext() {
		screenSource = new ObjectProperty<>();
		frameRate = new IntegerProperty();
	}

	public ObjectProperty<ScreenSource> screenSourceProperty() {
		return screenSource;
	}

	public ScreenSource getScreenSource() {
		return screenSource.get();
	}

	public void setScreenSource(ScreenSource source) {
		screenSource.set(source);
	}

	public IntegerProperty frameRateProperty() {
		return frameRate;
	}

	public Integer getFrameRate() {
		return frameRate.get();
	}

	public void setFrameRate(int rate) {
		frameRate.set(rate);
	}
}