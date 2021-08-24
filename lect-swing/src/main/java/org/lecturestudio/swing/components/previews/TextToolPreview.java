package org.lecturestudio.swing.components.previews;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.renderer.TextRenderer;

public class TextToolPreview extends ToolPreview {

    private final TextRenderer renderer;

    private final TextShape textShape;

    public TextToolPreview() {
        renderer = new TextRenderer();
        textShape = new TextShape();
    }

    @Override
    public void setColor(Color color) {
        textShape.setTextColor(ColorConverter.INSTANCE.from(color));
        repaint();
    }

    @Override
    public void setWidth(float width) {
        textShape.setFont(new Font("Arial", width));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        textShape.setText("AaBbYyZz");
        textShape.setLocation(new PenPoint2D(getWidth() / 10f, getHeight() / 4f));

        renderer.render(textShape, (Graphics2D) g);
    }
}
