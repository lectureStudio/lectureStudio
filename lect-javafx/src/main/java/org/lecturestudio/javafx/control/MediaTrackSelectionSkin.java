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

import org.lecturestudio.core.model.Interval;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

public class MediaTrackSelectionSkin extends SkinBase<MediaTrackSelection<?>> {

	private final MediaTrackSelection<?> trackSelection;

	protected MediaTrackControlBase<?> parent;

	protected SelectionSlider leftSlider;
	protected SelectionSlider rightSlider;

	protected Rectangle selectRect;

	protected Button closeButton;


	protected MediaTrackSelectionSkin(MediaTrackSelection control) {
		super(control);

		this.trackSelection = control;

		initLayout();
	}

	@Override
	public void dispose() {
		super.dispose();

		unregisterChangeListeners(parent.getTransform().mxxProperty());
		unregisterChangeListeners(parent.getTransform().txProperty());
		unregisterChangeListeners(trackSelection.leftSelectionProperty());
		unregisterChangeListeners(trackSelection.rightSelectionProperty());
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return 0;
	}

	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return 1;
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return 0;
	}

	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return 1;
	}

	private void initLayout() {
		parent = (MediaTrackControlBase<?>) trackSelection.getParent();

		Interval<Double> interval = trackSelection.getTrackControl().getInterval();

		trackSelection.setManaged(false);
		trackSelection.setPickOnBounds(false);
		trackSelection.setLeftSelection(interval.getStart());
		trackSelection.setRightSelection(interval.getEnd());

		leftSlider = new SelectionSlider(parent, HPos.LEFT);
		rightSlider = new SelectionSlider(parent, HPos.RIGHT);

		leftSlider.setVisible(false);
		rightSlider.setVisible(false);

		leftSlider.setOnUpdateValue(this::updateLeftSliderValue);
		rightSlider.setOnUpdateValue(this::updateRightSliderValue);

		selectRect = new Rectangle();
		selectRect.getStyleClass().add("select-rect");
		selectRect.setManaged(false);
		selectRect.setMouseTransparent(true);
		selectRect.heightProperty().bind(parent.heightProperty());

		SvgIcon closeIcon = new SvgIcon();
		closeIcon.getStyleClass().add("close-icon");

		closeButton = new Button();
		closeButton.onActionProperty().bind(trackSelection.removeActionProperty());
		closeButton.getStyleClass().add("close-button");
		closeButton.setManaged(false);
		closeButton.prefHeightProperty().bind(leftSlider.thumb.widthProperty());
		closeButton.prefWidthProperty().bind(leftSlider.thumb.widthProperty());
		closeButton.layoutXProperty().bind(leftSlider.layoutXProperty()
				.subtract(closeButton.widthProperty()).add(leftSlider.lineX));
		closeButton.layoutYProperty().bind(parent.heightProperty()
				.subtract(closeButton.heightProperty()));

		getChildren().addAll(selectRect, closeButton, leftSlider, rightSlider);

		registerChangeListener(parent.getTransform().mxxProperty(), o -> {
			updateSliderPos();
		});
		registerChangeListener(parent.getTransform().txProperty(), o -> {
			updateSliderPos();
		});
		registerChangeListener(trackSelection.leftSelectionProperty(), o -> {
			setSliderPos(leftSlider, (Double) o.getValue());
			updateSelectRect();
		});
		registerChangeListener(trackSelection.rightSelectionProperty(), o -> {
			setSliderPos(rightSlider, (Double) o.getValue());
			updateSelectRect();
		});

		Platform.runLater(() -> {
			double buttonSize = leftSlider.thumb.getWidth();
			double sizeOffset = buttonSize - leftSlider.getLineOffset();
			buttonSize -= sizeOffset * 2;

			closeButton.resize(buttonSize, buttonSize);
			closeButton.setGraphic(closeIcon);

			closeIcon.applyCss();

			updateSliderPos();

			leftSlider.setVisible(true);
			rightSlider.setVisible(true);
		});
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
		setSliderPos(leftSlider, trackSelection.getLeftSelection());
		setSliderPos(rightSlider, trackSelection.getRightSelection());

		updateSelectRect();
	}

	private void updateSelectRect() {
		Affine transform = parent.getTransform();
		double sx = transform.getMxx();
		double tx = transform.getTx() * parent.getWidth() + parent.getLayoutX();
		double width = parent.getWidth() * sx;

		double x1 = width * trackSelection.getLeftSelection() + tx;
		double x2 = width * trackSelection.getRightSelection() + tx;
		double selectX1 = Math.min(x1, x2);
		double selectX2 = Math.max(x1, x2);

		selectRect.setLayoutX(selectX1);
		selectRect.setWidth(selectX2 - selectX1);
	}

	private void updateLeftSliderValue() {
		double value = getSliderValue(leftSlider);

		trackSelection.setLeftSelection(value);
		trackSelection.getTrackControl().setStartTime(value);
	}

	private void updateRightSliderValue() {
		double value = getSliderValue(rightSlider);

		trackSelection.setRightSelection(value);
		trackSelection.getTrackControl().setEndTime(value);
	}



	private class SelectionSlider extends Group implements Slider {

		private final PseudoClass LEFT_PSEUDO_CLASS = PseudoClass.getPseudoClass("left");
		private final PseudoClass RIGHT_PSEUDO_CLASS = PseudoClass.getPseudoClass("right");

		private final BooleanProperty left = new SimpleBooleanProperty();
		private final BooleanProperty right = new SimpleBooleanProperty();

		private final SvgIcon thumb;

		private final DoubleProperty lineX;

		private HPos lineAnchor;

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

			ThumbMouseHandler mouseHandler = new ThumbMouseHandler(this, Orientation.HORIZONTAL);

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

		@Override
		public void moveByDelta(double dx) {
			double x = getLayoutX() + dx;
			double offset = getLineOffset();

			// Left-right slider collision.
			if (lineAnchor == HPos.LEFT) {
				x = Math.min(x, rightSlider.getLayoutX() + rightSlider.getLineOffset() - offset);
			}
			else if (lineAnchor == HPos.RIGHT) {
				x = Math.max(x, leftSlider.getLayoutX() + leftSlider.getLineOffset() - offset);
			}

			// Slider parent collision.
			x = Math.max(x, parent.getLayoutX() - offset);
			x = Math.min(x, parent.getLayoutX() + parent.getWidth() - offset);

			setLayoutX(x);
		}

		@Override
		public void mouseDragged() {
			if (nonNull(updateValueCallback)) {
				updateValueCallback.run();
			}
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

		double getLineOffset() {
			return lineX.get();
		}

		void setOnUpdateValue(Runnable callback) {
			updateValueCallback = callback;
		}

		protected Node getThumbNode() {
			return thumb.getChildrenUnmodifiable().get(0);
		}

		private void updateLinePos() {
			Bounds thumbBounds = getThumbNode().getLayoutBounds();
			double width = thumb.getWidth();
			double offset = (width + 2 - thumbBounds.getWidth()) * 0.5;

			switch (lineAnchor) {
				case CENTER -> lineX.set(snapPositionX(width * 0.5));
				case LEFT -> lineX.set(width - offset);
				case RIGHT -> lineX.set(offset);
			}
		}
	}



	protected static class ThumbMouseHandler implements EventHandler<MouseEvent> {

		protected final Slider slider;

		private final Orientation orientation;

		private double lastX;
		private double lastY;


		ThumbMouseHandler(Slider slider, Orientation orientation) {
			this.slider = slider;
			this.orientation = orientation;
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
				lastY = event.getY();

				slider.mousePressed();
			}
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				event.consume();

				slider.mouseReleased();
			}
			else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				event.consume();

				if (orientation == Orientation.HORIZONTAL) {
					slider.moveByDelta(event.getX() - lastX);
				}
				else if (orientation == Orientation.VERTICAL) {
					slider.moveByDelta(event.getY() - lastY);
				}

				slider.mouseDragged();
			}
		}
	}



	protected interface Slider {

		void moveByDelta(double delta);

		default void mousePressed() {}

		default void mouseReleased() {}

		default void mouseDragged() {}

		default void mouseDoubleClicked() {}
	}
}
