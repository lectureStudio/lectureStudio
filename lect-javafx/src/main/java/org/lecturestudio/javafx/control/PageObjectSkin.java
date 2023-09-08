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

package org.lecturestudio.javafx.control;

import static java.util.Objects.nonNull;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Transform;
import javafx.scene.transform.TransformChangedEvent;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.util.FxUtils;

public abstract class PageObjectSkin<T extends PageObject<?>> extends SkinBase<T> {

	private final EventHandler<TransformChangedEvent> transformHandler = event -> onTransform();

	private HBox header;

	private Node content;


	protected abstract Node createContent();

	protected abstract void onParentChange(Node parent);

	protected abstract void onPostLayout();

	protected abstract void onRelocateShape(Point2D location);

	protected abstract void onTransform();


	PageObjectSkin(T control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double textW = snapSizeX(content.prefWidth(-1));
		final double headW = snapSizeX(header.prefWidth(-1));
		final double width = Math.max(textW, headW);

		return leftInset + width + rightInset;
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		final boolean headerVisible = header.isManaged() && header.isVisible();
		final double textH = content.prefHeight(-1);
		final double headH = headerVisible ? header.prefHeight(-1) : 0;

		return topInset + textH + headH + bottomInset;
	}

	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		final boolean headerVisible = header.isManaged() && header.isVisible();

		final double contentW = snapSizeX(content.prefWidth(-1));

		final double headW = headerVisible ? snapSizeX(header.prefWidth(-1)) : 0;
		final double headH = headerVisible ? snapSizeY(header.prefHeight(-1)) : 0;

		final double textY = headH + contentY;
		final double width = Math.max(contentW, headW);

		if (headerVisible) {
			header.resize(width, headH);
			header.setMaxHeight(headH);

			layoutInArea(header, contentX, contentY, contentWidth, contentHeight, -1, HPos.LEFT, VPos.TOP);
		}

		layoutInArea(content, contentX, textY, contentWidth, contentHeight - textY, -1, HPos.LEFT, VPos.TOP);
	}

	void resize() {
		PageObject pageObject = getSkinnable();
		pageObject.resize(pageObject.prefWidth(-1), pageObject.prefHeight(-1));
	}

	private void initLayout(T control) {
		MouseHandler rectMouseHandler = new MouseHandler();

		boolean focus = control.getFocus();

		Region closeIcon = new Region();
		closeIcon.getStyleClass().add("close-icon");

		Pane closeButton = new StackPane();
		closeButton.getStyleClass().add("close-button");
		closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			onClose();
		});
		closeButton.getChildren().add(closeIcon);

		header = new HBox();
		header.getStyleClass().add("header");
		header.setAlignment(Pos.TOP_RIGHT);
		header.getChildren().add(closeButton);
		// Add mouse event handlers in order to move the TextBox.
		header.setOnMousePressed(rectMouseHandler);
		header.setOnMouseDragged(rectMouseHandler);
		header.setVisible(focus);
		header.setManaged(focus);

		content = createContent();

		content.addEventFilter(KeyEvent.ANY, event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				control.setFocus(false);
				control.getParent().requestFocus();
				event.consume();
			}
		});

		content.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			content.requestFocus();
		});
		content.focusedProperty().addListener((observable, oldFocus, newFocus) -> {
			control.setFocus(newFocus);
		});

		control.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			content.requestFocus();
		});
		control.getPageTransform().addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, transformHandler);
		control.parentProperty().addListener((observable, oldParent, newParent) -> {
			onParentChange(newParent);
		});
		control.focusedProperty().addListener((observable, oldFocus, newFocus) -> {
			content.requestFocus();
		});
		control.focusProperty().addListener((observable, oldFocus, newFocus) -> {
			FxUtils.invoke(() -> {
				final double headerHeight = header.prefHeight(-1);

				header.setVisible(newFocus);
				header.setManaged(newFocus);

				// Control header visibility according to the focus.
				if (newFocus) {
					control.resize(control.getWidth(), control.getHeight() + headerHeight);
					control.relocate(control.getLayoutX(), control.getLayoutY() - headerHeight);
				}
				else {
					control.resize(control.getWidth(), control.getHeight() - headerHeight);
					control.relocate(control.getLayoutX(), control.getLayoutY() + headerHeight);
				}

				if (newFocus) {
					content.requestFocus();
				}
			});
		});

		getChildren().addAll(header, content);

		Platform.runLater(() -> {
			onPostLayout();

			if (focus) {
				content.requestFocus();
			}
		});
	}

	private void onClose() {
		PageObject pageObject = getSkinnable();
		pageObject.getPageTransform().removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, transformHandler);

		Action onCloseAction = pageObject.getOnClose();

		if (nonNull(onCloseAction)) {
			onCloseAction.execute();
		}
	}

	private void relocatePageObject(double dx, double dy) {
		PageObject pageObject = getSkinnable();
		pageObject.relocate(pageObject.getLayoutX() + dx, pageObject.getLayoutY() + dy);

		relocateShape(dx, dy);
	}

	private void relocateShape(double dx, double dy) {
		Transform transform = getSkinnable().getPageTransform();
		Shape shape = getSkinnable().getPageShape();

		Point2D location = shape.getBounds().getLocation();
		location.set(location.getX() + dx / transform.getMxx(), location.getY() + dy / transform.getMyy());

		onRelocateShape(location);
	}



	private class MouseHandler implements EventHandler<MouseEvent> {

		private double lastX = 0;
		private double lastY = 0;


		@Override
		public void handle(MouseEvent event) {
			EventType<? extends MouseEvent> type = event.getEventType();

			if (type == MouseEvent.MOUSE_PRESSED) {
				lastX = event.getX();
				lastY = event.getY();

				// Copy page object.
				if (event.isShortcutDown()) {
					Action onCopyAction = getSkinnable().getOnCopy();

					if (nonNull(onCopyAction)) {
						getSkinnable().copyingProperty().set(true);
						onCopyAction.execute();
						getSkinnable().copyingProperty().set(false);
					}
				}
			}
			else if (type == MouseEvent.MOUSE_DRAGGED) {
				double dx = event.getX() - lastX;
				double dy = event.getY() - lastY;

				relocatePageObject(dx, dy);
			}
		}

	}

}
