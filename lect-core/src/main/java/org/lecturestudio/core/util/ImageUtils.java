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

package org.lecturestudio.core.util;

import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageUtils {

    private final static ImageWriter writer;
    private final static ImageWriteParam writerParam;

    static {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("png");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found.");
        }
        writer = writers.next();

        writerParam = writer.getDefaultWriteParam();
        writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writerParam.setCompressionQuality(0.5f);
    }

    /**
     * Creates a buffered image of a given width and height filled with a given color.
     *
     * @param width
     * @param height
     * @param color
     * @return
     */
    public static BufferedImage createBufferedImage(int width, int height, Color color) {
        BufferedImage image = createBufferedImage(width, height);
        Graphics2D g = image.createGraphics();

        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();

        return image;
    }

    /**
     * Creates a buffered image with a given width and height.
     *
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createBufferedImage(int width, int height) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = { 8, 8, 8, 8 };
        int[] bOffs = { 2, 1, 0, 3 }; // bgra
        ColorModel colorModel = new ComponentColorModel(cs, nBits, true, false,
                Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                width, height,
                width * 4, 4,
                bOffs, null);

        return new BufferedImage(colorModel, wr, false, null);
    }

    public static BufferedImage convertDesktopFrame(DesktopFrame frame, int width, int height) {
        BufferedImage image = ImageUtils.createBufferedImage(width, height);
        DataBufferByte byteBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        frame.buffer.get(byteBuffer.getData());
        return image;
    }

    public static byte[] compress(byte[] frameBytes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(ImageIO.read(new ByteArrayInputStream(frameBytes)), null, null), writerParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    // See http://www.java2s.com/example/java-utility-method/bufferedimage-compress/compress-bufferedimage-image-float-quality-e5d99.html
    public static BufferedImage compress(BufferedImage image, float quality) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (quality >= 0) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);

            return ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage crop(BufferedImage src, int width, int height) {
        int x = src.getWidth() / 2 - width / 2;
        int y = src.getHeight()/2 - height/2;

        BufferedImage croppedImage = createBufferedImage(width, height);
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(src, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), x, y, x + croppedImage.getWidth(),
                y + croppedImage.getHeight(), null);
        g.dispose();

        return croppedImage;
    }

    public static BufferedImage scale(BufferedImage src, int width, int height) throws IOException {
        BufferedImage scaledImage = createBufferedImage(width, height);
        Graphics2D g = scaledImage.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(
                (double) width / src.getWidth(),
                (double) height / src.getHeight());
        g.drawRenderedImage(src, at);
        g.dispose();
        return scaledImage;
    }

    // See http://www.java2s.com/Code/Java/2D-Graphics-GUI/CropImage.htm for more information
    public static BufferedImage cropAndScale(BufferedImage src, int width, int height) throws IOException {

        float scale;
        if (src.getWidth() > src.getHeight()) {
            scale = (float) height / (float) src.getHeight();
            if (src.getWidth() * scale < width) {
                scale = (float) width / (float) src.getWidth();
            }
        } else {
            scale = (float) width / (float) src.getWidth();
            if (src.getHeight() * scale < height) {
                scale = (float) height / (float) src.getHeight();
            }
        }

        BufferedImage temp = scale(src, Float.valueOf(src.getWidth() * scale).intValue(),
                Float.valueOf(src.getHeight() * scale).intValue());

        temp = crop(temp, width, height);

        return temp;
    }
}
