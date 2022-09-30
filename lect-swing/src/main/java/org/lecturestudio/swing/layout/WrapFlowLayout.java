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

package org.lecturestudio.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 *  FlowLayout subclass that supports wrapping of components.
 */
public class WrapFlowLayout extends FlowLayout {

	public WrapFlowLayout() {
		this(FlowLayout.LEFT);
	}

	public WrapFlowLayout(int align) {
		super(align);
	}

	public WrapFlowLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

	@Override
	public Dimension minimumLayoutSize(Container target) {
		// Size of largest component, so we can resize it in
		// either direction with something like a split-pane.
		return computeMinSize(target);
	}

	@Override
	public Dimension preferredLayoutSize(Container target) {
		return computeSize(target);
	}

	private Dimension computeSize(Container target) {
		synchronized (target.getTreeLock()) {
			int hgap = getHgap();
			int vgap = getVgap();
			int w = target.getWidth();

			// Let this behave like a regular FlowLayout (single row)
			// if the container hasn't been assigned any size yet.
			if (w == 0) {
				w = Integer.MAX_VALUE;
			}

			Insets insets = target.getInsets();
			if (insets == null) {
				insets = new Insets(0, 0, 0, 0);
			}
			
			int reqdWidth = 0;
			int maxwidth = w - (insets.left + insets.right + hgap * 2);
			int n = target.getComponentCount();
			int x = 0;
			int y = insets.top + vgap;
			int rowHeight = 0;

			for (int i = 0; i < n; i++) {
				Component c = target.getComponent(i);
				
				if (c.isVisible()) {
					Dimension d = c.getPreferredSize();
					
					if ((x == 0) || ((x + d.width) <= maxwidth)) {
						// Fits in current row.
						if (x > 0) {
							x += hgap;
						}
						x += d.width;
						rowHeight = Math.max(rowHeight, d.height);
					}
					else {
						// Start of new row.
						x = d.width;
						y += vgap + rowHeight;
						rowHeight = d.height;
					}
					reqdWidth = Math.max(reqdWidth, x);
				}
			}
			
			y += rowHeight;
			y += insets.bottom;
			
			return new Dimension(reqdWidth + insets.left + insets.right, y);
		}
	}

	private Dimension computeMinSize(Container target) {
		synchronized (target.getTreeLock()) {
			int minx = Integer.MAX_VALUE;
			int miny = Integer.MIN_VALUE;
			boolean found_one = false;
			int n = target.getComponentCount();

			for (int i = 0; i < n; i++) {
				Component c = target.getComponent(i);
				
				if (c.isVisible()) {
					found_one = true;
					Dimension d = c.getPreferredSize();
					minx = Math.min(minx, d.width);
					miny = Math.min(miny, d.height);
				}
			}
			
			if (found_one) {
				return new Dimension(minx, miny);
			}
			
			return new Dimension(0, 0);
		}
	}

}
