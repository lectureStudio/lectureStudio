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

public class ScreenCaptureShapeRenderer extends BaseRenderer {

    @Override
    public Class<? extends Shape> forClass() {
        return ScreenCaptureShape.class;
    }

    @Override
    protected void renderPrivate(Shape shape, Graphics2D context) {
        ScreenCaptureShape screenCaptureShape = (ScreenCaptureShape) shape;

        AffineTransform prev = context.getTransform();

        context.setTransform(new AffineTransform());
        context.drawImage(screenCaptureShape.getFrame(), 0, 0, null);

        context.setTransform(prev);
    }
}
