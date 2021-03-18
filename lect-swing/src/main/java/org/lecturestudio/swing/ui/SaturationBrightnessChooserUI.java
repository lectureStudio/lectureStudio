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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.lecturestudio.swing.components.SaturationBrightnessChooser;
import org.lecturestudio.swing.model.SaturationBrightnessChooserModel;

public class SaturationBrightnessChooserUI extends ComponentUI {

	protected SaturationBrightnessChooser sbChooser;

	protected ChangeListener changeListener;

	protected ChooserMouseListener chooserListener = new ChooserMouseListener();

	private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

	private GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

	private BufferedImage image;

	private int x = 0;

	private int y = 0;

	private float hue = 0;


	public void installUI(JComponent c) {
		sbChooser = (SaturationBrightnessChooser) c;
		sbChooser.setBorder(BorderFactory.createEmptyBorder());
		sbChooser.setLayout(null);
		sbChooser.setIgnoreRepaint(true);

		installComponents();
		installListeners();
	}

	public void uninstallUI(JComponent c) {
		c.setLayout(null);

		uninstallComponents();
		uninstallListeners();

		sbChooser = null;
	}

	public void installComponents() {

	}

	public void installListeners() {
		sbChooser.addMouseListener(chooserListener);
		sbChooser.addMouseMotionListener(chooserListener);

		changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateComponents();
			}
		};
		sbChooser.getModel().addChangeListener(changeListener);
	}

	public void uninstallComponents() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}

	public void uninstallListeners() {
		sbChooser.removeMouseListener(chooserListener);
		sbChooser.removeMouseMotionListener(chooserListener);

		sbChooser.getModel().removeChangeListener(changeListener);

		changeListener = null;
	}

	private void updateComponents() {
		SaturationBrightnessChooserModel model = sbChooser.getModel();
		Dimension size = sbChooser.getPreferredSize();

		x = (int) (model.getSaturation() * size.width);
		y = (int) ((1f - model.getBrightness()) * size.height);

		if (hue != model.getHue()) {
			hue = model.getHue();
			createBufferedImage();
		}

		sbChooser.repaint();
	}

	private void updateModel(int x, int y) {
		SaturationBrightnessChooserModel model = sbChooser.getModel();
		Dimension size = sbChooser.getPreferredSize();

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (x > size.width)
			x = size.width;
		if (y > size.height)
			y = size.height;

		float saturation = (float) x / (float) size.width;
		float brightness = 1f - (float) y / (float) size.height;

		model.setSaturation(saturation);
		model.setBrightness(brightness);
	}

	public void paint(Graphics g, JComponent c) {
		Dimension size = c.getPreferredSize();
		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (image == null) {
			createBufferedImage();
		}

		g2d.drawImage(image, 0, 0, null);

		paintCursor(g2d, x, y, 5);

		g2d.setPaint(Color.GRAY);
		g2d.drawRect(0, 0, size.width - 1, size.height - 1);
	}

	private void paintCursor(Graphics2D g2d, int x, int y, int radius) {
		g2d.setPaint(Color.DARK_GRAY);
		g2d.drawOval(x - radius - 1, y - radius - 1, 2 * (radius + 1), 2 * (radius + 1));

		g2d.setPaint(Color.WHITE);
		g2d.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
	}

	private void createBufferedImage() {
		Dimension size = sbChooser.getPreferredSize();

		if (size.height > 0 && size.width > 0) {
			if (image != null) {
				image.flush();
				image = null;
			}

			image = gc.createCompatibleImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			image.setAccelerationPriority(1.0f);

			Graphics2D g2d = image.createGraphics();

			float saturation = 0f;
			float brightness = 1f;

			for (int x = 0; x < size.width; x++) {
				brightness = 1f;
				for (int y = 0; y < size.height; y++) {
					g2d.setPaint(Color.getHSBColor(hue, saturation, brightness));
					g2d.drawLine(x, y, x, y);

					brightness -= 1f / size.height;
				}
				saturation += 1f / size.width;
			}

			g2d.dispose();
		}
	}


	private class ChooserMouseListener extends MouseAdapter implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			updateModel(e.getX(), e.getY());
		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
			updateModel(e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

	}

}
