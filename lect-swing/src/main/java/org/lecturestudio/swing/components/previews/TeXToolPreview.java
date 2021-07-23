package org.lecturestudio.swing.components.previews;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.renderer.TeXRenderer;

public class TeXToolPreview extends ToolPreview {

    private final TeXRenderer renderer;

    private final TeXShape texShape;

    public TeXToolPreview() {
        renderer = new TeXRenderer();
        texShape = new TeXShape();
    }

    @Override
    public void setColor(Color color) {
        texShape.setTextColor(ColorConverter.INSTANCE.from(color));
        repaint();
    }

    @Override
    public void setWidth(float width) {
        texShape.setFont(new TeXFont(TeXFont.Type.SERIF, width));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        texShape.setText("f(x)=mx+t");
        texShape.setLocation(new PenPoint2D(getWidth() / 10f, getHeight() / 4f));

        renderer.render(texShape, (Graphics2D) g);
    }
}
