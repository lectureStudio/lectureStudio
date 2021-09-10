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

        AffineTransform canvasTransform = context.getTransform();
        int canvasWidth = (int) canvasTransform.getScaleX();
        int canvasHeight = (int) canvasTransform.getScaleY();

        double scaleX = canvasWidth / (float) frame.getWidth();
        double scaleY = canvasHeight / (float) frame.getHeight();

        // Make sure to scale with same ratio
        double scale = Math.min(scaleX, scaleY);

        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);

        int offsetX = 0;
        int offsetY = 0;

        if (scaleX != scale) {
            offsetX = (int) ((canvasWidth - frame.getWidth() * scale) / 2f);
        }

        if (scaleY != scale) {
            offsetY = (int) ((canvasHeight - frame.getHeight() * scale) / 2f);
        }

        context.setColor(Color.BLACK);
        context.fillRect(0, 0, canvasWidth, canvasHeight);

        // Set transform to scale frame
        context.setTransform(transform);
        context.drawImage(frame, offsetX, offsetY, null);

        // Reset transform
        context.setTransform(canvasTransform);
    }
}
