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

package org.lecturestudio.core.swing;

import static java.util.Objects.nonNull;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.graphics.GraphicsContext;

public class SwingGraphicsContext implements GraphicsContext {

	private final Graphics2D g2d;

	private AffineTransform transform;


	public SwingGraphicsContext(Graphics2D g2d) {
		this.g2d = g2d;
	}

	@Override
	public void fillRect(double x, double y, double width, double height) {
		g2d.fill(new Rectangle2D.Double(x, y, width, height));
	}

	@Override
	public void restore() {
		if (nonNull(transform)) {
			g2d.setTransform(transform);
		}
	}

	@Override
	public void save() {
		transform = g2d.getTransform();
	}

	@Override
	public void setClip(double x, double y, double width, double height) {
		g2d.setClip(new Rectangle2D.Double(x, y, width, height));
	}

	@Override
	public void setFill(Color color) {
		g2d.setBackground(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()));
	}

	@Override
	public void scale(double sx, double sy) {
		g2d.scale(sx, sy);
	}

	@Override
	public void translate(double tx, double ty) {
		g2d.translate(tx, ty);
	}

	@Override
	public Object get() {
		return g2d;
	}

}
