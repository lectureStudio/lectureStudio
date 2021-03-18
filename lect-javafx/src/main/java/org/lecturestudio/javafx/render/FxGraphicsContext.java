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

package org.lecturestudio.javafx.render;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.graphics.GraphicsContext;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

public class FxGraphicsContext implements GraphicsContext {

	private final javafx.scene.canvas.GraphicsContext gc;


	public FxGraphicsContext(javafx.scene.canvas.GraphicsContext gc) {
		this.gc = gc;
	}

	@Override
	public void fillRect(double x, double y, double width, double height) {
		gc.fillRect(x, y, width, height);
	}

	@Override
	public void restore() {
		gc.restore();
	}

	@Override
	public void save() {
		gc.save();
	}

	@Override
	public void setClip(double x, double y, double width, double height) {
		//gc.beginPath();
		//gc.rect(x, y, width, height);
		//gc.clip();
	}

	@Override
	public void setFill(Color color) {
		gc.setFill(ColorConverter.INSTANCE.to(color));
	}

	@Override
	public void scale(double sx, double sy) {
		gc.scale(sx, sy);
	}

	@Override
	public void translate(double tx, double ty) {
		gc.translate(tx, ty);
	}

	@Override
	public Object get() {
		return gc;
	}

}
