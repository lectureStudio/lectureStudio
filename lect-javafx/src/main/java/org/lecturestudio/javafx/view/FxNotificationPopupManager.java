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

package org.lecturestudio.javafx.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.stage.Popup;
import javafx.util.Duration;

import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.javafx.util.FxUtils;

public class FxNotificationPopupManager implements NotificationPopupManager {

	private final Map<Pos, List<Popup>> popupMap = new HashMap<>();

	private final Map<Pos, List<BacklogEntry>> backlog = new HashMap<>();


	@Override
	public void show(View rootView, NotificationPopupView view) {
		if (!Node.class.isAssignableFrom(rootView.getClass())) {
			throw new RuntimeException("Root view has to be an instance of " + Node.class);
		}
		if (!Node.class.isAssignableFrom(view.getClass())) {
			throw new RuntimeException("View has to be an instance of " + Node.class);
		}

		final Node rootNode = (Node) rootView;
		final Node viewNode = (Node) view;
		final Pos pos = Pos.valueOf(view.getPosition().toString());

		FxUtils.invoke(() -> {
			show(rootNode, viewNode, pos, Duration.ZERO);
		});
	}

	private void show(Node rootNode, Node node, Pos position, Duration delay) {
		Popup popup = new Popup();
		popup.setAutoFix(false);
		popup.setOpacity(0);
		popup.getContent().add(node);
		popup.show(rootNode, 0, 0);

		Bounds contentBounds = node.getBoundsInLocal();
		Bounds rootBounds = rootNode.localToScreen(rootNode.getLayoutBounds());

		Point2D location = getLocation(contentBounds, rootBounds, position);

		if (location.getY() + contentBounds.getHeight() > rootBounds.getMinY() + rootBounds.getHeight() ||
			location.getY() < rootBounds.getMinY()) {
			popup.hide();
			addBacklog(new BacklogEntry(rootNode, node, position));
			return;
		}

		popup.setAnchorX(location.getX());
		popup.setAnchorY(location.getY());

		addPopup(popup, position);

		createShowAnimation(popup, delay).play();
		createHideAnimation(popup, delay, position).play();
	}

	private void showBacklog(Pos position) {
		List<BacklogEntry> entries = backlog.get(position);

		if (nonNull(entries) && !entries.isEmpty()) {
			ListIterator<BacklogEntry> iter = entries.listIterator();
			int i = 0;

			while (iter.hasNext()){
				BacklogEntry entry = iter.next();
				iter.remove();

				show(entry.rootNode, entry.node, entry.position, Duration.millis(i++ * 500));
			}
		}
	}

	private void hide(Popup popup, Pos position) {
		popup.hide();

		removePopup(popup, position);

		List<Popup> popups = popupMap.get(position);

		if (nonNull(popups) && popups.isEmpty()) {
			showBacklog(position);
		}
	}

	private void addPopup(Popup popup, Pos position) {
		List<Popup> popups = popupMap.get(position);

		if (isNull(popups)) {
			popups = new ArrayList<>();
			popupMap.put(position, popups);
		}

		popups.add(popup);
	}

	private void removePopup(Popup popup, Pos position) {
		List<Popup> popups = popupMap.get(position);

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

	private Point2D getLocation(Bounds contentBounds, Bounds rootBounds, Pos position) {
		Point2D location = getInitialLocation(contentBounds, rootBounds, position);
		List<Popup> popups = popupMap.get(position);

		if (isNull(popups)) {
			return location;
		}

		double x = location.getX();
		double y = location.getY();

		double spacing = 5;

		for (Popup popup : popups) {
			double popupHeight = popup.getContent().get(0).getBoundsInParent().getHeight();

			if (getDirection(position) == VerticalDirection.UP) {
				y -= popupHeight + spacing;
			}
			else {
				y += popupHeight + spacing;
			}
		}

		return new Point2D(x, y);
	}

	private Point2D getInitialLocation(Bounds contentBounds, Bounds rootBounds, Pos position) {
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
				return new Point2D(x + padX, y + padY);
			case TOP_CENTER:
				return new Point2D(x + (w - width) / 2, y + padY);
			case TOP_RIGHT:
				return new Point2D(x + w - width - padX, y + padY);
			case CENTER_LEFT:
				return new Point2D(x + padX, y + (h - height) / 2);
			case CENTER:
				return new Point2D(x + (w - width) / 2, y + (h - height) / 2);
			case CENTER_RIGHT:
				return new Point2D(x + w - width - padX, y + (h - height) / 2);
			case BOTTOM_LEFT:
				return new Point2D(x + padX, y + h - height - padY);
			case BOTTOM_CENTER:
				return new Point2D(x + (w - width) / 2, y + h - height - padY);
			case BOTTOM_RIGHT:
				return new Point2D(x + w - width - padX, y + h - height - padY);
		}

		return new Point2D(x, y);
	}

	private VerticalDirection getDirection(Pos pos) {
		if (pos.toString().toLowerCase().startsWith("bottom")) {
			return VerticalDirection.UP;
		}
		return VerticalDirection.DOWN;
	}

	private Animation createShowAnimation(final Popup popup, final Duration delay) {
		KeyValue fadeInBegin = new KeyValue(popup.opacityProperty(), 0.0);
		KeyValue fadeInEnd = new KeyValue(popup.opacityProperty(), 1.0);

		KeyFrame begin = new KeyFrame(Duration.ZERO, fadeInBegin);
		KeyFrame end = new KeyFrame(Duration.millis(100), fadeInEnd);

		Timeline timeline = new Timeline(begin, end);
		timeline.setDelay(delay);

		return timeline;
	}

	private Animation createHideAnimation(final Popup popup, final Duration delay, final Pos position) {
		KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1.0);
		KeyValue fadeOutEnd = new KeyValue(popup.opacityProperty(), 0.0);

		KeyFrame begin = new KeyFrame(Duration.ZERO, fadeOutBegin);
		KeyFrame end = new KeyFrame(Duration.millis(500), fadeOutEnd);

		Timeline timeline = new Timeline(begin, end);
		timeline.setOnFinished(actionEvent -> {
			hide(popup, position);
		});

		final double oldAnchorY = popup.getAnchorY();
		final double mult = getDirection(position) == VerticalDirection.UP ? 10 : -10;

		Transition translate = new Transition() {

			{
				setCycleDuration(Duration.millis(500));
			}

			@Override
			public void interpolate(double frac) {
				double newAnchorY = oldAnchorY + mult * frac;
				popup.setAnchorY(newAnchorY);
			}

		};

		return new SequentialTransition(
				new PauseTransition(Duration.seconds(2).add(delay)),
				new ParallelTransition(timeline, translate));
	}



	private static class BacklogEntry {

		Node rootNode;
		Node node;
		Pos position;


		BacklogEntry(Node rootNode, Node node, Pos position) {
			this.rootNode = rootNode;
			this.node = node;
			this.position = position;
		}

	}
}