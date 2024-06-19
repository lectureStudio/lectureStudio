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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntToDoubleFunction;

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.util.Slider;
import org.lecturestudio.javafx.util.ThumbMouseHandler;
import org.lecturestudio.media.track.EventsTrack;

public class EventTimelineSkin extends MediaTrackControlSkinBase {

	private final Consumer<List<RecordedPage>> trackListener = recordedPages -> updateControl();

	private final EventTimeline eventTimeline;
	private Pane pane;

	private final List<PageSlider> pageSliders = new ArrayList<>();
	private final ArrayList<Rectangle> pageEventList = new ArrayList<>();
	private IntToDoubleFunction timeToXPositionFunction;


	protected EventTimelineSkin(EventTimeline control) {
		super(control);

		eventTimeline = control;

		initLayout(control);
	}

	@Override
	protected void updateControl() {
		FxUtils.invoke(this::render);
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + bottomInset + 18;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + rightInset + 10;
	}

	@Override
	protected void layoutChildren(final double contentX, final double contentY,
								  final double contentWidth, final double contentHeight) {
		layoutInArea(pane, contentX, contentY, contentWidth, contentHeight, -1, HPos.LEFT, VPos.CENTER);
	}

	private void initLayout(EventTimeline control) {
		EventsTrack track = control.getMediaTrack();

		pane = new Pane();

		pane.setPrefHeight(Region.USE_PREF_SIZE);
		pane.setPrefWidth(Region.USE_PREF_SIZE);
		pane.widthProperty().addListener(o -> updateControl());
		pane.heightProperty().addListener(o -> updateControl());
		pane.getStyleClass().add("event-timeline-pane");

		getChildren().add(pane);

		if (nonNull(track)) {
			track.addChangeListener(trackListener);
			updateControl();
		}

		control.mediaTrackProperty().addListener((o, oldValue, newValue) -> {
			if (nonNull(oldValue)) {
				track.removeChangeListener(trackListener);
			}
			if (nonNull(newValue)) {
				track.addChangeListener(trackListener);
				updateControl();
			}
		});
	}

	private void render() {
		final Affine transform = eventTimeline.getTransform();
		final List<RecordedPage> events = eventTimeline.getMediaTrack().getData();

		if (isNull(events)) {
			return;
		}

		final double width = pane.getWidth();
		final double height = pane.getHeight();

		final Time duration = eventTimeline.getDuration();

		double sx = transform.getMxx();
		double tx = transform.getTx() * width;
		final double pixelPerSecond = width * sx / (duration.getMillis() / 1000D);

		timeToXPositionFunction = (timestamp) -> pixelPerSecond * timestamp / 1000 + tx;

		paintEvents(height);
		paintPageEvents(height);
	}

	private void paintPageEvents(double height) {
		List<RecordedPage> pages = eventTimeline.getMediaTrack().getData();

		pageSliders.forEach(pageSlider -> pane.getChildren().remove(pageSlider));
		pageSliders.clear();

		for (RecordedPage recPage : pages) {
			int pageNumber = recPage.getNumber();

			if (pageNumber != 0) {
				PageSlider pageSlider = new PageSlider(pages, pageNumber, height);
				ThumbMouseHandler mouseHandler = new ThumbMouseHandler(pageSlider, Orientation.HORIZONTAL);

				pageSlider.setOnMouseDragged(mouseHandler);
				pageSlider.setOnMousePressed(mouseHandler);
				pageSlider.setOnMouseReleased(mouseHandler);
				pageSlider.setOnMouseClicked(mouseHandler);

				int timestamp = recPage.getTimestamp();

				double boxX = timeToXPositionFunction.applyAsDouble(timestamp);

				pageSlider.setLayoutX(snapPositionX(boxX));
				pageSlider.setLayoutY(snapPositionY(0));

				pageSliders.add(pageSlider);
			}
		}
		pane.getChildren().addAll(pageSliders);
	}

	private void paintEvents(double height) {
		pageEventList.forEach(pageSlider -> pane.getChildren().remove(pageSlider));
		pageEventList.clear();

		List<RecordedPage> pages = eventTimeline.getMediaTrack().getData();
		for (RecordedPage page : pages) {
			List<PlaybackAction> actions = page.getPlaybackActions();
			Integer actionStartTime = null;

			for (PlaybackAction action : actions) {
				ActionType actionType = action.getType();

				if (actionType == ActionType.TOOL_BEGIN && isNull(actionStartTime)) {
					actionStartTime = action.getTimestamp();
				}
				else if (actionType == ActionType.TOOL_END && nonNull(actionStartTime)) {
					double beginningTime = timeToXPositionFunction.applyAsDouble(actionStartTime);
					double endTime = timeToXPositionFunction.applyAsDouble(action.getTimestamp());
					String styleClass = getMarkerStyleClass(actionType);

					addMarker(beginningTime, endTime - beginningTime, height, styleClass);

					actionStartTime = null;
				}
				else {
					switch (actionType) {
						case TEXT_SELECTION_EXT, RUBBER_EXT, DELETE_ALL, ZOOM_OUT -> {
							double x = timeToXPositionFunction.applyAsDouble(action.getTimestamp());

							addMarker(x, 1, height, getMarkerStyleClass(actionType));
						}
					}
				}
			}
		}
		pane.getChildren().addAll(pageEventList);
	}

	private void addMarker(double x, double width, double height,
			String styleClass) {
		Rectangle rectangle = new Rectangle(width + 0.5, height / 1.5);
		rectangle.getStyleClass().add(styleClass);
		rectangle.setX(snapPositionX(x));
		rectangle.setY(snapPositionY(height / 6));

		pageEventList.add(rectangle);
	}

	private String getMarkerStyleClass(ActionType actionType) {
		switch (actionType) {
			case RUBBER_EXT, DELETE_ALL -> {
				return "page-event-delete-marker";
			}
			default -> {
				return "page-event-marker";
			}
		}
	}



	private class PageSlider extends Group implements Slider {

		private final List<RecordedPage> pages;
		private final Label label;
		private final int pageNumber;
		private double minX;
		private double maxX;

		PageSlider(List<RecordedPage> pages, int pageNumber, double maxHeight) {
			super();

			this.pages = pages;
			this.pageNumber = pageNumber;

			label = new Label(String.valueOf(pageNumber + 1));

			label.getStyleClass().add("page-slider");

			this.getChildren().addAll(label);

			this.setManaged(false);
			this.setLayoutX(snapPositionX(0));
			this.setLayoutY(snapPositionY(0));

			label.setAlignment(Pos.CENTER);
			label.setPrefWidth(label.getWidth() + 18);
			label.setPrefHeight(Math.min(label.getHeight() + 15, maxHeight));
		}

		@Override
		public void mousePressed() {
			minX = Double.MIN_VALUE;
			maxX = Double.MAX_VALUE;

			RecordedPage minPage = pages.get(pageNumber - 1);
			RecordedPage maxPage = pages.get(pageNumber);

			minPage.getPlaybackActions().forEach(action -> minX = Math.max(minX, action.getTimestamp()));
			maxPage.getPlaybackActions().forEach(action -> maxX = Math.min(maxX, action.getTimestamp()));

			minX = Math.max(minX, minPage.getTimestamp());
			if (pages.size() > pageNumber + 1) {
				maxX = Math.min(maxX, pages.get(pageNumber + 1).getTimestamp());
			}

			minX = timeToXPositionFunction.applyAsDouble((int) minX);
			maxX = timeToXPositionFunction.applyAsDouble((int) maxX);

			minX = Math.max(minX, eventTimeline.getLayoutBounds().getMinX() + eventTimeline.getLayoutX());
			maxX = Math.min(maxX, eventTimeline.getLayoutBounds().getMaxX() + eventTimeline.getLayoutX());

			Time duration = eventTimeline.getDuration();
			Time current = new Time((long) (getSliderValue() * duration.getMillis()), true);

			eventTimeline.getShowTimeCallback().accept(current, this.getLayoutX() + this.getLayoutBounds().getCenterX());
		}

		@Override
		public void mouseReleased() {
			RecordedPage page = pages.get(pageNumber).clone();

			Time duration = eventTimeline.getDuration();
			int time = (int) (getSliderValue() * duration.getMillis());

			page.setTimestamp(time);

			eventTimeline.getShowTimeCallback().accept(null, null);

			moveOrHidePage(page);
		}

		@Override
		public void mouseDragged() {
			Time duration = eventTimeline.getDuration();
			Time current = new Time((long) (getSliderValue() * duration.getMillis()), true);

			eventTimeline.getShowTimeCallback().accept(current, this.getLayoutX() + this.getLayoutBounds().getCenterX());
		}

		@Override
		public void moveByDelta(double deltaX) {
			double x = getLayoutX() + deltaX;

			x = Math.min(Math.max(x, minX), maxX);

			this.setLayoutX(snapPositionX(x));
		}


		private double getSliderValue() {
			Affine transform = eventTimeline.getTransform();

			double width = eventTimeline.getWidth();
			double sx = transform.getMxx();
			double tx = transform.getTx();

			return ((getLayoutX()) / width - tx) / sx;
		}

		private void moveOrHidePage(RecordedPage page) {
			RecordedPage lowerPageBound;
			RecordedPage higherPageBound;

			lowerPageBound = pages.get(page.getNumber() - 1);

			if (page.getNumber() == pages.size() - 1) {
				higherPageBound = new RecordedPage();
				higherPageBound.setTimestamp((int) eventTimeline.getDuration().getMillis());
				higherPageBound.setNumber(page.getNumber() + 1);
			}
			else {
				higherPageBound = pages.get(page.getNumber() + 1);
			}

			if (page.getTimestamp() - lowerPageBound.getTimestamp() < 150) { // Allow a margin error of 150ms.
				eventTimeline.getOnHideAndMoveNextPage().execute(lowerPageBound);
			}
			else if (higherPageBound.getTimestamp() - page.getTimestamp() < 150) {
				eventTimeline.getOnHidePage().execute(page);
			}
			else {
				eventTimeline.getOnMovePage().execute(page);
			}
		}
	}
}
