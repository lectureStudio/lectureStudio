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

package org.lecturestudio.core.model.shape;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Shape to draw buffered images.
 *
 * @author Maximilian Felix Ratzke
 */
public class ImageShape extends Shape {

    private BufferedImage image;

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public Shape clone() {
        ImageShape shape = new ImageShape();
        BufferedImage clonedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        Graphics2D g = clonedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        shape.setImage(clonedImage);
        return shape;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        DataBufferByte byteBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        byte[] imageData = byteBuffer.getData();
        int imageDataLength = imageData.length;

        int bufferLength = 16 + imageDataLength;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);

        // Set header: 4 * 4 bytes
        buffer.putInt(image.getWidth());
        buffer.putInt(image.getHeight());
        buffer.putInt(image.getType());
        buffer.putInt(imageDataLength);

        // Set image data
        buffer.put(byteBuffer.getData());

        return buffer.array();
    }

    @Override
    protected void parseFrom(byte[] input) throws IOException {
        ByteBuffer buffer = createBuffer(input);

        // Get header
        int imageWidth = buffer.getInt();
        int imageHeight = buffer.getInt();
        int imageType = buffer.getInt();
        int imageDataLength = buffer.getInt();

        // Get image data
        byte[] imageData = new byte[imageDataLength];
        buffer.get(imageData);

        // Create image and populate raster
        image = new BufferedImage(imageWidth, imageHeight, imageType);
        image.getRaster().setDataElements(0, 0, imageWidth, imageHeight, imageData);
    }
}
