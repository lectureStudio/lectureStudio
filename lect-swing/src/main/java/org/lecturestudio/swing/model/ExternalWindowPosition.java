package org.lecturestudio.swing.model;

import org.lecturestudio.core.view.Screen;

import java.awt.*;

public class ExternalWindowPosition {
    private final Screen screen;

    private final Point position;

    public ExternalWindowPosition(Screen screen, Point position) {
        this.screen = screen;
        this.position = position;
    }

    public Screen getScreen() {
        return screen;
    }

    public Point getPosition() {
        return position;
    }
}
