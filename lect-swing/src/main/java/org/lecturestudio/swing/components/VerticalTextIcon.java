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

package org.lecturestudio.swing.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class VerticalTextIcon implements Icon, SwingConstants {

	private final String text;

	private final FontMetrics fm;

	private final int width;

	private final int height;

	private final int direction;


	public VerticalTextIcon(String text, int direction) {
		this.text = text;

		Font labelFont = UIManager.getFont("Label.font");

		this.fm = new JComponent() {
			private static final long serialVersionUID = 910246969230232259L;
		}.getFontMetrics(labelFont);

		this.width = SwingUtilities.computeStringWidth(fm, text);
		this.height = fm.getHeight();
		this.direction = direction;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform oldTransform = g2.getTransform();

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (direction == SwingConstants.RIGHT) {
			g2.translate(x + getIconWidth(), y);
			g2.rotate(Math.PI / 2);
		}
		else if (direction == SwingConstants.LEFT) {
			g2.translate(x, y + getIconHeight());
			g2.rotate(-Math.PI / 2);
		}

		g.drawString(text, 0, fm.getLeading() + fm.getAscent());
		g2.setTransform(oldTransform);
	}

	@Override
	public int getIconWidth() {
		return height;
	}

	@Override
	public int getIconHeight() {
		return width;
	}

	public String getTitle() {
		return text;
	}

}
