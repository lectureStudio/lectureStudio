package org.lecturestudio.editor.api.edit;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.edit.EditAction;

public class HideAndMoveNextPageAction extends RecordingAction {
	/**
	 * Removes the selected Page and changes the timestamp of the next page to the timestamp of the removed page.
	 */
	public HideAndMoveNextPageAction(Recording recording, int pageNumber, int timestamp) {
		super(recording, createActions(recording, pageNumber, timestamp));
	}

	public static List<EditAction> createActions(Recording recording, int pageNumber, int timestamp) {
		List<EditAction> actions = new ArrayList<>(HidePageAction.createActions(recording, pageNumber));
		actions.add(new MovePageAction(recording, pageNumber, timestamp));

		return actions;
	}
}
