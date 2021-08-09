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

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.screencapture.RandomAccessScreenCaptureStream;
import org.lecturestudio.media.screencapture.ScreenCaptureData;
import org.lecturestudio.media.track.ScreenCaptureTrack;

import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ScreenCaptureTimelineSkin extends MediaTrackControlSkinBase {

    private final Consumer<RandomAccessScreenCaptureStream> trackListener = stream -> {
        updateControl();
    };

    private final ScreenCaptureTimeline screenCaptureTimeline;
    private Canvas canvas;

    protected ScreenCaptureTimelineSkin(ScreenCaptureTimeline control) {
        super(control);
        screenCaptureTimeline = control;
        initLayout(control);
    }

    @Override
    protected void updateControl() {
        final Affine transform = screenCaptureTimeline.getTransform();
        final ScreenCaptureData data = screenCaptureTimeline.getMediaTrack().getScreenCaptureData();

        if (isNull(data)) {
            return;
        }

        final double width = canvas.getWidth();
        final double height = canvas.getHeight();

        final Time duration = screenCaptureTimeline.getDuration();
        final GraphicsContext ctx = canvas.getGraphicsContext2D();

        ctx.setFill(screenCaptureTimeline.getBackgroundColor());
        ctx.fillRect(0, 0, width + 0.5, height + 0.5);

        double sx = transform.getMxx();
        double tx = transform.getTx() * width;
        final double pixelPerSecond = width * sx / (duration.getMillis() / 1000D);

        paintScreenCaptures(ctx, height, pixelPerSecond, tx);
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

    private void initLayout(ScreenCaptureTimeline control) {
        ScreenCaptureTrack track = control.getMediaTrack();

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

    private void paintScreenCaptures(GraphicsContext ctx, double height, double pixelPerSecond, double tx) {
        ctx.setStroke(screenCaptureTimeline.getSegmentMarkColor());
        ctx.setLineWidth(5);

        ScreenCaptureData data = screenCaptureTimeline.getMediaTrack().getScreenCaptureData();
        for (ScreenCaptureData.ScreenCaptureSegment segment : data.segments) {
            double startX = pixelPerSecond * segment.timestamp / 1000 + tx;
            double endX = pixelPerSecond * (segment.timestamp + segment.duration) / 1000 + tx;

            ctx.strokeLine(snap(startX), height / 2, snap(endX), height / 2);
        }
    }

    private double snap(double v) {
        return ((int) v) + .5;
    }
}
