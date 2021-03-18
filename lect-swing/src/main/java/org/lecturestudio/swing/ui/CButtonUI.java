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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ButtonUI;

public class CButtonUI extends ButtonUI {

	private Color color1 = new Color(250, 250, 250);
	private Color color2 = new Color(230, 230, 230);
	private Color frameColor = Color.LIGHT_GRAY;

	private Color highlightColor1 = new Color(250, 250, 250);
	private Color highlightColor2 = new Color(185, 215, 250);
	private Color highlightFrameColor = new Color(125, 215, 250);

	private MouseListener mouseListener;

	private boolean mouseEntered = false;


	public void installUI(JComponent c) {
		super.installUI(c);

		c.setOpaque(true);
		installListeners(c);
	}

	public void uninstallUI(JComponent c) {
		c.setLayout(null);

		uninstallListeners(c);

		color1 = null;
		color2 = null;
		frameColor = null;

		highlightColor1 = null;
		highlightColor2 = null;
		highlightFrameColor = null;
	}

	public void installListeners(final JComponent c) {
		mouseListener = new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {
				mouseEntered = true;
				c.repaint();
			}

			public void mouseExited(MouseEvent e) {
				mouseEntered = false;
				c.repaint();
			}

			public void mousePressed(MouseEvent e) {
				JButton button = (JButton) c;
				button.doClick();
			}
		};

		c.addMouseListener(mouseListener);
	}

	public void uninstallListeners(JComponent c) {
		c.removeMouseListener(mouseListener);
		mouseListener = null;
	}

	public Dimension getPreferredSize(JComponent c) {
		AbstractButton button = (AbstractButton) c;
		Icon icon = button.getIcon();

		int width = 0;
		int height = 0;

		if (icon != null) {
			width = icon.getIconWidth();
			height = icon.getIconHeight();
		}

		String text = button.getText();
		if (text != null && !text.isEmpty()) {
			FontMetrics metrics = button.getFontMetrics(button.getFont());
			int stringWidth = metrics.stringWidth(text);
			int stringHeight = metrics.getHeight();

			int vTextPosition = button.getVerticalTextPosition();
			int hTextPosition = button.getHorizontalTextPosition();

			switch (vTextPosition) {
				case SwingConstants.TOP:
				case SwingConstants.BOTTOM:
					height += stringHeight + 2;
					break;
				case SwingConstants.CENTER:
					if (stringHeight > height)
						height = stringHeight;
					break;
			}

			switch (hTextPosition) {
				case SwingConstants.RIGHT:
				case SwingConstants.LEADING:
				case SwingConstants.TRAILING:
				case SwingConstants.LEFT:
					width += stringWidth;
					break;
				case SwingConstants.CENTER:
					if (stringWidth > width)
						width = stringWidth;
					break;
			}
		}

		Insets insets = button.getMargin();
		if (insets != null) {
			width += insets.left + insets.right;
			height += insets.top + insets.bottom;
		}

		return new Dimension(width + 4, height + 4);
	}

	public void paint(Graphics g, JComponent c) {
		AbstractButton button = (AbstractButton) c;
		Dimension size = button.getSize();
		Insets insets = button.getMargin();
		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Color fg = mouseEntered ? highlightColor1 : color1;
		Color bg = mouseEntered ? highlightColor2 : color2;
		Color frame = mouseEntered ? highlightFrameColor : frameColor;

		if (button.isOpaque() || mouseEntered) {
			float h = size.width / 2.F;
			Point2D center = new Point2D.Float(h, h);
			float radius = size.width - 1;
			float[] dist = { 0.0f, 0.5f };
			Color[] colors = { fg, bg };
			RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);

			g2d.setPaint(p);
			g2d.fillRoundRect(0, 0, size.width - 1, size.height - 1, 5, 5);

			g2d.setPaint(frame);
			g2d.drawRoundRect(0, 0, size.width - 1, size.height - 1, 5, 5);

			g2d.setPaint(new Color(225, 225, 225));
			g2d.drawRoundRect(1, 1, size.width - 3, size.height - 3, 5, 5);
		}

		int vTextPosition = button.getVerticalTextPosition();
		int hTextPosition = button.getHorizontalTextPosition();

		String text = button.getText();
		Icon icon = button.getIcon();

		if (icon != null) {
			int width = icon.getIconWidth();
			int height = icon.getIconHeight();

			int x = 2;
			int y = 2;

			if (text != null && !text.isEmpty()) {
				FontMetrics metrics = button.getFontMetrics(button.getFont());
				int stringWidth = metrics.stringWidth(text);
				int stringHeight = metrics.getHeight();

				switch (vTextPosition) {
					case SwingConstants.TOP:
						y += stringHeight;
						if (insets != null)
							y += insets.top;
						break;
					case SwingConstants.BOTTOM:
						if (insets != null)
							y += insets.top;
						break;
				}

				switch (hTextPosition) {
					case SwingConstants.RIGHT:
					case SwingConstants.LEADING:
						if (insets != null)
							x += insets.left;
						break;
					case SwingConstants.TRAILING:
					case SwingConstants.LEFT:
						x += stringWidth;
						if (insets != null)
							x += insets.left;
						break;
				}
			}
			else {
				x = (size.width - width) / 2;
				y = (size.height - height) / 2;
			}

			icon.paintIcon(button, g2d, x, y);
		}

		if (text != null && !text.isEmpty()) {
			FontMetrics metrics = button.getFontMetrics(button.getFont());
			int stringWidth = metrics.stringWidth(text);
			int stringHeight = metrics.getHeight();

			int x = 2;
			int y = 2;
			switch (vTextPosition) {
				case SwingConstants.TOP:
					y = (insets == null) ? stringHeight : insets.top;
					break;
				case SwingConstants.BOTTOM:
					y = size.height - 5;
					if (insets != null)
						y -= insets.bottom;
					break;
				case SwingConstants.CENTER:
					int offset = (insets == null) ? 0 : insets.top + insets.bottom;
					y += (size.height - offset) / 2;
					break;
			}

			switch (hTextPosition) {
				case SwingConstants.RIGHT:
				case SwingConstants.LEADING:
					x = size.width - stringWidth - 3;
					if (insets != null)
						x -= insets.right;
					break;
				case SwingConstants.TRAILING:
				case SwingConstants.LEFT:
					x = (insets == null) ? 3 : insets.left;
					break;
				case SwingConstants.CENTER:
					int offset = (insets == null) ? 0 : insets.left + insets.right;
					x = (size.width - stringWidth - offset) / 2;
					break;
			}

			g2d.setColor(button.getForeground());
			g2d.setFont(button.getFont());
			g2d.drawString(text, x, y);
		}
	}

}
