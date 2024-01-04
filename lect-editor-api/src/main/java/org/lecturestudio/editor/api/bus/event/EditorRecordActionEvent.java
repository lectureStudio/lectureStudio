package org.lecturestudio.editor.api.bus.event;

import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.recording.action.PlaybackAction;

public class EditorRecordActionEvent extends RecordActionEvent {
    /**
     * In use to differentiate between events from the Editor and already recorded events
     *
     * @param action The playback action.
     */
    public EditorRecordActionEvent(PlaybackAction action) {
        super(action);
    }
}