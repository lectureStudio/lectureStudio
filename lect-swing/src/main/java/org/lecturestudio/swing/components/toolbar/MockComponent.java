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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

/**
 * This component stores an image of another component, and displays only that image.
 * <P>
 * This is useful as a proxy/substitute for existing components that already have a permanent home in the UI.
 */
public class MockComponent extends JComponent {

	private static final long serialVersionUID = -8363030015091166404L;

	private final BufferedImage image;

	/**
	 * Creates a MockComponent that resembles the argument component.
	 * <P>
	 * Note this method will traverse c and its subcomponents and may temporarily change properties of inner components:
	 * such as the focused state, the visibility, etc.
	 * <P>
	 * The goal is of this component is not to mirror the exact state of a component, but rather to provide a sample
	 * image of this component in its plain, unmodified, unused state.
	 */
	public MockComponent(JComponent c) {
		Dimension preferredSize = c.getPreferredSize();
		Dimension currentSize = c.getSize();

		Dimension d = new Dimension(Math.max(preferredSize.width, currentSize.width), Math.max(preferredSize.height, currentSize.height));

		if (currentSize.width == 0 || currentSize.height == 0) {
			// if the component isn't visible yet
			c.setSize(d);
			c.doLayout();
		}

		storeState(c);

		image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, d.width, d.height);
		g.setComposite(AlphaComposite.SrcOver);

		c.paint(g);
		g.dispose();

		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		setOpaque(c.isOpaque());
		setName(c.getName());
		setToolTipText(c.getToolTipText());

		restoreState(c);
	}

	public BufferedImage getBufferedImage() {
		return image;
	}

	private static final String WAS_SELECTED = "MockComponent.wasSelected";
	private static final String WAS_FOCUS_PAINTED = "MockComponent.wasFocusPainted";
	private static final String WAS_ENABLED = "MockComponent.wasEnabled";
	private static final String WAS_VISIBLE = "MockComponent.wasVisible";

	/**
	 * Temporarily massage this component, so it is visible, enabled, unselected, unfocused, etc.
	 */
	private void storeState(JComponent c) {
		if (c instanceof AbstractButton) {
			AbstractButton b = (AbstractButton) c;
			b.putClientProperty(WAS_SELECTED, b.isSelected());
			b.putClientProperty(WAS_FOCUS_PAINTED, b.isFocusPainted());
//			b.setSelected(false);
			b.setFocusPainted(false);
			b.setBackground(Color.decode("#F0F0F0"));
		}
		if (!c.isEnabled()) {
			c.putClientProperty(WAS_ENABLED, c.isEnabled());
			c.setEnabled(true);
		}
		if (!c.isVisible()) {
			c.putClientProperty(WAS_VISIBLE, c.isVisible());
			c.setVisible(true);
		}
		for (int a = 0; a < c.getComponentCount(); a++) {
			if (c.getComponent(a) instanceof JComponent) {
				storeState((JComponent) c.getComponent(a));
			}
		}
	}

	/** Restore this component back to its original goodness. */
	private void restoreState(JComponent c) {
		if (c instanceof AbstractButton) {
			AbstractButton b = (AbstractButton) c;
			if (b.getClientProperty(WAS_SELECTED) != null) {
//				b.setSelected((Boolean) b.getClientProperty(WAS_SELECTED));
				b.putClientProperty(WAS_SELECTED, null);
			}
			if (b.getClientProperty(WAS_FOCUS_PAINTED) != null) {
				b.setFocusPainted((Boolean) b.getClientProperty(WAS_FOCUS_PAINTED));
				b.putClientProperty(WAS_FOCUS_PAINTED, null);
			}
			b.setBackground(null);
		}
		if (c.getClientProperty(WAS_ENABLED) != null) {
			c.setEnabled((Boolean) c.getClientProperty(WAS_ENABLED));
			c.putClientProperty(WAS_ENABLED, null);
		}
		if (c.getClientProperty(WAS_VISIBLE) != null) {
			c.setVisible((Boolean) c.getClientProperty(WAS_VISIBLE));
			c.putClientProperty(WAS_VISIBLE, null);
		}
		for (int a = 0; a < c.getComponentCount(); a++) {
			if (c.getComponent(a) instanceof JComponent) {
				restoreState((JComponent) c.getComponent(a));
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(image, getWidth() / 2 - image.getWidth() / 2, getHeight() / 2 - image.getHeight() / 2, null);
	}
}
