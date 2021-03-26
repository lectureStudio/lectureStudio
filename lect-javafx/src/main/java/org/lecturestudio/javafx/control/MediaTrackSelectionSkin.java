/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

public class MediaTrackSelectionSkin extends SkinBase<MediaTrackSelection> {

	private MediaTrackControlBase<?> parent;

	private SelectionSlider leftSlider;
	private SelectionSlider rightSlider;

	private Rectangle selectRect;


	protected MediaTrackSelectionSkin(MediaTrackSelection control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + bottomInset;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + rightInset;
	}

	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + rightInset;
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
	}

	private void initLayout(MediaTrackSelection control) {
		parent = (MediaTrackControlBase<?>) control.getParent();

		control.setManaged(false);

		leftSlider = new SelectionSlider(parent, HPos.LEFT);
		rightSlider = new SelectionSlider(parent, HPos.RIGHT);

		leftSlider.setOnUpdateValue(this::updateLeftSliderValue);
		rightSlider.setOnUpdateValue(this::updateRightSliderValue);

		selectRect = new Rectangle();
		selectRect.getStyleClass().add("select-rect");
		selectRect.setManaged(false);
		selectRect.heightProperty().bind(parent.heightProperty());

		getChildren().addAll(selectRect, leftSlider, rightSlider);

		registerChangeListener(parent.getTransform().mxxProperty(), o -> {
			updateSliderPos();
		});
		registerChangeListener(parent.getTransform().txProperty(), o -> {
			updateSliderPos();
		});
		registerChangeListener(control.leftSelectionProperty(), o -> {
			setSliderPos(leftSlider, (Double) o.getValue());
			updateSelectRect();
		});
		registerChangeListener(control.rightSelectionProperty(), o -> {
			setSliderPos(rightSlider, (Double) o.getValue());
			updateSelectRect();
		});

		control.toFront();
	}

	private double getSliderValue(SelectionSlider slider) {
		Affine transform = parent.getTransform();

		double width = parent.getWidth();
		double sx = transform.getMxx();
		double tx = transform.getTx();
		double offset = slider.getLineOffset() - parent.getLayoutX();

		return ((slider.getLayoutX() + offset) / width - tx) / sx;
	}

	private void setSliderPos(SelectionSlider slider, double value) {
		Affine transform = parent.getTransform();
		double width = parent.getWidth();
		double sx = transform.getMxx();
		double tx = transform.getTx() * width + parent.getLayoutX();

		slider.setLayoutX(width * sx * value + tx - slider.getLineOffset());
	}

	private void updateSliderPos() {
		setSliderPos(leftSlider, getSkinnable().getLeftSelection());
		setSliderPos(rightSlider, getSkinnable().getRightSelection());
		updateSelectRect();
	}

	private void updateSelectRect() {
		Affine transform = parent.getTransform();
		double sx = transform.getMxx();
		double tx = transform.getTx() * parent.getWidth() + parent.getLayoutX();
		double width = parent.getWidth() * sx;

		double x1 = width * getSkinnable().getLeftSelection() + tx;
		double x2 = width * getSkinnable().getRightSelection() + tx;
		double selectX1 = Math.min(x1, x2);
		double selectX2 = Math.max(x1, x2);

		if (nonNull(selectRect)) {
			selectRect.setX(selectX1);
			selectRect.setWidth(selectX2 - selectX1);
		}
	}

	private void updateLeftSliderValue() {
		double value = getSliderValue(leftSlider);

		getSkinnable().setLeftSelection(value);
	}

	private void updateRightSliderValue() {
		double value = getSliderValue(rightSlider);

		getSkinnable().setRightSelection(value);
	}



	private class SelectionSlider extends Group {

		private final PseudoClass LEFT_PSEUDO_CLASS = PseudoClass.getPseudoClass("left");
		private final PseudoClass RIGHT_PSEUDO_CLASS = PseudoClass.getPseudoClass("right");

		private final BooleanProperty left = new SimpleBooleanProperty();
		private final BooleanProperty right = new SimpleBooleanProperty();

		private final SvgIcon thumb;

		private final DoubleProperty lineX;

		private HPos lineAnchor;

		private boolean valueChanging;

		private Runnable updateValueCallback;


		SelectionSlider(Control parent, HPos position) {
			getStyleClass().add("selection-slider");
			setManaged(false);
			setLayoutX(0);
			setLayoutY(0);

			lineX = new SimpleDoubleProperty();

			Line line = new Line();
			line.getStyleClass().add("slider-line");
			line.setManaged(false);
			line.startXProperty().bind(lineX);
			line.endXProperty().bind(lineX);
			line.startYProperty().bind(parent.layoutYProperty());
			line.endYProperty().bind(parent.heightProperty());

			thumb = new SvgIcon();
			thumb.getStyleClass().add("slider-thumb");
			thumb.setLayoutY(-2);
			thumb.widthProperty().addListener(o -> {
				updateLinePos();
			});
			thumb.setPickOnBounds(false);

			getChildren().addAll(line, thumb);

			ThumbMouseHandler mouseHandler = new ThumbMouseHandler(this);

			Node thumbNode = getThumbNode();
			thumbNode.setOnMouseDragged(mouseHandler);
			thumbNode.setOnMousePressed(mouseHandler);
			thumbNode.setOnMouseReleased(mouseHandler);
			thumbNode.setOnMouseClicked(mouseHandler);

			registerChangeListener(left, o -> {
				pseudoClassStateChanged(LEFT_PSEUDO_CLASS, left.get());
			});
			registerChangeListener(right, o -> {
				pseudoClassStateChanged(RIGHT_PSEUDO_CLASS, right.get());
			});

			setPosition(position);
		}

		void setPosition(HPos position) {
			left.set(position == HPos.LEFT);
			right.set(position == HPos.RIGHT);

			setLineAnchor(position);
		}

		void setLineAnchor(HPos anchor) {
			lineAnchor = anchor;

			updateLinePos();
		}

		void setValueChanging(boolean changing) {
			valueChanging = changing;
		}

		boolean isValueChanging() {
			return valueChanging;
		}

		double getLineOffset() {
			return lineX.get();
		}

		void setOnUpdateValue(Runnable callback) {
			updateValueCallback = callback;
		}

		void updateSliderValue() {
			if (nonNull(updateValueCallback)) {
				updateValueCallback.run();
			}
		}

		void moveByDeltaX(double dx) {
			setLayoutX(getLayoutX() + dx);
		}

		void mousePressed() {
			setValueChanging(true);
		}

		void mouseReleased() {
			setValueChanging(false);
		}

		void mouseDragged() {
			updateSliderValue();
		}

		void mouseDoubleClicked() {

		}

		protected Node getThumbNode() {
			return thumb.getChildrenUnmodifiable().get(0);
		}

		private void updateLinePos() {
			Bounds thumbBounds = getThumbNode().getLayoutBounds();
			double width = thumb.getWidth();
			double offset = (width + 2 - thumbBounds.getWidth()) * 0.5;

			switch (lineAnchor) {
				case CENTER:
					lineX.set(snapPositionX(width * 0.5));
					break;
				case LEFT:
					lineX.set(width - offset);
					break;
				case RIGHT:
					lineX.set(offset);
					break;
			}
		}
	}



	private static class ThumbMouseHandler implements EventHandler<MouseEvent> {

		protected final SelectionSlider slider;

		private double lastX;


		ThumbMouseHandler(SelectionSlider slider) {
			this.slider = slider;
		}

		@Override
		public void handle(MouseEvent event) {
			if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
				event.consume();

				if (event.getClickCount() == 2) {
					slider.mouseDoubleClicked();
				}
			}
			else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				event.consume();

				lastX = event.getX();

				slider.mousePressed();
			}
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				event.consume();

				slider.mouseReleased();
			}
			else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				event.consume();

				slider.moveByDeltaX(event.getX() - lastX);
				slider.mouseDragged();
			}
		}
	}
}
