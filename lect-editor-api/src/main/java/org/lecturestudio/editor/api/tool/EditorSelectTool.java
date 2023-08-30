package org.lecturestudio.editor.api.tool;

import static java.util.Objects.nonNull;

import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.tool.SelectTool;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.editor.api.controller.EditorToolController;

/**
 * Changes the initial positions of the selected shape, by changing the recorded points.
 * {@link org.lecturestudio.editor.api.edit.ModifyPlaybackActionPositionsAction}
 * Does not record any PlaybackActions itself
 */
public class EditorSelectTool extends SelectTool {

    private final EditorToolController toolController;
    private PenPoint2D totalDelta;

    public EditorSelectTool(EditorToolController toolController) {
        super(toolController);
        this.toolController = toolController;
    }

    @Override
    public void begin(PenPoint2D point, Page page) {
        this.page = page;
        this.sourcePoint = point.clone();
        this.totalDelta = point.clone();

        selectedShape = getTopLevelShape(point, page);

        if (nonNull(selectedShape)) {
            removeSelection();

            selectedShape.setSelected(true);

            fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
        }
        else {
            if (removeSelection()) {
                fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
            }
        }
    }

    @Override
    public void execute(PenPoint2D point) {
        if (nonNull(selectedShape)) {
            sourcePoint.subtract(point);

            selectedShape.moveByDelta(sourcePoint);

            sourcePoint.set(point);

            fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
        }
    }

    @Override
    public void end(PenPoint2D point) {
        if (nonNull(selectedShape)) {
            sourcePoint.subtract(point);

            selectedShape.moveByDelta(sourcePoint);

            sourcePoint.set(point);

            fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));

            selectedShape.setSelected(false);

            toolController.getRecordingFileService().modifyPlaybackActionPositions(selectedShape.getHandle(), page.getPageNumber(), (PenPoint2D) totalDelta.subtract(point));
        }
    }
}
