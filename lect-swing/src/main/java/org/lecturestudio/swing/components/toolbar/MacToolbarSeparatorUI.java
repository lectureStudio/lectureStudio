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
/* based on the code from the Pumpernickel project under the following licence: */
/*
 * This software is released as part of the Pumpernickel project.
 *
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 *
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package org.lecturestudio.swing.components.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicSeparatorUI;

/**
 * A fine dotted line.
 */
public class MacToolbarSeparatorUI extends BasicSeparatorUI {

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension d = super.getPreferredSize(c);
		d.width = 8;
		return d;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		if (c.isOpaque()) {
			g.setColor(c.getBackground());
			g.fillRect(0, 0, c.getWidth(), c.getHeight());
		}
		paintSeparator(g, c.getWidth(), c.getHeight());
	}

	/**
	 * Paints a separator in the dimensions provided.
	 */
	protected static void paintSeparator(Graphics g, int w, int h) {
		int y = 0;
		g.setColor(new Color(128, 128, 128));
		int x = w / 2;

		while (y < h) {
			g.fillRect(x, y, 1, 1);
			y += 3;
		}
	}
}
