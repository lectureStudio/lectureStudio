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

import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.media.track.EventsTrack;

public class EventTimelineSkin extends MediaTrackControlSkinBase {

	private final Consumer<List<RecordedPage>> trackListener = recordedPages -> {
		updateControl();
	};

	private final EventTimeline eventTimeline;

	private Canvas canvas;


	protected EventTimelineSkin(EventTimeline control) {
		super(control);

		eventTimeline = control;

		initLayout(control);
	}

	@Override
	public void dispose() {
		super.dispose();

		//unregisterChangeListeners(eventTimeline.mediaTrackProperty());
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
		layoutInArea(canvas, contentX, contentY, contentWidth, contentHeight,
					 -1, HPos.LEFT, VPos.TOP);
	}

	private void initLayout(EventTimeline control) {
		EventsTrack track = control.getMediaTrack();

		canvas = new Canvas();
		canvas.widthProperty().bind(control.widthProperty());
		canvas.heightProperty().bind(control.heightProperty());
		canvas.widthProperty().addListener(o -> updateControl());
		canvas.heightProperty().addListener(o -> updateControl());

		getChildren().add(canvas);

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

		final double width = canvas.getWidth();
		final double height = canvas.getHeight();

		final Time duration = eventTimeline.getDuration();
		final GraphicsContext ctx = canvas.getGraphicsContext2D();

		ctx.setFill(eventTimeline.getBackgroundColor());
		ctx.fillRect(0, 0, width + 0.5, height + 0.5);

		double sx = transform.getMxx();
		double tx = transform.getTx() * width;
		final double pixelPerSecond = width * sx / (duration.getMillis() / 1000D);

		paintEvents(ctx, height, pixelPerSecond, tx);
		paintPageEvents(ctx, height, pixelPerSecond, tx);
	}

	private void paintPageEvents(GraphicsContext ctx, double height, double pixelPerSecond, double tx) {
		ctx.setStroke(eventTimeline.getPageMarkColor());
		ctx.setTextBaseline(VPos.CENTER);

		Text text = new Text();
		text.setFont(ctx.getFont());

		int boxPadding = 5;
		height--;

		List<RecordedPage> pages = eventTimeline.getMediaTrack().getData();
		for (RecordedPage recPage : pages) {
			int pageNumber = recPage.getNumber();
			if (pageNumber != 0) {
				text.setText("" + (pageNumber + 1));

				int timestamp = recPage.getTimestamp();

				double boxW = text.getLayoutBounds().getWidth() + boxPadding * 2;
				double boxH = Math.min(ctx.getFont().getSize() + boxPadding * 2, height);
				double boxX = pixelPerSecond * timestamp / 1000 + tx;
				double boxY = (height - boxH) / 2;

				ctx.setFill(eventTimeline.getPageMarkBackground());
				ctx.fillRoundRect(snap(boxX), snap(boxY), boxW, boxH, 5, 5);

				double textX = boxX + boxPadding;
				double textY = height / 2;

				ctx.setFill(eventTimeline.getPageMarkColor());
				ctx.fillText(text.getText(), snap(textX), textY);
			}
		}
	}

	private void paintEvents(GraphicsContext ctx, double height, double pixelPerSecond, double tx) {
		ctx.setStroke(eventTimeline.getEventMarkColor());

		List<RecordedPage> pages = eventTimeline.getMediaTrack().getData();
		for (RecordedPage page : pages) {
			List<PlaybackAction> actions = page.getPlaybackActions();
			for (PlaybackAction action : actions) {
				double x = pixelPerSecond * action.getTimestamp() / 1000 + tx;

				ctx.strokeLine(snap(x), 0, snap(x), height / 2);
			}
		}
	}

	private double snap(double v) {
		return ((int) v) + .5;
	}
}
