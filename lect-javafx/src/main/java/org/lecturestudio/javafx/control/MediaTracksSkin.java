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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.media.track.AudioTrack;
import org.lecturestudio.media.track.EventsTrack;
import org.lecturestudio.media.track.MediaTrack;

public class MediaTracksSkin extends SkinBase<MediaTracks> {

	private final MediaTracks mediaTracks;

	private Timeline timeline;

	private VBox trackContainer;
	private VBox trackInfoContainer;

	private Pane sliderPane;
	private Pane placeholder;
	private Pane scrollPlaceholder;
	private Pane rightPlaceholder;

	private Text sliderTime;

	private TimeSlider primarySlider;
	private SecondaryTimeSlider leftSlider;
	private SecondaryTimeSlider rightSlider;

	private Rectangle selectRect;

	private ScrollBar scrollBar;


	protected MediaTracksSkin(MediaTracks control) {
		super(control);

		mediaTracks = control;

		initLayout(control);
	}

	@Override
	public void dispose() {
		mediaTracks.setOnStickSliders(null);
		unregisterChangeListeners(mediaTracks.getTransform().mxxProperty());
		unregisterChangeListeners(mediaTracks.getTransform().txProperty());
		unregisterChangeListeners(mediaTracks.primarySelectionProperty());
		unregisterChangeListeners(mediaTracks.leftSelectionProperty());
		unregisterChangeListeners(mediaTracks.rightSelectionProperty());
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double sliderPaneHeight = sliderPane.prefHeight(-1);
		final double timelineHeight = timeline.prefHeight(-1);
		final double tracksHeight = trackContainer.prefHeight(-1);
		final double scrollBarHeight = scrollBar.prefHeight(-1);

		return topInset + bottomInset + sliderPaneHeight + timelineHeight + tracksHeight + scrollBarHeight;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected void layoutChildren(final double contentX, final double contentY,
								  final double contentWidth, final double contentHeight) {
		final double sliderPaneH = sliderPane.prefHeight(-1);
		final double infoW = trackInfoContainer.prefWidth(-1);
		final double infoSizeW = snapSizeX(infoW);
		final double rightPlaceW = rightPlaceholder.prefWidth(-1);
		final double rightPlaceX = snapPositionX(contentWidth - rightPlaceW);
		final double trackX = snapPositionX(contentX + infoW);
		final double trackW = snapSizeX(contentWidth - infoW - rightPlaceW);
		final double timelinePosY = snapPositionY(sliderPaneH);

		sliderPane.resizeRelocate(contentX, contentY, contentWidth, snapSizeY(sliderPaneH));

		layoutInArea(timeline, trackX, timelinePosY, trackW, contentHeight, -1,
					 HPos.LEFT, VPos.TOP);

		final double timelineH = timeline.getHeight();
		final double scrollBarH = scrollBar.prefHeight(-1);
		final double scrollBarSizeH = snapSizeY(scrollBarH);
		final double trackY = snapPositionY(sliderPaneH + timelineH);
		final double trackH = snapSizeY(contentHeight - sliderPaneH - timelineH - scrollBarH);
		final double scrollBarY = snapPositionY(contentHeight - scrollBarH);

		placeholder.resizeRelocate(contentX, timelinePosY, infoSizeW, snapSizeY(timelineH));
		scrollPlaceholder.resizeRelocate(contentX, scrollBarY, infoSizeW, scrollBarSizeH);
		trackInfoContainer.resizeRelocate(contentX, trackY, infoSizeW, trackH);
		trackContainer.resizeRelocate(trackX, trackY, trackW, trackH);
		rightPlaceholder.resizeRelocate(rightPlaceX, timelinePosY, snapSizeX(rightPlaceW), contentHeight);
		scrollBar.resizeRelocate(trackX, scrollBarY, trackW, scrollBarSizeH);
	}

	private void initLayout(MediaTracks control) {
		timeline = new Timeline();
		timeline.setMaxHeight(Control.USE_PREF_SIZE);

		scrollBar = new ScrollBar();
		scrollBar.setOrientation(Orientation.HORIZONTAL);
		scrollBar.setMin(0);
		scrollBar.setValue(0);
		scrollBar.maxProperty().bind(scrollBar.widthProperty()
				.multiply(control.getTransform().mxxProperty())
				.subtract(scrollBar.widthProperty()));
		scrollBar.visibleAmountProperty().bind(scrollBar.maxProperty()
				.multiply(scrollBar.widthProperty())
				.divide(scrollBar.widthProperty()
						.multiply(control.getTransform().mxxProperty())));
		scrollBar.disableProperty().bind(scrollBar.visibleAmountProperty().lessThan(1));
		scrollBar.valueProperty().addListener(o -> {
			control.getTransform().setTx(-scrollBar.getValue() / scrollBar.getWidth());
		});

		sliderPane = new Pane();
		sliderPane.getStyleClass().add("slider-pane");

		sliderTime = new Text();
		sliderTime.getStyleClass().add("slider-time");
		sliderTime.setManaged(false);
		sliderTime.setVisible(false);
		sliderTime.setMouseTransparent(true);

		primarySlider = new PrimaryTimeSlider();
		leftSlider = new SecondaryTimeSlider(HPos.LEFT);
		rightSlider = new SecondaryTimeSlider(HPos.RIGHT);

		primarySlider.setOnUpdateValue(this::updatePrimarySliderValue);
		leftSlider.setOnUpdateValue(this::updateLeftSliderValue);
		rightSlider.setOnUpdateValue(this::updateRightSliderValue);

		primarySlider.bindValueProperty(control.primarySelectionProperty());
		leftSlider.bindValueProperty(control.leftSelectionProperty());
		rightSlider.bindValueProperty(control.rightSelectionProperty());

		leftSlider.setStickToPrimary(true);
		rightSlider.setStickToPrimary(true);

		selectRect = new Rectangle();
		selectRect.getStyleClass().add("select-rect");
		selectRect.setManaged(false);
		selectRect.setMouseTransparent(true);
		selectRect.yProperty().bind(timeline.layoutYProperty()
				.add(timeline.heightProperty()));
		selectRect.heightProperty().bind(control.heightProperty()
				.subtract(selectRect.yProperty())
				.subtract(scrollBar.heightProperty()));

		trackContainer = new VBox();

		placeholder = new StackPane();
		placeholder.getStyleClass().addAll("placeholder", "top");

		scrollPlaceholder = new StackPane();
		scrollPlaceholder.getStyleClass().addAll("placeholder", "bottom");

		rightPlaceholder = new StackPane();
		rightPlaceholder.getStyleClass().addAll("placeholder", "right");

		trackInfoContainer = new VBox();
		trackInfoContainer.getStyleClass().add("track-info-pane");

		Line primaryLine = primarySlider.createLine(control);
		Line leftLine = leftSlider.createLine(control);
		Line rightLine = rightSlider.createLine(control);

		getChildren().addAll(sliderPane, timeline, placeholder,
				rightPlaceholder, trackInfoContainer, trackContainer,
				selectRect, leftLine, rightLine, primaryLine, leftSlider,
				rightSlider, primarySlider, sliderTime, scrollBar,
				scrollPlaceholder);

		timeline.durationProperty().bind(control.durationProperty());
		timeline.transformProperty().bind(control.transformProperty());
		timeline.setOnMousePressed(this::setPrimarySliderPos);
		timeline.layoutXProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				timeline.layoutXProperty().removeListener(this);

				setSliderPos(primarySlider, control.getPrimarySelection());
				setSliderPos(leftSlider, control.getLeftSelection());
				setSliderPos(rightSlider, control.getRightSelection());
			}
		});

		sliderPane.setOnMousePressed(this::setPrimarySliderPos);
		sliderPane.heightProperty().addListener(o -> {
			Bounds textBounds = sliderTime.getLayoutBounds();
			double ty = textBounds.getMaxY() - textBounds.getCenterY();

			sliderTime.setLayoutY((sliderPane.getHeight() + ty) * 0.5);
		});

		control.setOnStickSliders(event -> stickSliders(primarySlider));
		control.getTracks().forEach(this::addTrack);
		control.getTracks().addListener((InvalidationListener) observable -> {
			trackContainer.getChildren().clear();
			trackInfoContainer.getChildren().clear();

			control.getTracks().forEach(this::addTrack);
		});
		registerChangeListener(control.getTransform().mxxProperty(), o -> {
			updateSliderPos();
			centerViewPort(mediaTracks.getPrimarySelection(), true);
		});
		registerChangeListener(control.getTransform().txProperty(), o -> {
			updateSliderPos();
		});
		registerChangeListener(control.primarySelectionProperty(), o -> {
			Double value = (Double) o.getValue();

			setSliderPos(primarySlider, value);

			if (leftSlider.stickToPrimary()) {
				mediaTracks.setLeftSelection(value);
			}
			if (rightSlider.stickToPrimary()) {
				mediaTracks.setRightSelection(value);
			}
			if (!primarySlider.isValueChanging()) {
				centerViewPort(value, false);
			}
		});
		registerChangeListener(control.leftSelectionProperty(), o -> {
			changeSliderValue(leftSlider, (Double) o.getValue());
		});
		registerChangeListener(control.rightSelectionProperty(), o -> {
			changeSliderValue(rightSlider, (Double) o.getValue());
		});
	}

	private void addTrack(MediaTrack<?> track) {
		if (track instanceof AudioTrack) {
			Waveform waveform = new Waveform();
			waveform.setMediaTrack((AudioTrack) track);

			addMediaTrackControl(waveform, "audio-track-icon");
		}
		else if (track instanceof EventsTrack) {
			EventTimeline eventTimeline = new EventTimeline();
			eventTimeline.durationProperty().bind(mediaTracks.durationProperty());
			eventTimeline.setMediaTrack((EventsTrack) track);

			addMediaTrackControl(eventTimeline, "events-track-icon");
		}
	}

	private void addMediaTrackControl(MediaTrackControlBase<?> control, String iconName) {
		control.transformProperty().bind(mediaTracks.transformProperty());

		Pane infoPane = createTrackInfo(iconName);
		infoPane.prefHeightProperty().bind(control.heightProperty());

		trackContainer.getChildren().add(control);
		trackInfoContainer.getChildren().add(infoPane);
	}

	private Pane createTrackInfo(String iconName) {
		SvgIcon icon = new SvgIcon();
		icon.getStyleClass().add(iconName);

		StackPane infoPane = new StackPane(icon);
		infoPane.getStyleClass().add("track-info");

		return infoPane;
	}

	private void changeSliderValue(SecondaryTimeSlider slider, double value) {
		double primary = mediaTracks.getPrimarySelection();
		boolean stick = Math.abs(value - primary) < 0.0001D;

		if (stick) {
			slider.setStickToPrimary(true);
			setSliderPos(slider, primary);
		}
		else {
			setSliderPos(slider, value);
		}

		updateSelectRect();
	}

	private void setSliderPos(TimeSlider slider, double value) {
		Affine transform = mediaTracks.getTransform();
		double width = timeline.getWidth();
		double sx = transform.getMxx();
		double tx = transform.getTx() * width + timeline.getLayoutX();

		slider.setLayoutX(width * sx * value + tx - slider.getLineOffset());
	}

	private void setPrimarySliderPos(MouseEvent event) {
		Node source = (Node) event.getSource();

		double width = timeline.getWidth();
		double normPos = event.getX() / width;
		double offset = primarySlider.getLineOffset();
		double x = width * normPos - offset + source.getLayoutX();

		x = Math.max(x, timeline.getLayoutX() - offset);
		x = Math.min(x, timeline.getLayoutX() - offset + width);

		primarySlider.setLayoutX(x);

		triggerSeekEvent();
		updatePrimarySliderValue();
	}

	private void updateSliderPos() {
		setSliderPos(primarySlider, mediaTracks.getPrimarySelection());
		setSliderPos(leftSlider, mediaTracks.getLeftSelection());
		setSliderPos(rightSlider, mediaTracks.getRightSelection());
		updateSelectRect();
	}

	private void updatePrimarySliderValue() {
		double value = getSliderValue(primarySlider);

		mediaTracks.setPrimarySelection(value);
	}

	private void updateLeftSliderValue() {
		double value = getSliderValue(leftSlider);

		mediaTracks.setLeftSelection(value);
	}

	private void updateRightSliderValue() {
		double value = getSliderValue(rightSlider);

		mediaTracks.setRightSelection(value);
	}

	private void updateSelectRect() {
		Affine transform = mediaTracks.getTransform();
		double sx = transform.getMxx();
		double tx = transform.getTx() * timeline.getWidth() + timeline.getLayoutX();
		double width = timeline.getWidth() * sx;
		boolean stick = leftSlider.stickToPrimary() && rightSlider.stickToPrimary();

		if (!stick) {
			double x1 = width * mediaTracks.getLeftSelection() + tx;
			double x2 = width * mediaTracks.getRightSelection() + tx;
			double selectX1 = Math.min(x1, x2);
			double selectX2 = Math.max(x1, x2);

			if (nonNull(selectRect)) {
				selectRect.setX(selectX1);
				selectRect.setWidth(selectX2 - selectX1);
			}
		}

		selectRect.setVisible(!stick);
	}

	private void stickSliders(TimeSlider slider) {
		double value = getSliderValue(slider);

		leftSlider.setStickToPrimary(true);
		rightSlider.setStickToPrimary(true);

		mediaTracks.setLeftSelection(value);
		mediaTracks.setRightSelection(value);
		mediaTracks.setPrimarySelection(value);
	}

	private double getSliderValue(TimeSlider slider) {
		Affine transform = mediaTracks.getTransform();

		double width = timeline.getWidth();
		double sx = transform.getMxx();
		double tx = transform.getTx();
		double offset = slider.getLineOffset() - timeline.getLayoutX();

		return ((slider.getLayoutX() + offset) / width - tx) / sx;
	}

	private void centerViewPort(double value, boolean always) {
		Affine transform = mediaTracks.getTransform();
		double width = timeline.getWidth();
		double sx = transform.getMxx();
		double minX = -transform.getTx() / sx;
		double maxX = minX + 1 / sx;
		double scrollValue = width * sx * value - width / 2;
		double scrollMin = scrollBar.getMin();
		double scrollMax = scrollBar.getMax();

		if (!always) {
			if (value > minX && value < maxX) {
				scrollValue = scrollBar.getValue();
			}
		}

		scrollBar.setValue(Math.max(scrollMin, Math.min(scrollMax, scrollValue)));
	}

	private void triggerSeekEvent() {
		EventHandler<ActionEvent> handler = mediaTracks.onSeekActionProperty().get();

		if (nonNull(handler)) {
			handler.handle(new ActionEvent(mediaTracks, Event.NULL_SOURCE_TARGET));
		}
	}



	private class PrimaryTimeSlider extends TimeSlider {

		PrimaryTimeSlider() {
			super();

			SvgIcon thumbShadow = new SvgIcon();
			thumbShadow.getStyleClass().add("slider-thumb-shadow");
			thumbShadow.setMouseTransparent(true);

			getStyleClass().add("primary-slider");
			getChildren().add(1, thumbShadow);
		}

		@Override
		Line createLine(MediaTracks parent) {
			Line line = super.createLine(parent);
			line.getStyleClass().add("primary-line");
			line.startYProperty().unbind();
			line.startYProperty().bind(timeline.layoutYProperty());

			return line;
		}

		@Override
		void mousePressed() {
			triggerSeekEvent();

			super.mousePressed();
		}
	}



	private class SecondaryTimeSlider extends TimeSlider {

		private final PseudoClass LEFT_PSEUDO_CLASS = PseudoClass.getPseudoClass("left");
		private final PseudoClass RIGHT_PSEUDO_CLASS = PseudoClass.getPseudoClass("right");

		private final BooleanProperty left = new SimpleBooleanProperty();
		private final BooleanProperty right = new SimpleBooleanProperty();

		private final BooleanProperty stick = new SimpleBooleanProperty();


		SecondaryTimeSlider(HPos position) {
			super();

			getStyleClass().add("secondary-slider");

			registerChangeListener(left, o -> {
				pseudoClassStateChanged(LEFT_PSEUDO_CLASS, left.get());
			});
			registerChangeListener(right, o -> {
				pseudoClassStateChanged(RIGHT_PSEUDO_CLASS, right.get());
			});

			setPosition(position);
		}

		@Override
		void mouseDragged() {
			super.mouseDragged();

			setStickToPrimary(false);
		}

		void setStickToPrimary(boolean stick) {
			this.stick.set(stick);
		}

		boolean stickToPrimary() {
			return stick.get();
		}

		void setPosition(HPos position) {
			left.set(position == HPos.LEFT);
			right.set(position == HPos.RIGHT);

			setLineAnchor(position);
		}
	}



	private class TimeSlider extends Group {

		private final InvalidationListener layoutXListener;

		private final SvgIcon thumb;

		private final DoubleProperty lineX;

		private HPos lineAnchor;

		private boolean valueChanging;

		private Runnable updateValueCallback;

		private DoubleProperty valueProperty;


		TimeSlider() {
			getStyleClass().add("time-slider");
			setManaged(false);
			setLayoutX(0);
			setLayoutY(0);

			lineX = new SimpleDoubleProperty();

			thumb = new SvgIcon();
			thumb.getStyleClass().add("slider-thumb");
			thumb.widthProperty().addListener(o -> {
				updateLinePos();
			});
			thumb.setPickOnBounds(false);

			setLineAnchor(HPos.CENTER);

			getChildren().add(thumb);

			ThumbMouseHandler mouseHandler = new ThumbMouseHandler(this);

			Node thumbNode = getThumbNode();
			thumbNode.setOnMouseDragged(mouseHandler);
			thumbNode.setOnMousePressed(mouseHandler);
			thumbNode.setOnMouseReleased(mouseHandler);
			thumbNode.setOnMouseClicked(mouseHandler);

			layoutXListener = observable -> updateSliderTimePos();
		}

		void bindValueProperty(DoubleProperty property) {
			valueProperty = property;
		}

		Line createLine(MediaTracks parent) {
			Line line = new Line();
			line.getStyleClass().add("slider-line");
			line.setManaged(false);
			line.startYProperty().bind(timeline.layoutYProperty()
					.add(timeline.heightProperty()));
			line.endYProperty().bind(parent.heightProperty()
					.subtract(scrollBar.heightProperty()));
			line.startXProperty().bind(lineX);
			line.endXProperty().bind(lineX);
			line.layoutXProperty().bind(layoutXProperty());

			return line;
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

		void setSliderTimeVisible(boolean visible) {
			if (visible) {
				layoutXProperty().addListener(layoutXListener);
				updateSliderTimePos();
			}
			else {
				layoutXProperty().removeListener(layoutXListener);
			}

			sliderTime.setVisible(visible);
		}

		void updateSliderTime() {
			double selection = valueProperty.get();
			Time duration = mediaTracks.getDuration();
			Time current = new Time((long) (selection * duration.getMillis()), true);

			sliderTime.setText(current.toString());
		}

		void updateSliderTimePos() {
			int textPadding = 10;
			double width = thumb.getLayoutBounds().getWidth();
			double textWidth = sliderTime.getLayoutBounds().getWidth();
			double layoutX = getLayoutX();
			double x = layoutX + width + textWidth + textPadding;

			if (x > mediaTracks.getWidth()) {
				sliderTime.setLayoutX(snapPositionX(layoutX - textWidth - textPadding));
			}
			else {
				sliderTime.setLayoutX(snapPositionX(layoutX + width + textPadding));
			}
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
			Bounds bounds = timeline.getLayoutBounds();
			double offset = getLineOffset();
			double x = getLayoutX() + dx;
			double minX = bounds.getMinX() - offset + timeline.getLayoutX();
			double maxX = bounds.getMaxX() - offset + timeline.getLayoutX();
			double scrollValue = scrollBar.getValue() + dx;

			if (x < minX && scrollValue >= scrollBar.getMin() ||
				x > maxX && scrollValue <= scrollBar.getMax()) {
				scrollBar.setValue(scrollValue);
			}

			x = Math.min(Math.max(x, minX), maxX);

			setLayoutX(x);
		}

		void mousePressed() {
			updateSliderTime();
			setSliderTimeVisible(true);
			setValueChanging(true);
		}

		void mouseReleased() {
			setSliderTimeVisible(false);
			setValueChanging(false);
		}

		void mouseDragged() {
			updateSliderValue();
			updateSliderTime();
		}

		void mouseDoubleClicked() {
			stickSliders(this);
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

		protected final TimeSlider slider;

		private double lastX;


		ThumbMouseHandler(TimeSlider slider) {
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
