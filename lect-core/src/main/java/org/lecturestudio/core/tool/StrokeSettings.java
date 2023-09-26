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

package org.lecturestudio.core.tool;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;

public class StrokeSettings extends PaintSettings {

	private final DoubleProperty width = new DoubleProperty();

	private final ObjectProperty<StrokeWidthSettings> strokeWidthSettings = new ObjectProperty<>(StrokeWidthSettings.NORMAL);

	private final BooleanProperty scale = new BooleanProperty();


	public StrokeSettings() {

	}

	public StrokeSettings(StrokeSettings settings) {
		super(settings);

		setWidth(settings.getWidth());
		setScale(settings.getScale());
		setStrokeWidthSettings(settings.getStrokeWidthSettings());
	}

	public DoubleProperty widthProperty() {
		return width;
	}

	public double getWidth() {
		return width.get();
	}

	public void setWidth(double width) {
		this.width.set(width);
	}

	/**
	 * @return the scale
	 */
	public Boolean getScale() {
		return scale.get();
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(boolean scale) {
		this.scale.set(scale);
	}

	public BooleanProperty scaleProperty() {
		return scale;
	}

	public ObjectProperty<StrokeWidthSettings> strokeWidthSettingsProperty() {
		return strokeWidthSettings;
	}

	public StrokeWidthSettings getStrokeWidthSettings() {
		return strokeWidthSettings.get();
	}

	public void setStrokeWidthSettings(StrokeWidthSettings strokeWidthSettings) {
		this.strokeWidthSettings.set(strokeWidthSettings);
	}
}
