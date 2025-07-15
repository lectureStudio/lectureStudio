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

import static java.util.Objects.isNull;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SizeRequirements;

import org.lecturestudio.core.app.configuration.ScreenConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.swing.util.SwingUtils;

public class DisplayPanel extends JPanel implements ItemSelectable {

	private final List<ItemListener> listeners;

	private List<ScreenConfiguration> screens;


	public DisplayPanel() {
		super();

		listeners = new ArrayList<>();

		initialize();
	}

	@Override
	public Object[] getSelectedObjects() {
		return null;
	}

	@Override
	public void addItemListener(ItemListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeItemListener(ItemListener listener) {
		listeners.remove(listener);
	}

	public void setScreens(List<ScreenConfiguration> screens) {
		if (screens.isEmpty()) {
			return;
		}

		this.screens = screens;

		addScreens(screens);

		revalidate();
		repaint();
	}

	private void addScreens(List<ScreenConfiguration> screens) {
		for (int i = 0; i < screens.size(); i++) {
			final ScreenConfiguration screenConfig = screens.get(i);

			ScreenComponent screenComponent = new ScreenComponent(screenConfig, Integer.toString(i));
			screenComponent.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					ItemEvent event = new ItemEvent(DisplayPanel.this,
							ItemEvent.ITEM_STATE_CHANGED, screenConfig.getScreen(),
							ItemEvent.SELECTED);

					for (ItemListener listener : listeners) {
						listener.itemStateChanged(event);
					}
				}
			});

			SwingUtils.bindBidirectional(screenComponent, screenConfig.enabledProperty());

			add(screenComponent);
		}
	}

	private void initialize() {
		setLayout(new DisplayLayoutManager(0));
	}



	private static class ScreenComponent extends JToggleButton {

		private final ScreenConfiguration screenConfig;


		ScreenComponent(ScreenConfiguration screenConfig, String idText) {
			super(idText);

			this.screenConfig = screenConfig;
		}

		Rectangle2D getScreenBounds() {
			return screenConfig.getScreen().getBounds();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(isSelected() ? Color.WHITE : Color.LIGHT_GRAY);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			// Draw screen id.
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			FontMetrics metrics = g.getFontMetrics();
			Rectangle rect = getBounds();

			float x = (rect.width - metrics.stringWidth(getText())) >> 1;
			float y = ((rect.height - metrics.getHeight()) >> 1) + metrics.getAscent();

			g2d.setColor(Color.DARK_GRAY);
			g2d.drawString(getText(), x, y);

			// Draw screen dimension.
			Rectangle2D screenBounds = screenConfig.getScreen().getBounds();
			Font font = g.getFont().deriveFont(g.getFont().getSize2D() / 2);
			double width = screenBounds.getWidth();
			double height = screenBounds.getHeight();
			x = 10;
			y = 10 + font.getSize2D();

			g2d.setFont(font);
			g2d.setColor(Color.DARK_GRAY);
			g2d.drawString(String.format("%.0fx%.0f", width, height), x, y);
		}
	}



	private class DisplayLayoutManager implements LayoutManager2 {

		private final double padding;

		private SizeRequirements[] xChildren;
		private SizeRequirements[] yChildren;
		private SizeRequirements xTotal;
		private SizeRequirements yTotal;


		DisplayLayoutManager(double padding) {
			this.padding = padding;
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			invalidateLayout(comp.getParent());
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			invalidateLayout(comp.getParent());
		}

		@Override
		public void addLayoutComponent(Component comp, Object constraints) {
			invalidateLayout(comp.getParent());
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			checkRequests(parent);

			Insets insets = parent.getInsets();
			Dimension size = new Dimension(xTotal.maximum, yTotal.maximum);
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;

			return size;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			checkRequests(parent);

			Insets insets = parent.getInsets();
			Dimension size = new Dimension(xTotal.minimum, yTotal.minimum);
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;

			return size;
		}

		@Override
		public Dimension maximumLayoutSize(Container parent) {
			checkRequests(parent);

			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		@Override
		public void layoutContainer(Container parent) {
			Insets insets = parent.getInsets();
			int contentWidth = parent.getWidth() - (insets.left + insets.right);
			int contentHeight = parent.getHeight() - (insets.top + insets.bottom);

			if (screens.isEmpty()) {
				return;
			}

			// Find the bounding box of all screens
			double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

			for (ScreenConfiguration screen : screens) {
				Rectangle2D bounds = screen.getScreen().getBounds();

				minX = Math.min(minX, bounds.getX());
				minY = Math.min(minY, bounds.getY());
				maxX = Math.max(maxX, bounds.getX() + bounds.getWidth());
				maxY = Math.max(maxY, bounds.getY() + bounds.getHeight());
			}

			double totalWidth = maxX - minX;
			double totalHeight = maxY - minY;

			double scaleFactor = 1.0;

			// Calculate scale to fit in the component with padding.
			double scaleX = (contentWidth - 2 * padding) / totalWidth;
			double scaleY = (contentHeight - 2 * padding) / totalHeight;
			scaleFactor = Math.min(scaleX, scaleY);

			// Calculate offset to center the screens.
			double scaledWidth = totalWidth * scaleFactor;
			double scaledHeight = totalHeight * scaleFactor;

			int centerX = (int) ((contentWidth - scaledWidth) / 2 - minX * scaleFactor);
			int centerY = (int) ((contentHeight - scaledHeight) / 2 - minY * scaleFactor);

			for (int i = 0; i < parent.getComponentCount(); i++) {
				ScreenComponent c = (ScreenComponent) parent.getComponent(i);

				if (c.isVisible()) {
					Rectangle2D bounds = c.getScreenBounds();

					int x = (int) (bounds.getX() * scaleFactor);
					int y = (int) (bounds.getY() * scaleFactor);
					int w = (int) (bounds.getWidth() * scaleFactor);
					int h = (int) (bounds.getHeight() * scaleFactor);

					c.setBounds(x + centerX, y + centerY, w, h);
					c.setFont(c.getFont().deriveFont((float) (parent.getFont().getSize2D() * scaleFactor * 25)));
				}
			}
		}

		@Override
		public float getLayoutAlignmentX(Container parent) {
			checkRequests(parent);

			return xTotal.alignment;
		}

		@Override
		public float getLayoutAlignmentY(Container parent) {
			checkRequests(parent);

			return yTotal.alignment;
		}

		@Override
		public void invalidateLayout(Container parent) {
			xChildren = null;
			yChildren = null;
			xTotal = null;
			yTotal = null;
		}

		private void checkRequests(Container target) {
			if (isNull(xChildren) || isNull(yChildren)) {
				int n = target.getComponentCount();
				xChildren = new SizeRequirements[n];
				yChildren = new SizeRequirements[n];

				for (int i = 0; i < n; i++) {
					Component c = target.getComponent(i);
					Dimension min = c.getMinimumSize();
					Dimension typ = c.getPreferredSize();
					Dimension max = c.getMaximumSize();

					xChildren[i] = new SizeRequirements(min.width, typ.width,
							max.width, c.getAlignmentX());
					yChildren[i] = new SizeRequirements(min.height, typ.height,
							max.height, c.getAlignmentY());
				}

				xTotal = SizeRequirements.getAlignedSizeRequirements(xChildren);
				yTotal = SizeRequirements.getAlignedSizeRequirements(yChildren);
			}
		}
	}
}
