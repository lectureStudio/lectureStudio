package org.lecturestudio.editor.api.stylus;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.editor.api.controller.EditorToolController;
import org.lecturestudio.stylus.StylusEvent;

public class EditorStylusHandler extends StylusHandler {
    private final EditorToolController toolController;

    public EditorStylusHandler(EditorToolController toolController, Runnable cursorEnteredCallback) {
        super(toolController, cursorEnteredCallback);
        this.toolController = toolController;
    }

    @Override
    public void onButtonDown(StylusEvent stylusEvent, Rectangle2D viewBounds) {
        toolController.setIsEditing(true);
        super.onButtonDown(stylusEvent, viewBounds);
    }

    @Override
    public void onButtonUp(StylusEvent stylusEvent, Rectangle2D viewBounds) {
        super.onButtonUp(stylusEvent, viewBounds);
        toolController.setIsEditing(false);
    }
}