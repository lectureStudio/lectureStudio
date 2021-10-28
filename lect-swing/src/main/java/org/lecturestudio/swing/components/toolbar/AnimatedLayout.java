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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A layout that smoothly animates components as they need to move from one
 * location to another.
 * <p>
 * Each animated movement is proportional to the distance that the component has
 * to cover, giving the impression of a decelerating animation as the object
 * approaches its destination.
 *
 */
public abstract class AnimatedLayout implements LayoutManager2 {

	/**
	 * This client property on the container resolves to a Timer used to animate the layout.
	 */
	private static final String PROPERTY_TIMER = "AnimatedLayout.timer";

	/**
	 * This client property for the parent maps to a Boolean indicating whether
	 * installation logic has ever been performed on the parent or not.
	 */
	private static final String PROPERTY_INITIALIZED = "AnimatedLayout#initialized";

	/**
	 * This client property for the parent maps to a Boolean indicating whether
	 * the layout should immediately (without animation) layout the container.
	 */
	private static final String PROPERTY_LAYOUT_IMMEDIATELY = "AnimatedLayout#layoutImmediately";

	/**
	 * This is the delay, in milliseconds, between adjustments.
	 */
	public static int DELAY = 25;

	ComponentListener componentListener = new ComponentAdapter() {

		@Override
		public void componentResized(ComponentEvent e) {
			JComponent jc = ((JComponent) e.getComponent());
			layoutContainerImmediately(jc);
		}

		@Override
		public void componentShown(ComponentEvent e) {
			JComponent jc = ((JComponent) e.getComponent());
			layoutContainerImmediately(jc);
		}

	};

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	class AdjustListener implements ActionListener {
		JComponent container;

		public AdjustListener(JComponent container) {
			this.container = container;
		}

		public void actionPerformed(ActionEvent e) {
			Timer timer = (Timer) e.getSource();

			boolean workToDo = false;

			synchronized (container.getTreeLock()) {
				Map<JComponent, Rectangle> destinationMap = getDestinationMap(container);
				for (Entry<JComponent, Rectangle> entry : destinationMap.entrySet()) {
					if (!nudge(entry.getKey(), entry.getValue())) {
						workToDo = true;
					}
				}
			}
			container.repaint();

			if (!workToDo)
				timer.stop();
		}
	}

	private static double sign(double d) {
		if (d < 0)
			return -1;
		if (d > 0)
			return 1;
		return 0;
	}

	private static double getDouble(JComponent c, String key) {
		Double d = (Double) c.getClientProperty(key);
		if (d == null)
			return 0;
		return d;
	}

	private static double limit(double v1, double limit) {
		if (limit < 0) {
			return Math.max(limit, v1);
		}
		return Math.min(limit, v1);

	}

	/**
	 * Nudge a component towards the destination.
	 *
	 * @param c  the component to nudge.
	 * @param dest the target bounds for the component.
	 * @return true when the component is at the desired location
	 */
	protected static boolean nudge(JComponent c, Rectangle dest) {
		Rectangle bounds = c.getBounds();

		String PROPERTY_LAST_DX = "AnimatedLayout.lastDX";
		double lastDX = getDouble(c, PROPERTY_LAST_DX);
		String PROPERTY_LAST_DY = "AnimatedLayout.lastDY";
		double lastDY = getDouble(c, PROPERTY_LAST_DY);
		String PROPERTY_LAST_DW = "AnimatedLayout.lastDW";
		double lastDW = getDouble(c, PROPERTY_LAST_DW);
		String PROPERTY_LAST_DH = "AnimatedLayout.lastDH";
		double lastDH = getDouble(c, PROPERTY_LAST_DH);

		double dx = dest.x - bounds.x;
		double dy = dest.y - bounds.y;
		double dw = dest.width - bounds.width;
		double dh = dest.height - bounds.height;

		dx = limit(.5 * sign(dx) * Math.pow(Math.abs(dx), .7) + .5 * lastDX, dx);
		dy = limit(.5 * sign(dy) * Math.pow(Math.abs(dy), .7) + .5 * lastDY, dy);
		dw = limit(.5 * sign(dw) * Math.pow(Math.abs(dw), .7) + .5 * lastDW, dw);
		dh = limit(.5 * sign(dh) * Math.pow(Math.abs(dh), .7) + .5 * lastDH, dh);

		c.putClientProperty(PROPERTY_LAST_DX, dx);
		c.putClientProperty(PROPERTY_LAST_DY, dy);
		c.putClientProperty(PROPERTY_LAST_DW, dw);
		c.putClientProperty(PROPERTY_LAST_DH, dh);

		if (Math.abs(dx) < 1.2 && Math.abs(dy) < 1.2 && Math.abs(dw) < 1.2 && Math.abs(dh) < 1.2) {
			c.setBounds(dest);
			return true;
		}

		bounds.x += (int) (dx + .5);
		bounds.y += (int) (dy + .5);
		bounds.width += (int) (dw + .5);
		bounds.height += (int) (dh + .5);

		c.setBounds(bounds);

		return false;
	}

	@Override
	public void layoutContainer(Container parent) {
		JComponent jc = (JComponent) parent;
		install(jc);
		Timer timer = (Timer) jc.getClientProperty(PROPERTY_TIMER);
		Boolean layoutImmediately = (Boolean) jc.getClientProperty(PROPERTY_LAYOUT_IMMEDIATELY);
		if (layoutImmediately == null)
			layoutImmediately = false;
		if (layoutImmediately) {
			layoutContainerImmediately(jc);
			if (parent.isShowing())
				jc.putClientProperty(PROPERTY_LAYOUT_IMMEDIATELY, false);
		} else {
			if (!timer.isRunning())
				timer.start();
		}
	}

	protected void layoutContainerImmediately(JComponent parent) {
		synchronized (parent.getTreeLock()) {
			Map<JComponent, Rectangle> destMap = getDestinationMap(parent);
			for (Entry<JComponent, Rectangle> entry : destMap.entrySet()) {
				entry.getKey().setBounds(entry.getValue());
			}
		}
		parent.repaint();
	}

	protected void install(JComponent parent) {
		Boolean initialized = (Boolean) parent.getClientProperty(PROPERTY_INITIALIZED);
		if (initialized == null)
			initialized = Boolean.FALSE;
		if (!initialized) {
			Timer timer = new Timer(DELAY, new AdjustListener(parent));
			parent.putClientProperty(PROPERTY_TIMER, timer);
			parent.putClientProperty(PROPERTY_INITIALIZED, true);
			parent.addComponentListener(componentListener);
			parent.addHierarchyListener(new HierarchyListener() {

				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					final JComponent parent = (JComponent) e.getComponent();
					if (parent.getLayout() == AnimatedLayout.this) {
						SwingUtilities.invokeLater(() -> {
							if (!parent.isShowing()) {
								// if we were hidden, then restore the layout to a "new" untouched state,
								// so the next time we're shown we don't animate.
								parent.putClientProperty(PROPERTY_LAYOUT_IMMEDIATELY, Boolean.TRUE);
							}
						});
					} else {
						// TODO: this isn't universally guaranteed to call
						// uninstall at the right time.
						uninstall(parent);
						parent.removeHierarchyListener(this);
					}
				}
			});
		}
	}

	protected void uninstall(JComponent parent) {
		parent.removeComponentListener(componentListener);
		parent.putClientProperty(PROPERTY_INITIALIZED, null);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int width = 0;
		int height = 0;
		synchronized (parent.getTreeLock()) {
			Map<JComponent, Rectangle> destMap = getDestinationMap((JComponent) parent);
			for (Entry<JComponent, Rectangle> entry : destMap.entrySet()) {
				width = Math.max(width, (int) (entry.getValue().getMaxX() + .5));
				height = Math.max(height, (int) (entry.getValue().getMaxY() + .5));
			}
		}
		return new Dimension(width, height);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	/**
	 * Return a map of components to their target size.
	 * This method should be called inside blocks of code synchronized against the container's tree lock.
	 */
	protected abstract Map<JComponent, Rectangle> getDestinationMap(JComponent container);

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return .5f;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return .5f;
	}

	@Override
	public void invalidateLayout(Container target) {
	}
}
