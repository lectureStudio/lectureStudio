package org.lecturestudio.editor.api.bus.event;

import org.lecturestudio.core.bus.event.ToolSelectionEvent;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;

public class EditorToolSelectionEvent extends ToolSelectionEvent {
	/**
	 * Create the {@link ToolSelectionEvent} with specified tool type and paint settings.
	 * This extra class is required so that the tool toolbar is not affected by the tool actions from the playback.
	 *
	 * @param toolType The tool type.
	 * @param settings The paint settings.
	 */
	public EditorToolSelectionEvent(ToolType toolType, PaintSettings settings) {
		super(toolType, settings);
	}
}