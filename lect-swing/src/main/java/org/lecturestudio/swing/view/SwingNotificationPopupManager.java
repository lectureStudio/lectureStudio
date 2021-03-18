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

package org.lecturestudio.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.swing.util.SwingUtils;

/**
 * Application-wide Notification Manager which shows important messages to the
 * user. This manager will restrain new notifications, if the threshold of
 * maximum visible notifications is reached. Restrained notifications appear
 * after all previous notifications disappeared.
 *
 * @author Alex Andres
 */
public class SwingNotificationPopupManager implements NotificationPopupManager {

	/** Visible notifications. */
	private final Map<Position, List<JWindow>> popupMap = new HashMap<>();

	/** Notifications hold back, if MAX_VISIBLE_NOTIFICATIONS reached. */
	private final Map<Position, List<BacklogEntry>> backlog = new HashMap<>();


	@Override
	public void show(View rootView, NotificationPopupView view) {
		if (!SwingUtils.isComponent(rootView)) {
			throw new RuntimeException("Root view has to be an instance of java.awt.Component");
		}
		if (!SwingUtils.isComponent(view)) {
			throw new RuntimeException("View has to be an instance of java.awt.Component");
		}

		final Component rootComponent = (Component) rootView;
		final Component viewComponent = (Component) view;
		final Position pos = view.getPosition();

		SwingUtils.invoke(() -> {
			show(rootComponent, viewComponent, pos, Duration.ZERO);
		});
	}

	private void show(Component root, Component component, Position position, Duration delay) {
		JWindow popup = new JWindow(SwingUtilities.getWindowAncestor(component));
		popup.setContentPane((Container) component);
		popup.setSize(component.getPreferredSize());
		popup.setAlwaysOnTop(true);

		Rectangle contentBounds = new Rectangle(0, 0, component.getPreferredSize().width, component.getPreferredSize().height);
		Point rootLoc = root.getLocationOnScreen();
		Rectangle rootBounds = new Rectangle(rootLoc.x, rootLoc.y, root.getWidth(), root.getHeight());
		Point2D location = getLocation(contentBounds, rootBounds, position);

		if (location.getY() + contentBounds.getHeight() > rootBounds.getMinY() + rootBounds.getHeight() ||
				location.getY() < rootBounds.getMinY()) {
			popup.setVisible(false);
			addBacklog(new BacklogEntry(root, component, position));
			return;
		}

		popup.setLocation((int) location.getX(), (int) location.getY());
		popup.setVisible(true);

		addPopup(popup, position);

		createHideAnimation(popup, Duration.ofSeconds(2), position);
	}

	private void showBacklog(Position position) {
		List<BacklogEntry> entries = backlog.get(position);

		if (nonNull(entries) && !entries.isEmpty()) {
			ListIterator<BacklogEntry> iter = entries.listIterator();
			int i = 0;

			while (iter.hasNext()){
				BacklogEntry entry = iter.next();
				iter.remove();

				show(entry.root, entry.component, entry.position, Duration.ofMillis(i++ * 500));
			}
		}
	}

	private void hide(JWindow popup, Position position) {
		popup.setVisible(false);

		removePopup(popup, position);

		List<JWindow> popups = popupMap.get(position);

		if (nonNull(popups) && popups.isEmpty()) {
			showBacklog(position);
		}
	}

	private void addPopup(JWindow popup, Position position) {
		List<JWindow> popups = popupMap.get(position);

		if (isNull(popups)) {
			popups = new ArrayList<>();
			popupMap.put(position, popups);
		}

		popups.add(popup);
	}

	private void removePopup(JWindow popup, Position position) {
		List<JWindow> popups = popupMap.get(position);

		if (nonNull(popups)) {
			popups.remove(popup);
		}
	}

	private void addBacklog(BacklogEntry entry) {
		List<BacklogEntry> entries = backlog.get(entry.position);

		if (isNull(entries)) {
			entries = new ArrayList<>();
			backlog.put(entry.position, entries);
		}

		entries.add(entry);
	}

	private Point2D getLocation(Rectangle contentBounds, Rectangle rootBounds, Position position) {
		Point2D location = getInitialLocation(contentBounds, rootBounds, position);
		List<JWindow> popups = popupMap.get(position);

		if (isNull(popups)) {
			return location;
		}

		double x = location.getX();
		double y = location.getY();

		double spacing = 5;

		for (JWindow popup : popups) {
			double popupHeight = popup.getContentPane().getHeight();

			if (getDirection(position) == SwingConstants.NORTH) {
				y -= popupHeight + spacing;
			}
			else {
				y += popupHeight + spacing;
			}
		}

		return new Point2D.Double(x, y);
	}

	private Point2D getInitialLocation(Rectangle contentBounds, Rectangle rootBounds, Position position) {
		double width = contentBounds.getWidth();
		double height = contentBounds.getHeight();
		double x = rootBounds.getMinX();
		double y = rootBounds.getMinY();
		double w = rootBounds.getWidth();
		double h = rootBounds.getHeight();

		double padX = 10;
		double padY = 10;

		switch (position) {
			case TOP_LEFT:
				return new Point2D.Double(x + padX, y + padY);
			case TOP_CENTER:
				return new Point2D.Double(x + (w - width) / 2, y + padY);
			case TOP_RIGHT:
				return new Point2D.Double(x + w - width - padX, y + padY);
			case CENTER_LEFT:
				return new Point2D.Double(x + padX, y + (h - height) / 2);
			case CENTER:
				return new Point2D.Double(x + (w - width) / 2, y + (h - height) / 2);
			case CENTER_RIGHT:
				return new Point2D.Double(x + w - width - padX, y + (h - height) / 2);
			case BOTTOM_LEFT:
				return new Point2D.Double(x + padX, y + h - height - padY);
			case BOTTOM_CENTER:
				return new Point2D.Double(x + (w - width) / 2, y + h - height - padY);
			case BOTTOM_RIGHT:
				return new Point2D.Double(x + w - width - padX, y + h - height - padY);
		}

		return new Point2D.Double(x, y);
	}

	private int getDirection(Position pos) {
		if (pos.toString().toLowerCase().startsWith("bottom")) {
			return SwingConstants.NORTH;
		}
		return SwingConstants.SOUTH;
	}

	private void createHideAnimation(final JWindow popup, final Duration delay, final Position position) {
		Timer timer = new Timer((int) delay.toMillis(), e -> {
			hide(popup, position);
		});
		timer.setRepeats(false);
		timer.setCoalesce(true);
		timer.start();
	}



	private static class BacklogEntry {

		Component root;

		Component component;

		Position position;


		BacklogEntry(Component root, Component component, Position position) {
			this.root = root;
			this.component = component;
			this.position = position;
		}
	}
}
