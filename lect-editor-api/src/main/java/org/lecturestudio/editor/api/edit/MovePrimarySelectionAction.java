package org.lecturestudio.editor.api.edit;

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;

public class MovePrimarySelectionAction implements EditAction {

	private final DoubleProperty primarySelectionProperty;
	private final double newPosition;

	private final double oldPosition;

	public MovePrimarySelectionAction(DoubleProperty primarySelectionProperty, double newPosition) {

		this.primarySelectionProperty = primarySelectionProperty;
		this.newPosition = newPosition;
		this.oldPosition = primarySelectionProperty.get();
	}

	@Override
	public void undo() throws RecordingEditException {
		primarySelectionProperty.set(oldPosition);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		primarySelectionProperty.set(newPosition);
	}
}
