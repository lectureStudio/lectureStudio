package org.lecturestudio.editor.api.edit;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.LocationModifiable;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.TextLocationChangeAction;
import org.lecturestudio.core.recording.action.TextSelectionExtAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class ModifyPlaybackActionPositionsAction extends RecordedObjectAction<RecordedEvents> {
    private final Recording recording;
    private final int handle;
    private final int pageNumber;
    private final PenPoint2D delta;
    private final RecordedPage recordedPage;
    private final Page page;
    private final List<PlaybackAction> handleActions;
    private Interval<Double> editDuration;

    /**
     * Constructor for a {@code RecordingAction} to be used by specific action
     * implementations.
     *
     * @param recording The recording on which to apply this action.
     * @param actions   The sub-actions to manage.
     */
    public ModifyPlaybackActionPositionsAction(Recording recording, int handle, int pageNumber, PenPoint2D delta) {
        super(recording.getRecordedEvents());
        this.recording = recording;
        this.handle = handle;
        this.pageNumber = pageNumber;
        this.delta = delta;
        this.recordedPage = recording.getRecordedEvents().getRecordedPage(pageNumber);
        this.page = recording.getRecordedDocument().getDocument().getPage(pageNumber);
        this.handleActions = getActionsFromHandle();
        this.editDuration = null;

        if (!handleActions.isEmpty()) {
            int totalDuration = (int) recording.getRecordingHeader().getDuration();
            editDuration = new Interval<>((double) handleActions.get(0).getTimestamp() / totalDuration,
                    (double) handleActions.get(handleActions.size() - 1).getTimestamp() / totalDuration);
        }
    }

    @Override
    public void undo() throws RecordingEditException {
        movePointsByDelta(handleActions, delta.clone().invert());
        recording.fireChangeEvent(Content.EVENTS_CHANGED, editDuration);
    }

    @Override
    public void redo() throws RecordingEditException {
        movePointsByDelta(handleActions, delta);
        recording.fireChangeEvent(Content.EVENTS_CHANGED, editDuration);
    }

    @Override
    public void execute() throws RecordingEditException {
        movePointsByDelta(handleActions, delta);
        recording.fireChangeEvent(Content.EVENTS_ADDED, editDuration);
    }

    private List<PlaybackAction> getActionsFromHandle() {
        List<PlaybackAction> handleActions = new ArrayList<>();
        List<PlaybackAction> pageActions = recordedPage.getPlaybackActions();

        boolean actionStarted = false;
        for (PlaybackAction action : pageActions) {
            if (action instanceof TextLocationChangeAction textLocationChangeAction) {
                checkActionHandle(textLocationChangeAction.getHandle(), handleActions, action);
            }
            else if (action instanceof TextSelectionExtAction textSelectionExtAction) {
                checkActionHandle(textSelectionExtAction.getHandle(), handleActions, action);
            }
            else if (actionStarted &&
                    (action.getType() == ActionType.TOOL_BEGIN || action.getType() == ActionType.TOOL_EXECUTE)) {
                handleActions.add(action);
            }
            else if (actionStarted && action.getType() == ActionType.TOOL_END) {
                handleActions.add(action);
                actionStarted = false;
            }
            else if (action.hasHandle()) {
                actionStarted = checkActionHandle(action.getHandle(), handleActions, action);
            }
        }

        return handleActions;
    }

    public void movePointsByDelta(List<PlaybackAction> actions, PenPoint2D delta) {
        getPageShape().moveByDelta(delta);

        for (PlaybackAction action : actions) {
            if (action instanceof LocationModifiable locationModifiable) {
                locationModifiable.moveByDelta(delta);
            }
        }
    }

    private Shape getPageShape() {
        return page.getShape(handle);
    }

    private boolean checkActionHandle(int actionHandle, List<PlaybackAction> handleActions, PlaybackAction action) {
        if (actionHandle == handle) {
            handleActions.add(action);
            return true;
        }
        return false;
    }
}
