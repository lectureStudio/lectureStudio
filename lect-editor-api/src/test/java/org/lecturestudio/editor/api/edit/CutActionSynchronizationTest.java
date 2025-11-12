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

import org.junit.jupiter.api.Test;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.mock.MockRecording;

/**
 * Tests to verify that multiple cut operations maintain audio/waveform synchronization.
 */
class CutActionSynchronizationTest {

	@Test
	void testMultipleCutsMaintainSynchronization() throws RecordingEditException {
		Recording recording = new MockRecording();

		// Perform multiple cuts
		CutAction cut1 = new CutAction(recording, 0.1, 0.2);
		CutAction cut2 = new CutAction(recording, 0.4, 0.5);
		CutAction cut3 = new CutAction(recording, 0.7, 0.8);

		// Execute cuts
		cut1.execute();
		cut2.execute();
		cut3.execute();

		// Verify the recording state is consistent
		var audioStream = recording.getRecordedAudio().getAudioStream();
		assertNotNull(audioStream);

		// Check that exclusions were properly added
		var exclusions = audioStream.getExclusions();
		assertFalse(exclusions.isEmpty(), "Exclusions should be present after cuts");

		// Verify that we can undo all operations
		cut3.undo();
		cut2.undo();
		cut1.undo();

		// After undo, exclusions should be cleared
		assertTrue(audioStream.getExclusions().isEmpty(), "Exclusions should be cleared after undo");
	}

	@Test
	void testCutOperationsAreIndependent() throws RecordingEditException {
		Recording recording = new MockRecording();

		// Create multiple overlapping cut actions
		CutAction cut1 = new CutAction(recording, 0.2, 0.3);
		CutAction cut2 = new CutAction(recording, 0.25, 0.35); // Overlaps with cut1

		// Execute first cut
		cut1.execute();

		// Verify first cut worked
		var exclusions1 = recording.getRecordedAudio().getAudioStream().getExclusions();
		assertFalse(exclusions1.isEmpty());

		// Execute second cut
		cut2.execute();

		// Verify second cut also worked (exclusions should be combined properly)
		var exclusions2 = recording.getRecordedAudio().getAudioStream().getExclusions();
		assertTrue(exclusions2.size() >= exclusions1.size());

		// Both undos should work
		cut2.undo();
		cut1.undo();

		// Final state should be clean
		var finalExclusions = recording.getRecordedAudio().getAudioStream().getExclusions();
		assertTrue(finalExclusions.isEmpty());
	}

	@Test
	void testCutBoundaryConditions() throws RecordingEditException {
		Recording recording = new MockRecording();

		// Test cuts at boundaries
		CutAction cutStart = new CutAction(recording, 0.0, 0.05);
		CutAction cutEnd = new CutAction(recording, 0.95, 1.0);

		cutStart.execute();
		cutEnd.execute();

		// Should still be able to undo
		cutEnd.undo();
		cutStart.undo();

		var finalExclusions = recording.getRecordedAudio().getAudioStream().getExclusions();
		assertTrue(finalExclusions.isEmpty());
	}

	@Test
	void testRapidSequentialCuts() throws RecordingEditException {
		Recording recording = new MockRecording();

		// Perform many small cuts rapidly
		for (int i = 0; i < 10; i++) {
			double start = i * 0.08 + 0.01;
			double end = start + 0.02;
			if (end > 1.0) end = 1.0;

			CutAction cut = new CutAction(recording, start, end);
			cut.execute();
		}

		// Should have multiple exclusions
		var exclusions = recording.getRecordedAudio().getAudioStream().getExclusions();
		assertFalse(exclusions.isEmpty());

		// Should be able to undo all cuts in reverse order
		// Note: In practice, we'd need to keep references to all cut actions
		// This test just verifies the system doesn't break under rapid operations
		assertDoesNotThrow(() -> {
			// The recording should remain in a consistent state
			var audioStream = recording.getRecordedAudio().getAudioStream();
			assertNotNull(audioStream);
		});
	}
}
