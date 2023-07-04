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

package org.lecturestudio.presenter.swing.input;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.stylus.StylusCursor;
import org.lecturestudio.stylus.StylusEvent;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.components.SlideView;

public class StylusListener implements org.lecturestudio.stylus.StylusListener {

	private static final Point HOTSPOT = new Point(16, 15);

	private final StylusHandler handler;

	private final SlideView slideView;

	private final Rectangle2D viewBounds;

	private final Image penCursorImage;

	private StylusCursor stylusCursor;


	public StylusListener(StylusHandler handler, SlideView slideView) {
		this.handler = handler;
		this.slideView = slideView;
		this.viewBounds = new Rectangle2D();

		penCursorImage = AwtResourceLoader.getImage("gfx/icons/pen-cursor.png");

		slideView.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				handler.onCursorEntered();
			}
		});
		slideView.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle canvasBounds = slideView.getCanvasBounds();

				viewBounds.setLocation(canvasBounds.getMinX(), canvasBounds.getMinY());
				viewBounds.setSize(canvasBounds.getWidth(), canvasBounds.getHeight());
			}
		});
	}

	@Override
	public void onCursorChange(StylusEvent stylusEvent) {
		stylusCursor = stylusEvent.getCursor();

		Cursor cursor;

		if (stylusEvent.getCursor() == StylusCursor.PEN || stylusEvent.getCursor() == StylusCursor.ERASER) {
			cursor = Toolkit.getDefaultToolkit().createCustomCursor(penCursorImage, HOTSPOT, "Stroke");
		}
		else {
			cursor = Cursor.getDefaultCursor();
		}

		slideView.setCursor(cursor);
	}

	@Override
	public void onCursorMove(StylusEvent stylusEvent) {
		if (stylusCursor != stylusEvent.getCursor()) {
			onCursorChange(stylusEvent);
		}

		handler.onCursorMove(stylusEvent, viewBounds);
	}

	@Override
	public void onButtonDown(StylusEvent stylusEvent) {
		if (stylusCursor != stylusEvent.getCursor()) {
			onCursorChange(stylusEvent);
		}

		handler.onButtonDown(stylusEvent, viewBounds);
	}

	@Override
	public void onButtonUp(StylusEvent stylusEvent) {
		if (stylusCursor != stylusEvent.getCursor()) {
			onCursorChange(stylusEvent);
		}

		handler.onButtonUp(stylusEvent, viewBounds);
	}
}
