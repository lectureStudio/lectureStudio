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

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.graphics.Color;

public abstract class PaintSettings {

	private final ObjectProperty<Color> color = new ObjectProperty<>(Color.BLACK);

	private final IntegerProperty alpha = new IntegerProperty(255);


	public PaintSettings() {

	}

	public PaintSettings(PaintSettings settings) {
		setAlpha(settings.getAlpha());
		setColor(settings.getColor());
	}

	public ObjectProperty<Color> colorProperty() {
		return color;
	}

	public Color getColor() {
		return color.get();
	}

	public void setColor(Color color) {
		this.color.set(color);
	}

	public IntegerProperty alphaProperty() {
		return alpha;
	}

	public int getAlpha() {
		return alpha.get();
	}

	public void setAlpha(int value) {
		this.alpha.set(value);
	}

}
