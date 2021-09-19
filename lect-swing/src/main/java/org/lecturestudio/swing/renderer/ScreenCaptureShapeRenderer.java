/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.swing.renderer;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.ScreenCaptureShape;
import org.lecturestudio.core.model.shape.Shape;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ScreenCaptureShapeRenderer extends BaseRenderer {

    @Override
    public Class<? extends Shape> forClass() {
        return ScreenCaptureShape.class;
    }

    @Override
    protected void renderPrivate(Shape shape, Graphics2D context) {
        ScreenCaptureShape screenCaptureShape = (ScreenCaptureShape) shape;
        BufferedImage frame = screenCaptureShape.getFrame();

        Rectangle2D pageRect = shape.getBounds();
        double pageRatio = pageRect.getWidth() / pageRect.getHeight();

        // Store current transform
        AffineTransform canvasTransform = context.getTransform();

        double width = canvasTransform.getScaleX();
        double height = width / pageRatio;

        double scaleX = width / frame.getWidth();
        double scaleY = height / frame.getHeight();

        // Make sure to scale with same ratio
        double scale = Math.min(scaleX, scaleY);

        int offsetX = (scaleX > scale) ? (int) ((width - frame.getWidth() * scale) / 2f) : 0;
        int offsetY = (scaleY > scale) ? (int) ((height - frame.getHeight() * scale) / 2f) : 0;

        // Draw background color
        context.setColor(Color.BLACK);
        context.fillRect(0, 0, (int) width, (int) height);

        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);

        // Set transform to scale frame
        context.setTransform(transform);
        context.drawImage(frame, (int) (offsetX / scale), (int) (offsetY / scale), null);

        // Reset transform
        context.setTransform(canvasTransform);
    }
}
