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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class CSliderUI extends BasicSliderUI {

	private final int thumbHeight = 11;
	private final int thumbWidth = 11;

	private MouseAdapter mouseListener;

	private ComponentAdapter resizeListener;


	public CSliderUI(JSlider slider) {
		super(slider);

		slider.setOpaque(false);
	}

	public void installUI(JComponent c) {
		super.installUI(c);

		initListeners();
		installListeners();
	}

	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);

		JSlider slider = (JSlider) c;
		slider.removeMouseListener(mouseListener);
		slider.removeMouseMotionListener(mouseListener);
		slider.removeComponentListener(resizeListener);

		uninstallListeners();
	}

	private void uninstallListeners() {
		resizeListener = null;
		mouseListener = null;
	}

	private void installListeners() {
		slider.addMouseListener(mouseListener);
		slider.addMouseMotionListener(mouseListener);
		slider.addComponentListener(resizeListener);
	}

	private void initListeners() {
		mouseListener = new MouseAdapter() {

			public void mouseDragged(MouseEvent e) {
				CSliderUI.this.slider.repaint();
			}

			public void mousePressed(MouseEvent e) {
				CSliderUI.this.slider.setValue(e.getX() - (thumbRect.width + 5) / 2);
			}
		};

		resizeListener = new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				int value = slider.getValue();
				float ratio = (trackRect.width - 1) / (float) slider.getMaximum();
				
				CSliderUI.this.slider.setMaximum(trackRect.width - 1);
				CSliderUI.this.slider.setValue((int) (value * ratio));
			}
		};
	}

	public double getNormalizedThumbPosition() {
		double pos = (thumbRect.x - 2.D) / (trackRect.width - 1.D);
		return pos;
	}

	@Override
	protected Dimension getThumbSize() {
		return new Dimension(thumbHeight, thumbWidth);
	}

	@Override
	public void paintThumb(Graphics g) {
		Rectangle knobBounds = thumbRect;

		int x = knobBounds.x;
		int y = slider.getHeight() / 2 - 5;

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(new Color(50, 50, 50));
		g2d.fillOval(x - 1, y, thumbWidth + 2, thumbHeight + 2);

		g2d.setColor(new Color(255, 255, 255));
		g2d.fillOval(x, y + 1, thumbWidth, thumbHeight);

		g2d.setColor(new Color(50, 90, 235));
		g2d.fillOval(x + 3, y + 4, 5, 5);
	}

	@Override
	public void paintTrack(Graphics g) {
		int center = slider.getHeight() / 2;
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(2, center - 2, slider.getWidth() - 5, 5, 4, 4);

		g.setColor(Color.GRAY);
		g.drawRoundRect(2, center - 2, slider.getWidth() - 5, 5, 4, 4);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(45, 135, 235));
		g2d.fillRect(4, center, thumbRect.x - 5, 2);
	}

}
