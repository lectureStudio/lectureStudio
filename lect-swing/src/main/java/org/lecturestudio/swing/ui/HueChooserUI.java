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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.lecturestudio.swing.components.HueChooser;


public class HueChooserUI extends ComponentUI {

	private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

	private GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

	private BufferedImage image;

	protected HueChooser hueChooser;

	protected ChooserThumb thumb1;

	protected ChooserThumb thumb2;

	protected ChooserMouseListener chooserListener = new ChooserMouseListener();

	protected ThumbMouseListener thumbListener = new ThumbMouseListener();

	protected ChangeListener changeListener;

	protected ComponentListener componentListener;


	public static ComponentUI createUI(JComponent c) {
		return new HueChooserUI();
	}

	public void installUI(JComponent c) {
		hueChooser = (HueChooser) c;
		hueChooser.setBorder(BorderFactory.createEmptyBorder());
		hueChooser.setLayout(null);
		hueChooser.setIgnoreRepaint(true);

		if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
			thumb1 = new ChooserThumb(SwingConstants.TOP);
			thumb2 = new ChooserThumb(SwingConstants.BOTTOM);
		}
		else {
			thumb1 = new ChooserThumb(SwingConstants.LEFT);
			thumb2 = new ChooserThumb(SwingConstants.RIGHT);
		}

		installComponents();
		installListeners();
	}

	public void uninstallUI(JComponent c) {
		c.setLayout(null);

		uninstallComponents();
		uninstallListeners();

		hueChooser = null;
	}

	public void installComponents() {
		hueChooser.add(thumb1);
		hueChooser.add(thumb2);

		layoutComponents();
	}

	public void installListeners() {
		thumb1.addMouseListener(thumbListener);
		thumb1.addMouseMotionListener(thumbListener);

		thumb2.addMouseListener(thumbListener);
		thumb2.addMouseMotionListener(thumbListener);

		hueChooser.addMouseListener(chooserListener);
		hueChooser.addMouseMotionListener(chooserListener);

		changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateComponents();
			}
		};

		componentListener = new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				layoutComponents();
				updateComponents();
			}
		};

		hueChooser.getModel().addChangeListener(changeListener);
		hueChooser.addComponentListener(componentListener);
	}

	public void uninstallComponents() {
		hueChooser.remove(thumb1);
		hueChooser.remove(thumb2);

		thumb1 = null;
		thumb2 = null;

		if (image != null)
			image.flush();
		image = null;
	}

	public void uninstallListeners() {
		thumb1.removeMouseListener(thumbListener);
		thumb1.removeMouseMotionListener(thumbListener);

		thumb2.removeMouseListener(thumbListener);
		thumb2.removeMouseMotionListener(thumbListener);

		hueChooser.removeMouseListener(chooserListener);
		hueChooser.removeMouseMotionListener(chooserListener);

		hueChooser.getModel().removeChangeListener(changeListener);
		hueChooser.removeComponentListener(componentListener);

		changeListener = null;
		componentListener = null;
	}

	private void createBufferedImage() {
		Dimension size = hueChooser.getPreferredSize();

		if (size.height > 0 && size.width > 0) {
			if (image == null) {
				image = gc.createCompatibleImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
				image.setAccelerationPriority(1.0f);
			}

			Graphics2D g2d = image.createGraphics();

			int offset = thumb1.getSize().width / 2;
			float hue = 0;

			if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
				for (int x = offset; x < size.width - offset; x++) {
					for (int y = 0; y < size.height - 5; y++) {
						g2d.setPaint(Color.getHSBColor(hue, 1, 1));
						g2d.drawLine(x, y + 3, x, y + 3);
					}
					hue = x / (float) (size.width - 2 * offset);
				}

				g2d.setPaint(Color.GRAY);
				g2d.drawRect(offset, 3, size.width - 2 * offset, size.height - 6);
			}
			else {
				for (int x = 0; x < size.width - offset - 2; x++) {
					for (int y = offset; y < size.height - offset; y++) {
						g2d.setPaint(Color.getHSBColor(hue, 1, 1));
						g2d.drawLine(x + 3, y, x + 3, y);

						hue = y / (float) (size.height - 2 * offset);
					}
				}

				g2d.setPaint(Color.GRAY);
				g2d.drawRect(3, offset, size.width - offset - 2, size.height - 2 * offset);
			}

			g2d.dispose();
		}
	}

	private void layoutComponents() {
		Dimension size = hueChooser.getPreferredSize();

		Dimension thumbSize = thumb1.getSize();
		if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
			thumb1.setBounds(new Rectangle(0, 0, thumbSize.width, thumbSize.height));
			thumb2.setBounds(new Rectangle(0, size.height - 5, thumbSize.width, thumbSize.height));
		}
		else {
			thumb1.setBounds(new Rectangle(0, 0, thumbSize.width, thumbSize.height));
			thumb2.setBounds(new Rectangle(size.width - 5, 0, thumbSize.width, thumbSize.height));
		}
	}

	private void updateComponents() {
		Dimension size = hueChooser.getPreferredSize();
		float hue = hueChooser.getModel().getValue();

		if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
			int offset = thumb1.getSize().width / 2;
			int x = (int) (hue * (size.width - 2 * offset));
			thumb1.setLocation(x, thumb1.getLocation().y);
			thumb2.setLocation(x, thumb2.getLocation().y);
		}
		else {
			int offset = thumb1.getSize().height / 2;
			int y = (int) (hue * (size.height - 2 * offset));
			thumb1.setLocation(thumb1.getLocation().x, y);
			thumb2.setLocation(thumb2.getLocation().x, y);
		}
	}

	private void updateModel(int value) {
		Dimension size = hueChooser.getPreferredSize();
		float hue = 0;
		boolean horizontal = hueChooser.getOrientation() == SwingConstants.HORIZONTAL;

		if (value < 0)
			value = 0;

		if (value > size.width - thumb1.getSize().width && horizontal)
			value = size.width - thumb1.getSize().width;

		if (value > (size.height - thumb1.getSize().height) && !horizontal)
			value = size.height - thumb1.getSize().height;

		if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
			int offset = thumb1.getSize().width / 2;
			hue = value / (float) (size.width - 2 * offset);
		}
		else {
			int offset = thumb1.getSize().height / 2;
			hue = value / (float) (size.height - 2 * offset);
		}

		hueChooser.getModel().setValue(hue);
	}

	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Graphics2D g2d = (Graphics2D) g;

		if (image == null)
			createBufferedImage();

		g2d.drawImage(image, 0, 0, null);
	}


	private class ChooserThumb extends JComponent {

		private static final long serialVersionUID = 6721961977279281693L;

		private Polygon polygon;

		private Dimension size = new Dimension(10, 10);


		public ChooserThumb(int orientation) {
			polygon = new Polygon();

			switch (orientation) {
				case SwingConstants.TOP:
					polygon.addPoint(0, 0);
					polygon.addPoint(10, 0);
					polygon.addPoint(5, 5);
					break;
				case SwingConstants.BOTTOM:
					polygon.addPoint(0, 5);
					polygon.addPoint(10, 5);
					polygon.addPoint(5, 0);
					break;
				case SwingConstants.LEFT:
					polygon.addPoint(0, 0);
					polygon.addPoint(0, 10);
					polygon.addPoint(5, 5);
					break;
				case SwingConstants.RIGHT:
					polygon.addPoint(5, 0);
					polygon.addPoint(5, 10);
					polygon.addPoint(0, 5);
					break;
			}

			setOpaque(false);
		}

		@Override
		public void update(Graphics g) {
			paint(g);
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.fillPolygon(polygon);
		}

		@Override
		public Dimension getSize() {
			return size;
		}

		@Override
		public void setLocation(int x, int y) {
			JComponent parent = (JComponent) this.getParent();
			Dimension parentSize = parent.getPreferredSize();

			if (x < 0)
				x = 0;
			else if (y < 0)
				y = 0;
			else if (x > parentSize.width)
				x = parentSize.width;
			else if (y > parentSize.height)
				y = parentSize.height;

			super.setLocation(x, y);
		}

	}

	private class ThumbMouseListener extends MouseAdapter implements MouseMotionListener {

		private int lastX = 0;

		private int lastY = 0;


		@Override
		public void mouseDragged(MouseEvent e) {
			ChooserThumb thumb = (ChooserThumb) e.getSource();
			Point location = thumb.getLocation();
			int value = 0;

			if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
				value = location.x + e.getX() - lastX;
			}
			else {
				value = location.y + e.getY() - lastY;
			}

			updateModel(value);
		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

	}

	private class ChooserMouseListener extends MouseAdapter implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			setThumbLocation(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
			setThumbLocation(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		private void setThumbLocation(MouseEvent e) {
			int value = 0;

			if (hueChooser.getOrientation() == SwingConstants.HORIZONTAL) {
				int offset = thumb1.getSize().width / 2;
				value = e.getX() - offset;
			}
			else {
				int offset = thumb1.getSize().height / 2;
				value = e.getY() - offset;
			}

			updateModel(value);
		}

	}

}
