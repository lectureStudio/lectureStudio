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

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.model.Time;

public class TimelineSkin extends MediaTrackControlSkinBase {

	private final Timeline timeline;

	private Canvas canvas;


	protected TimelineSkin(Timeline control) {
		super(control);

		timeline = control;

		initLayout(control);
	}

	@Override
	public void dispose() {
		super.dispose();

		unregisterChangeListeners(timeline.durationProperty());
	}

	@Override
	protected void updateControl() {
		final Affine transform = timeline.getTransform();
		final Time duration = timeline.getDuration();

		if (isNull(duration) || isNull(transform)) {
			return;
		}

		final double width = canvas.getWidth();
		final double height = canvas.getHeight();

		final Time time = new Time(0);
		final GraphicsContext ctx = canvas.getGraphicsContext2D();

		final Text text = new Text(new Time(0, true).toString());
		text.setFont(ctx.getFont());

		double sx = transform.getMxx();
		double tx = transform.getTx() * width;
		double seconds = duration.getMillis() / 1000D;
		double pixelPerSecond = width * sx / seconds;
		double textWidth = text.getLayoutBounds().getWidth() + 20;
		int secondStep = (int) Math.ceil(seconds / (width * sx / textWidth));

		ctx.setFill(timeline.getBackgroundColor());
		ctx.fillRect(0, 0, width + 0.5, height + 0.5);
		ctx.strokeLine(0, 0, width, 0);

		for (int s = 0; s < seconds; s++) {
			double x = s * pixelPerSecond + tx;

			ctx.setStroke(timeline.getTickColor());

			if (s % secondStep == 0) {
				time.setMillis(s * 1000);

				ctx.strokeLine(x, 0, x, 10);

				ctx.setFill(timeline.getTextColor());
				ctx.fillText(time.toString(), x, 23);
			}
			else {
				ctx.strokeLine(x, 0, x, 6);
			}
		}
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + bottomInset + 30;
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + rightInset + 30;
	}

	@Override
	protected void layoutChildren(final double contentX, final double contentY,
								  final double contentWidth, final double contentHeight) {
		layoutInArea(canvas, contentX, contentY, contentWidth, contentHeight,
					 -1, HPos.LEFT, VPos.TOP);
	}

	private void initLayout(Timeline control) {
		canvas = new Canvas();
		canvas.widthProperty().bind(control.widthProperty());
		canvas.heightProperty().bind(control.heightProperty());
		canvas.widthProperty().addListener(o -> updateControl());
		canvas.heightProperty().addListener(o -> updateControl());

		getChildren().add(canvas);

		registerChangeListener(control.durationProperty(), value -> updateControl());

		updateControl();
	}
}
