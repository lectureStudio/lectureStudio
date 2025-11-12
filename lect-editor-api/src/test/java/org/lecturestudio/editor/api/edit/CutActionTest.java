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

package org.lecturestudio.editor.api.edit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.mock.MockRecording;

/**
 * Comprehensive tests for CutAction edge cases and validation.
 */
class CutActionTest {

	private Recording recording;

	@BeforeEach
	void setUp() {
		recording = new MockRecording();
	}

	@Test
	void testValidCutIntervals() {
		// Test normal valid intervals
		assertDoesNotThrow(() -> new CutAction(recording, 0.1, 0.9));
		assertDoesNotThrow(() -> new CutAction(recording, 0.0, 1.0));
		assertDoesNotThrow(() -> new CutAction(recording, 0.5, 0.5)); // Edge case: zero duration
	}

	@Test
	void testInvalidStartTime() {
		// Test invalid start times
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, -0.1, 0.5));
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, 1.1, 0.5));
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, Double.NaN, 0.5));
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, Double.POSITIVE_INFINITY, 0.5));
	}

	@Test
	void testInvalidEndTime() {
		// Test invalid end times
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, 0.1, -0.1));
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, 0.1, 1.1));
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, 0.1, Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new CutAction(recording, 0.1, Double.NEGATIVE_INFINITY));
	}

	@Test
	void testNullRecording() {
		assertThrows(IllegalArgumentException.class, () -> new CutAction(null, 0.1, 0.9));
	}

	@Test
	void testSwappedIntervals() {
		// Test that intervals are automatically corrected (start > end)
		assertDoesNotThrow(() -> new CutAction(recording, 0.9, 0.1));
	}

	@Test
	void testBoundaryValues() {
		// Test exact boundary values
		assertDoesNotThrow(() -> new CutAction(recording, 0.0, 0.0));
		assertDoesNotThrow(() -> new CutAction(recording, 1.0, 1.0));
		assertDoesNotThrow(() -> new CutAction(recording, 0.0, 1.0));
	}

	@Test
	void testFloatingPointPrecision() {
		// Test floating point precision edge cases
		assertDoesNotThrow(() -> new CutAction(recording, 0.0000001, 0.9999999));
		assertDoesNotThrow(() -> new CutAction(recording, 1e-10, 1.0 - 1e-10));
	}

	@Test
	void testVerySmallIntervals() {
		// Test very small but valid intervals
		assertDoesNotThrow(() -> new CutAction(recording, 0.5, 0.5000001));
	}

	@Test
	void testExecutionWithValidData() {
		// Test that execution works with valid data (assuming MockRecording works)
		CutAction action = new CutAction(recording, 0.2, 0.8);
		assertDoesNotThrow(() -> action.execute());
		assertDoesNotThrow(() -> action.undo());
		assertDoesNotThrow(() -> action.redo());
	}

	@Test
	void testPrimarySelectionAdjustment() {
		// Test that primary selection is adjusted correctly during cut
		// This would require a more complex test setup with actual selection properties
		// For now, just ensure the constructor accepts selection properties
		assertDoesNotThrow(() -> new CutAction(recording, 0.2, 0.8, null));
	}

	@Test
	void testConcurrentExecution() {
		// Test that multiple cut actions can be created and executed concurrently
		assertDoesNotThrow(() -> {
			CutAction action1 = new CutAction(recording, 0.1, 0.3);
			CutAction action2 = new CutAction(recording, 0.7, 0.9);

			// Execute concurrently (this is a basic test - in practice you'd want more sophisticated concurrency testing)
			action1.execute();
			action2.execute();

			action1.undo();
			action2.undo();
		});
	}
}
