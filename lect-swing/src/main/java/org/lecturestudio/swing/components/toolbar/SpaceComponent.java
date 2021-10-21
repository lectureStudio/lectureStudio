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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

/**
 * This is a component that represents padding in the CustomizedToolbar.
 */
public class SpaceComponent extends JComponent {

	private static final long serialVersionUID = 5357317835625217198L;

	public static final int SPACE_COMPONENT_WIDTH = 32;

	private final CustomizedToolbar toolbar;
	private boolean showBorder = false;
	private final boolean paintArrows;

	/**
	 * Creates a new SpaceComponent
	 *
	 * @param tb
	 * 				the toolbar this gap is associated with. This component listens to the toolbar for a specific property:
	 *              when that property is changed, this component may choose to paint itself
	 *              (such as when borders/arrows are necessary).
	 * @param paintArrows
	 * 				whether the arrows should be painted or not.
	 */
	public SpaceComponent(CustomizedToolbar tb, boolean paintArrows) {
		toolbar = tb;
		setPreferredSize(new Dimension(SPACE_COMPONENT_WIDTH, toolbar.minimumHeight));
		setSize(getPreferredSize());
		updateBorder();
		PropertyChangeListener propertyListener = evt -> updateBorder();
		tb.addPropertyChangeListener(CustomizedToolbar.DIALOG_ACTIVE, propertyListener);
		this.paintArrows = paintArrows;
	}

	private void updateBorder() {
		Boolean b = (Boolean) toolbar.getClientProperty(CustomizedToolbar.DIALOG_ACTIVE);
		if (b == null)
			b = Boolean.FALSE;
		showBorder = b;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (showBorder) {
			paintSpace(g, paintArrows, getWidth(), getHeight());
		}
	}

	/**
	 * Paints the visual elements of a SpaceComponent.
	 *
	 * @param g the Graphics to draw to
	 * @param drawArrows whether arrows should be painted
	 * @param w the width to paint
	 * @param h the height to paint
	 */
	protected static void paintSpace(Graphics g, boolean drawArrows, int w, int h) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color(0, 0, 0, 80));
		g2.setStroke(new BasicStroke(1));
		g2.drawRect(0, 0, w - 1, h - 1);

		GeneralPath path = new GeneralPath();
		if (drawArrows) {
			g2.setColor(new Color(0, 0, 0, 180));

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			path.reset();
			path.moveTo(0, h / 2 + .5f);
			path.lineTo(6, h / 2 - 2);
			path.lineTo(6, h / 2 + 3);
			path.lineTo(0, h / 2 + .5f);
			g2.fill(path);

			path.reset();
			path.moveTo(w - 1, h / 2 + .5f);
			path.lineTo(w - 7, h / 2 - 2);
			path.lineTo(w - 7, h / 2 + 3);
			path.lineTo(w - 1, h / 2 + .5f);
			g2.fill(path);

			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[] { 1, 2 }, 0));
			path.reset();
			path.moveTo(7, h / 2);
			path.lineTo(w - 8, h / 2);
			g2.draw(path);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}
}
