package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Screen;

import java.awt.*;

public class ExternalWindowConfiguration {
    /**
     * The screen object defining the screen bounds of the connected display.
     */
    private final ObjectProperty<Screen> screen = new ObjectProperty<>();

    /**
     * The external window position.
     */
    private final ObjectProperty<Point> position = new ObjectProperty<>();

    /**
     * The external window size.
     */
    private final ObjectProperty<Dimension> size = new ObjectProperty<>();

    /**
     * Indicates whether the external window should be displayed.
     */
    private final BooleanProperty enabled = new BooleanProperty();

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setPosition(Point position) {
        this.position.set(position);
    }

    public Point getPosition() {
        return position.get();
    }

    public void setScreen(Screen screen) {
        this.screen.set(screen);
    }

    public Screen getScreen() {
        return screen.get();
    }

    public void setSize(Dimension size) {
        this.size.set(size);
    }

    public Dimension getSize() {
        return size.get();
    }
}
