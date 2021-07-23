package org.lecturestudio.swing.components.previews;

import java.awt.Color;

import javax.swing.JComponent;

public abstract class ToolPreview extends JComponent {

    /**
     * Set the color of the preview.
     *
     * @param color The color.
     */
    abstract public void setColor(Color color);

    /**
     * Set the width of the preview.
     *
     * @param width The width.
     */
    abstract public void setWidth(float width);

}
