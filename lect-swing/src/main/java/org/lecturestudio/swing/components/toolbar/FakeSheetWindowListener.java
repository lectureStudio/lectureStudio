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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * <P>
 * This listens to the parent window and makes important changes to the child window to best emulate a "sheet-like" dialog.
 * <P>
 * If the parent is resized: then the dialog needs to be repositioned and the modal cover needs to be resized.
 * <P>
 * When the parent window is dragged, this drags the other window an identical (+dx, +dy).
 * <P>
 * This class is flawed, though, because on Mac the componentResized events are coalesced together,
 * and therefore the second window moves in spurts and jumps.
 * But I don't see how else to implement this without actual sheets.
 */
public class FakeSheetWindowListener implements ComponentListener {
	private Point lastLocation;
	private final Window window1;
	private final Window window2;
	private final JComponent modalCover;
	private final JComponent dialogAnchor;

	public FakeSheetWindowListener(Window window1, Window window2, JComponent dialogAnchor, JComponent modalCover) {
		lastLocation = window1.getLocation();
		this.window1 = window1;
		this.window2 = window2;
		this.modalCover = modalCover;
		this.dialogAnchor = dialogAnchor;
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
		Point newLocation = window1.getLocation();

		translate(window2, newLocation.x - lastLocation.x, newLocation.y - lastLocation.y);

		lastLocation = newLocation;
	}

	/** Translate a window (+dx, +dy) */
	public static void translate(Window w, int dx, int dy) {
		Point p = w.getLocation();
		p.x += dx;
		p.y += dy;
		w.setLocation(p);
	}

	public void componentResized(ComponentEvent e) {
		modalCover.setSize(window1.getSize());
		repositionDialog();
	}

	protected void repositionDialog() {
		Point topLeft = new Point(0, 0);
		topLeft = SwingUtilities.convertPoint(dialogAnchor, topLeft, window1);
		int x = window1.getX() - window2.getWidth() / 2 + dialogAnchor.getWidth() / 2 + topLeft.x;
		int y = topLeft.y + dialogAnchor.getHeight() + 1 + window1.getY();
		Rectangle optionsBounds = new Rectangle(x, y, window2.getWidth(), window2.getHeight());
		SwingUtilities.convertRectangle(dialogAnchor, optionsBounds, window1);
		window2.setBounds(optionsBounds);
	}

	public void componentShown(ComponentEvent e) {
	}
}
