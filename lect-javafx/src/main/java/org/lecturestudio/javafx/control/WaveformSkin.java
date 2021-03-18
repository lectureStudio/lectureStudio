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

import java.util.function.Consumer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.media.audio.WaveformData;
import org.lecturestudio.media.track.AudioTrack;

public class WaveformSkin extends MediaTrackControlSkin {

	private final Consumer<RandomAccessAudioStream> trackListener = recordedPages -> {
		updateControl();
	};

	private final Waveform waveform;

	private Canvas canvas;


	protected WaveformSkin(Waveform control) {
		super(control);

		waveform = control;

		initLayout(control);
	}

	@Override
	public void dispose() {
		super.dispose();

//		unregisterChangeListeners(waveform.waveformDataProperty());
	}

	@Override
	protected void updateControl() {
		final WaveformData data = waveform.getMediaTrack().getWaveformData();
		final Affine transform = waveform.getTransform();
		final double width = canvas.getWidth();
		final double height = canvas.getHeight();

		if (isNull(data) || isNull(transform) || width == 0 || height == 0) {
			return;
		}

		double sx = transform.getMxx();
		double tx = transform.getTx() * width;

		// Sample blocks per pixel.
		int blocks = data.negSamples.length;
		int blocksPerPixel = (int) (blocks / (width * sx));
		int errorBlock = 0;
		int errorBlocks = 0;
		double error = blocks / (width * sx) - blocksPerPixel;
		double errorSum = 0;

		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.setFill(waveform.getBackgroundColor());
		ctx.fillRect(0, 0, width + 0.5, height + 0.5);
		ctx.setStroke(waveform.getWaveColor());

		double half = height / 2;

		int blocksRead = 0;

		for (int x = 0; x < width * sx; x++) {
			if (errorSum > 1) {
				errorSum -= 1;
				errorBlock = 1;
				errorBlocks++;
			}

			double pos = 0;
			double neg = 0;

			int blocksToRead = blocksPerPixel + errorBlock;
			if (blocksToRead + blocksRead >= blocks) {
				blocksToRead = blocks - blocksRead - 1;
			}

			for (int i = 0; i < blocksToRead; i++) {
				int offset = x * blocksPerPixel + i + errorBlocks;

				pos = Math.max(pos, data.posSamples[offset]);
				neg = Math.max(neg, data.negSamples[offset]);
			}

			double y1 = pos * half;
			double y2 = neg * half;

			ctx.strokeLine(x + tx, half - y1, x + tx, half + y2);

			blocksRead += blocksToRead;

			errorSum += error;
			errorBlock = 0;
		}

		ctx.setStroke(waveform.getWaveCenterColor());

		ctx.strokeLine(0, half, width, half);
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + bottomInset + 120;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + rightInset + 10;
	}

	private void initLayout(Waveform control) {
		AudioTrack track = control.getMediaTrack();

		canvas = new Canvas();
		canvas.widthProperty().bind(control.widthProperty());
		canvas.heightProperty().bind(control.heightProperty());
		canvas.widthProperty().addListener(o -> updateControl());
		canvas.heightProperty().addListener(o -> updateControl());

		getChildren().add(canvas);

		if (nonNull(track.getWaveformData())) {
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
}
