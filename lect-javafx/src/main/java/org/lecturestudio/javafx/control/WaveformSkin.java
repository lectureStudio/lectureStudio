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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.media.audio.WaveformData;
import org.lecturestudio.media.track.AudioTrack;
import org.lecturestudio.media.track.control.AdjustAudioVolumeControl;
import org.lecturestudio.media.track.control.MediaTrackControl;

public class WaveformSkin extends MediaTrackControlSkinBase {

	private final Consumer<RandomAccessAudioStream> trackListener = recordedPages -> {
		updateControl();
	};

	private final Runnable controlChangeListener = this::updateControl;

	private final Waveform waveform;

	private Canvas canvas;

	private List<AdjustAudioVolumeControl> volumeControls;


	protected WaveformSkin(Waveform control) {
		super(control);

		waveform = control;
		volumeControls = List.of();

		initLayout(control);
	}

	@Override
	public void dispose() {
		super.dispose();

//		unregisterChangeListeners(waveform.waveformDataProperty());
	}

	@Override
	protected void updateControl() {
		FxUtils.invoke(this::render);
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

	protected void render() {
		final WaveformData data = waveform.getMediaTrack().getWaveformData();
		final Affine transform = waveform.getTransform();
		final double width = canvas.getWidth();
		final double height = canvas.getHeight();

		if (isNull(data) || isNull(transform) || width == 0 || height == 0) {
			return;
		}

		// Use effective duration that accounts for cuts/exclusions
		final long effectiveDurationMs = (waveform.getMediaTrack()).getEffectiveDurationMs();
		if (effectiveDurationMs <= 0) {
			return;
		}

		double sx = transform.getMxx();
		double tx = transform.getTx() * width;

		double scaledWidth = width * sx;

		// Sample blocks per pixel, scaled by effective duration ratio
		int blocks = data.negSamples.length;

		// Calculate how many waveform samples we need to cover the scaled width
		// Since blocks represents the effective duration, we map blocks directly to scaledWidth
		double samplesPerPixel = (double) blocks / scaledWidth;

		// For high zoom levels, we want to render the full width
		// If we have fewer samples than pixels, we'll interpolate
		int pixelsToRender = (int) scaledWidth;

		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.setFill(waveform.getBackgroundColor());
		ctx.fillRect(0, 0, width + 0.5, height + 0.5);
		ctx.setStroke(waveform.getWaveColor());

		double half = height / 2;

		for (int x = 0; x < pixelsToRender; x++) {
			// Calculate which waveform sample(s) to use for this pixel
			int sampleStart, sampleEnd;

			if (samplesPerPixel >= 1.0) {
				// Downsampling: multiple samples per pixel
				sampleStart = (int) (x * samplesPerPixel);
				sampleEnd = (int) ((x + 1) * samplesPerPixel);
			} else {
				// Upsampling: interpolate between samples or repeat samples
				// For high zoom levels, we want smooth interpolation
				int sampleIndex = (int) (x * samplesPerPixel);
				sampleStart = sampleEnd = Math.min(sampleIndex, blocks - 1);
			}

			// Ensure bounds
			sampleStart = Math.max(0, Math.min(sampleStart, blocks - 1));
			sampleEnd = Math.max(sampleStart, Math.min(sampleEnd, blocks - 1));

			// Find max values in this range
			float pos = 0, neg = 0;
			for (int i = sampleStart; i <= sampleEnd; i++) {
				if (i >= 0 && i < blocks) {
					pos = Math.max(pos, data.posSamples[i]);
					neg = Math.max(neg, data.negSamples[i]);
				}
			}

			double y1 = pos * half;
			double y2 = neg * half;

			// Scale sample values only for visual representation.
			for (AdjustAudioVolumeControl control : volumeControls) {
				// Convert control intervals from time ratio to pixel coordinates
				// The controls work on the effective timeline (0-1 range)
				long controlStartMs = (long) (control.getInterval().getStart() * effectiveDurationMs);
				long controlEndMs = (long) (control.getInterval().getEnd() * effectiveDurationMs);

				// Convert to pixel positions based on effective duration
				double x1 = (controlStartMs * scaledWidth) / effectiveDurationMs;
				double x2 = (controlEndMs * scaledWidth) / effectiveDurationMs;

				if (x > x1 && x < x2) {
					y1 *= control.getVolumeScalar();
					y2 *= control.getVolumeScalar();
				}
			}

			ctx.strokeLine(x + tx, half - y1, x + tx, half + y2);
		}

		ctx.setStroke(waveform.getWaveCenterColor());

		ctx.strokeLine(0, half, width, half);
	}

	private void addMediaTrackControl(MediaTrackControl control) {
		Node controlNode = null;

		if (control instanceof AdjustAudioVolumeControl volumeControl) {
			AdjustAudioVolumeSelection trackSelection = new AdjustAudioVolumeSelection();
			trackSelection.setVolumeScalar(volumeControl.getVolumeScalar());
			trackSelection.setTrackControl(volumeControl);
			trackSelection.setRemoveAction(event -> volumeControl.remove());

			controlNode = trackSelection;
		}

		if (nonNull(controlNode)) {
			control.addChangeListener(controlChangeListener);

			controlNodeMap.put(control, controlNode);

			getChildren().add(controlNode);
		}
	}

	private void removeMediaTrackControl(MediaTrackControl control) {
		Node trackSelection = controlNodeMap.get(control);

		if (nonNull(trackSelection)) {
			control.removeChangeListener(controlChangeListener);

			getChildren().remove(trackSelection);
		}
	}

	private void initLayout(Waveform control) {
		AudioTrack track = control.getMediaTrack();

		canvas = new Canvas();
		canvas.widthProperty().bind(control.widthProperty());
		canvas.heightProperty().bind(control.heightProperty());
		canvas.widthProperty().addListener(o -> updateControl());
		canvas.heightProperty().addListener(o -> updateControl());

		getChildren().add(canvas);

		if (nonNull(track)) {
			track.addChangeListener(trackListener);

			if (nonNull(track.getWaveformData())) {
				updateControl();
			}
		}
		if (track.getControls().size() > 1) {
			updateTrackControls(track);
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
		track.getControls().addListener(new ListChangeListener<>() {

			@Override
			public void listItemsInserted(ObservableList<MediaTrackControl> list,
					int startIndex, int itemCount) {
				Platform.runLater(() -> {
					addMediaTrackControl(list.get(startIndex));

					updateVolumeControls();
					updateControl();
				});
			}

			@Override
			public void listItemsRemoved(ObservableList<MediaTrackControl> list,
					int startIndex, int itemCount) {
				Platform.runLater(() -> {
					if (list.isEmpty()) {
						for (Iterator<Entry<MediaTrackControl, Node>> it = controlNodeMap
								.entrySet().iterator(); it.hasNext(); ) {
							Entry<MediaTrackControl, Node> entry = it.next();

							removeMediaTrackControl(entry.getKey());
							it.remove();
						}
					}

					for (Iterator<Entry<MediaTrackControl, Node>> it = controlNodeMap
							.entrySet().iterator(); it.hasNext(); ) {
						Entry<MediaTrackControl, Node> entry = it.next();

						if (!list.contains(entry.getKey())) {
							removeMediaTrackControl(entry.getKey());
							it.remove();
						}
					}

					updateVolumeControls();
					updateControl();
				});
			}
		});
	}

	private void updateTrackControls(AudioTrack track) {
		for (MediaTrackControl control : track.getControls()) {
			addMediaTrackControl(control);
		}

		updateVolumeControls();
		updateControl();
	}

	private void updateVolumeControls() {
		if (controlNodeMap.isEmpty()) {
			volumeControls.clear();
		}
		else {
			volumeControls = controlNodeMap.keySet().stream()
					.filter(AdjustAudioVolumeControl.class::isInstance)
					.map(AdjustAudioVolumeControl.class::cast)
					.collect(Collectors.toList());
		}
	}
}
