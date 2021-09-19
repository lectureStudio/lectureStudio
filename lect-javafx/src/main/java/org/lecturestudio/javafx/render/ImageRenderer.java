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

package org.lecturestudio.javafx.render;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.lecturestudio.core.model.shape.ImageShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;

/**
 * This class implements a renderer for {@link ImageShape} instances.
 *
 * @author Maximilian Felix Ratzke
 */
public class ImageRenderer implements Renderer<GraphicsContext> {

    @Override
    public Class<? extends Shape> forClass() {
        return ImageShape.class;
    }

    @Override
    public void render(Shape shape, GraphicsContext context) throws Exception {
        ImageShape imageShape = (ImageShape) shape;
        Image image = SwingFXUtils.toFXImage(imageShape.getImage(), null);
        context.drawImage(image, 0, 0);
    }
}
