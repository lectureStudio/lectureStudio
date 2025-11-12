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
		// Clamp values to valid range [0, 1] and handle invalid values
		this.newPosition = clamp(newPosition);
		this.oldPosition = clamp(primarySelectionProperty.get());
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
	
	private static double clamp(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return 0.0;
		}
		return Math.max(0.0, Math.min(1.0, value));
	}
}
