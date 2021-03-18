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

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;

import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.util.AudacityColormapByteBgra;
import org.lecturestudio.core.util.Colormap;
import org.lecturestudio.media.audio.Spectrogram;

public class SpectrogramChart extends LineChart<Number, Number> {

	private Colormap<ByteBuffer> colormap;

	private ImageView imageView;

	private PixelBuffer<ByteBuffer> pixelBuffer;


	public SpectrogramChart(@NamedArg("xAxis") NumberAxis xAxis, @NamedArg("yAxis") NumberAxis yAxis) {
		super(xAxis, yAxis);

		initialize();
	}

	public void setColormap(Colormap<ByteBuffer> colormap) {
		requireNonNull(colormap);

		this.colormap = colormap;
	}

	public void setSpectrogram(Spectrogram spectrogram) {
		final int width = spectrogram.getWidth();
		final int height = spectrogram.getHeight();

		updateAxes(spectrogram);

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4);
		PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
		pixelBuffer = new PixelBuffer<>(width, height, byteBuffer, pixelFormat);

		imageView.setImage(new WritableImage(pixelBuffer));

		double[][] data = spectrogram.getData();

		for (int x = 0; x < pixelBuffer.getWidth(); x++) {
			for (int y = 0; y < pixelBuffer.getHeight(); y++) {
				int offset = (x + y * pixelBuffer.getWidth()) * 4;

				colormap.setPixel(data[x][y], byteBuffer, offset);
			}
		}

		Platform.runLater(() -> pixelBuffer.updateBuffer(pixelBuffer -> null));
	}

	private void updateAxes(Spectrogram spectrogram) {
		AudioFormat audioFormat = spectrogram.getAudioFormat();
		double shiftSize = spectrogram.getFrameLength() / (double) spectrogram.getWidth();
		double shiftDuration = shiftSize / audioFormat.getSampleRate();

		NumberAxis xAxis = (NumberAxis) getXAxis();
		xAxis.setUpperBound(spectrogram.getWidth() * shiftDuration);

		NumberAxis yAxis = (NumberAxis) getYAxis();
		yAxis.setUpperBound(audioFormat.getSampleRate() * 0.5);
	}

	private void initialize() {
		setColormap(new AudacityColormapByteBgra(-100.0, 0.0));

		Region plotBackground = (Region) lookup(".chart-plot-background");
		plotBackground.setVisible(false);

		imageView = new ImageView();
		imageView.translateXProperty().bind(plotBackground.layoutXProperty());
		imageView.translateYProperty().bind(plotBackground.layoutYProperty());
		imageView.fitWidthProperty().bind(plotBackground.widthProperty());
		imageView.fitHeightProperty().bind(plotBackground.heightProperty());

		getChartChildren().add(imageView);

		imageView.toBack();
	}
}
