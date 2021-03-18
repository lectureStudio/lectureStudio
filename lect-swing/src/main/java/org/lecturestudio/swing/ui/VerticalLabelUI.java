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

package org.lecturestudio.swing.ui;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicLabelUI;

public class VerticalLabelUI extends BasicLabelUI {

	private final boolean clockwise;


	public VerticalLabelUI(int tabPlacement) {
		super();

		clockwise = tabPlacement == SwingConstants.RIGHT;
	}

	public Dimension getPreferredSize(JComponent c) {
		Dimension size = super.getPreferredSize(c);
		// Use inverted height & width.
		return new Dimension(size.height, size.width);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		JLabel label = (JLabel) c;
		String text = label.getText();
		Icon icon = (label.isEnabled()) ?
				label.getIcon() :
				label.getDisabledIcon();

		if (isNull(icon) && isNull(text)) {
			return;
		}

		FontMetrics fm = g.getFontMetrics();
		Insets insets = c.getInsets();

		Rectangle paintViewR = new Rectangle();
		paintViewR.x = insets.left;
		paintViewR.y = insets.top;

		// Use inverted height & width.
		paintViewR.height = c.getWidth() - (insets.left + insets.right);
		paintViewR.width = c.getHeight() - (insets.top + insets.bottom);

		Rectangle paintIconR = new Rectangle();
		Rectangle paintTextR = new Rectangle();

		String clippedText = layoutCL(label, fm, text, icon, paintViewR,
				paintIconR, paintTextR);

		Graphics2D g2 = (Graphics2D) g;
		AffineTransform transform = g2.getTransform();

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (clockwise) {
			g2.rotate(Math.PI / 2);
			g2.translate(0, -c.getWidth());
		}
		else {
			g2.rotate(-Math.PI / 2);
			g2.translate(-c.getHeight(), 0);
		}

		if (nonNull(icon)) {
			icon.paintIcon(c, g, paintIconR.x, paintIconR.y + insets.left);
		}
		if (nonNull(text)) {
			int textX = paintTextR.x;
			int textY = paintTextR.y + fm.getAscent() + insets.left;

			if (label.isEnabled()) {
				paintEnabledText(label, g, clippedText, textX, textY);
			}
			else {
				paintDisabledText(label, g, clippedText, textX, textY);
			}
		}

		g2.setTransform(transform);
	}
}
