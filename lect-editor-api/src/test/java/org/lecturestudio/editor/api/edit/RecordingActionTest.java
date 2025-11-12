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
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.recording.mock.MockRecording;

/**
 * Tests for atomic operation functionality in RecordingAction.
 */
class RecordingActionTest {

	@Test
	void testAtomicExecutionSuccess() throws RecordingEditException {
		// Test successful atomic execution
		Recording recording = new MockRecording();
		EditAction action1 = mock(EditAction.class);
		EditAction action2 = mock(EditAction.class);
		EditAction action3 = mock(EditAction.class);

		List<EditAction> actions = Arrays.asList(action1, action2, action3);
		TestRecordingAction recordingAction = new TestRecordingAction(recording, actions);

		recordingAction.execute();

		// All actions should be executed
		verify(action1).execute();
		verify(action2).execute();
		verify(action3).execute();
	}

	@Test
	void testAtomicExecutionFailureRollback() throws RecordingEditException {
		// Test that failed execution rolls back successfully executed actions
		Recording recording = new MockRecording();
		EditAction action1 = mock(EditAction.class);
		EditAction action2 = mock(EditAction.class);
		EditAction action3 = mock(EditAction.class);

		// Action 2 fails during execution
		doThrow(new RecordingEditException("Test failure")).when(action2).execute();

		List<EditAction> actions = Arrays.asList(action1, action2, action3);
		TestRecordingAction recordingAction = new TestRecordingAction(recording, actions);

		assertThrows(RecordingEditException.class, () -> recordingAction.execute());

		// Action 1 should be executed and then undone (rolled back)
		verify(action1).execute();
		verify(action1).undo();

		// Action 2 should be executed but not undone (since it failed)
		verify(action2).execute();
		verify(action2, never()).undo();

		// Action 3 should never be executed
		verify(action3, never()).execute();
		verify(action3, never()).undo();
	}

	@Test
	void testAtomicExecutionRollbackFailure() throws RecordingEditException {
		// Test that if rollback fails, other rollbacks continue
		Recording recording = new MockRecording();
		EditAction action1 = mock(EditAction.class);
		EditAction action2 = mock(EditAction.class);

		// Action 2 fails during execution
		doThrow(new RecordingEditException("Test failure")).when(action2).execute();
		// Action 1 fails during undo (rollback)
		doThrow(new RecordingEditException("Rollback failure")).when(action1).undo();

		List<EditAction> actions = Arrays.asList(action1, action2);
		TestRecordingAction recordingAction = new TestRecordingAction(recording, actions);

		assertThrows(RecordingEditException.class, () -> recordingAction.execute());

		// Both actions should be executed
		verify(action1).execute();
		verify(action2).execute();

		// Action 1 should attempt undo but fail
		verify(action1).undo();
	}

	@Test
	void testRedoAfterSuccessfulExecution() throws RecordingEditException {
		// Test that redo works correctly after successful execution
		Recording recording = new MockRecording();
		EditAction action1 = mock(EditAction.class);
		EditAction action2 = mock(EditAction.class);

		List<EditAction> actions = Arrays.asList(action1, action2);
		TestRecordingAction recordingAction = new TestRecordingAction(recording, actions);

		recordingAction.execute();
		recordingAction.redo();

		// Each action should be executed twice (once for execute, once for redo)
		verify(action1, times(2)).execute();
		verify(action2, times(2)).execute();
	}

	@Test
	void testUndoAfterSuccessfulExecution() throws RecordingEditException {
		// Test that undo works correctly after successful execution
		Recording recording = new MockRecording();
		EditAction action1 = mock(EditAction.class);
		EditAction action2 = mock(EditAction.class);

		List<EditAction> actions = Arrays.asList(action1, action2);
		TestRecordingAction recordingAction = new TestRecordingAction(recording, actions);

		recordingAction.execute();
		recordingAction.undo();

		// Each action should be executed once and undone once
		verify(action1).execute();
		verify(action1).undo();
		verify(action2).execute();
		verify(action2).undo();
	}

	/**
	 * Test implementation of RecordingAction for testing purposes.
	 */
	private static class TestRecordingAction extends RecordingAction {

		public TestRecordingAction(Recording recording, List<EditAction> actions) {
			super(recording, actions);
		}

		@Override
		public void execute() throws RecordingEditException {
			super.execute();
		}

		@Override
		public void undo() throws RecordingEditException {
			super.undo();
		}

		@Override
		public void redo() throws RecordingEditException {
			super.redo();
		}
	}
}
