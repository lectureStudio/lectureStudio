package org.lecturestudio.swing.components.previews;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.ArrowShape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.renderer.ArrowRenderer;


public class ArrowToolPreview extends ToolPreview {

    private final ArrowRenderer renderer;

    private final ArrowShape arrowShape;

    public ArrowToolPreview() {
        renderer = new ArrowRenderer();
        arrowShape = new ArrowShape(new Stroke());
    }

    @Override
    public void setColor(Color color) {
        arrowShape.getStroke().setColor(ColorConverter.INSTANCE.from(color));
        repaint();
    }

    @Override
    public void setWidth(float width) {
        arrowShape.getStroke().setWidth(width * 1.1);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        arrowShape.setStartPoint(new PenPoint2D(10f, getHeight() / 2f));
        arrowShape.setEndPoint(new PenPoint2D(getWidth() - 10f, getHeight() / 2f));

        renderer.render(arrowShape, (Graphics2D) g);
    }
}
