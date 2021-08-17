package org.lecturestudio.swing.components.previews;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.EllipseShape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.renderer.EllipseRenderer;

public class EllipseToolPreview extends ToolPreview {

    private final EllipseRenderer renderer;

    private final EllipseShape ellipseShape;

    public EllipseToolPreview() {
        renderer = new EllipseRenderer();
        ellipseShape = new EllipseShape(new Stroke());
    }

    @Override
    public void setColor(Color color) {
        ellipseShape.getStroke().setColor(ColorConverter.INSTANCE.from(color));
        repaint();
    }

    @Override
    public void setWidth(float width) {
        ellipseShape.getStroke().setWidth(width * 1.2);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ellipseShape.setStartPoint(new PenPoint2D(15f, getHeight() / 8f));
        ellipseShape.setEndPoint(new PenPoint2D(getWidth() - 15f, 7f * getHeight() / 8f));

        renderer.render(ellipseShape, (Graphics2D) g);
    }
}
