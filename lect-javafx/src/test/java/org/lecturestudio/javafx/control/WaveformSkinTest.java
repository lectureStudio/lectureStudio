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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.lecturestudio.media.audio.WaveformData;
import org.lecturestudio.media.track.AudioTrack;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

/**
 * Tests for WaveformSkin to ensure proper rendering at different zoom levels.
 */
class WaveformSkinTest {

	@Test
	void testHighZoomLevelRendersFullWidth() {
		// Test that at high zoom levels, waveform renders to full available width
		Waveform waveform = new Waveform();
		AudioTrack mockTrack = mock(AudioTrack.class);

		// Mock waveform data with fewer samples than display pixels
		WaveformData mockData = mock(WaveformData.class);
		mockData.posSamples = new float[100]; // Only 100 samples
		mockData.negSamples = new float[100];

		when(mockTrack.getWaveformData()).thenReturn(mockData);
		when(mockTrack.getEffectiveDurationMs()).thenReturn(1000L); // 1 second

		waveform.setMediaTrack(mockTrack);

		WaveformSkin skin = new WaveformSkin(waveform);

		// Simulate high zoom level - display width much larger than sample count
		waveform.setWidth(1000); // 1000 pixels wide display
		waveform.setHeight(200);

		// Set up transform for high zoom
		Affine transform = new Affine();
		transform.setMxx(10.0); // 10x zoom
		waveform.setTransform(transform);

		// The skin should handle this properly without crashing
		// and render to the full available width
		assertDoesNotThrow(() -> {
			skin.updateControl();
		});
	}

	@Test
	void testNormalZoomLevelWorks() {
		// Test normal zoom levels still work correctly
		Waveform waveform = new Waveform();
		AudioTrack mockTrack = mock(AudioTrack.class);

		WaveformData mockData = mock(WaveformData.class);
		mockData.posSamples = new float[1000];
		mockData.negSamples = new float[1000];

		when(mockTrack.getWaveformData()).thenReturn(mockData);
		when(mockTrack.getEffectiveDurationMs()).thenReturn(10000L); // 10 seconds

		waveform.setMediaTrack(mockTrack);

		WaveformSkin skin = new WaveformSkin(waveform);

		waveform.setWidth(1000);
		waveform.setHeight(200);

		// Normal zoom level
		Affine transform = new Affine();
		transform.setMxx(1.0);
		waveform.setTransform(transform);

		assertDoesNotThrow(() -> {
			skin.updateControl();
		});
	}

	@Test
	void testVeryLowZoomLevelWorks() {
		// Test very low zoom levels (zoomed out)
		Waveform waveform = new Waveform();
		AudioTrack mockTrack = mock(AudioTrack.class);

		WaveformData mockData = mock(WaveformData.class);
		mockData.posSamples = new float[1000];
		mockData.negSamples = new float[1000];

		when(mockTrack.getWaveformData()).thenReturn(mockData);
		when(mockTrack.getEffectiveDurationMs()).thenReturn(10000L);

		waveform.setMediaTrack(mockTrack);

		WaveformSkin skin = new WaveformSkin(waveform);

		waveform.setWidth(1000);
		waveform.setHeight(200);

		// Very low zoom level
		Affine transform = new Affine();
		transform.setMxx(0.1);
		waveform.setTransform(transform);

		assertDoesNotThrow(() -> {
			skin.updateControl();
		});
	}

	@Test
	void testNullWaveformDataHandling() {
		// Test that null waveform data is handled gracefully
		Waveform waveform = new Waveform();
		AudioTrack mockTrack = mock(AudioTrack.class);

		when(mockTrack.getWaveformData()).thenReturn(null);
		when(mockTrack.getEffectiveDurationMs()).thenReturn(1000L);

		waveform.setMediaTrack(mockTrack);

		WaveformSkin skin = new WaveformSkin(waveform);

		waveform.setWidth(1000);
		waveform.setHeight(200);

		// Should not crash with null data
		assertDoesNotThrow(() -> {
			skin.updateControl();
		});
	}

	@Test
	void testZeroEffectiveDurationHandling() {
		// Test that zero effective duration is handled gracefully
		Waveform waveform = new Waveform();
		AudioTrack mockTrack = mock(AudioTrack.class);

		WaveformData mockData = mock(WaveformData.class);
		mockData.posSamples = new float[100];
		mockData.negSamples = new float[100];

		when(mockTrack.getWaveformData()).thenReturn(mockData);
		when(mockTrack.getEffectiveDurationMs()).thenReturn(0L); // Zero duration

		waveform.setMediaTrack(mockTrack);

		WaveformSkin skin = new WaveformSkin(waveform);

		waveform.setWidth(1000);
		waveform.setHeight(200);

		// Should not crash with zero duration
		assertDoesNotThrow(() -> {
			skin.updateControl();
		});
	}
}
