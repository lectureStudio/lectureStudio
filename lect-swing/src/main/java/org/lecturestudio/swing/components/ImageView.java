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

package org.lecturestudio.swing.components;

import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;
import org.lecturestudio.core.util.ScreenCaptureUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ImageView extends JComponent {

    static {
        try {
            nu.pattern.OpenCV.loadLocally();
        }
        catch (Exception e) {
            // Should not land here.
        }
    }

    private BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void drawFrame(DesktopFrame frame) {
        int width = frame.frameSize.width;
        int height = frame.frameSize.height;

        int viewWidth = getPreferredSize().width;
        int viewHeight = getPreferredSize().height;

        // Copy frame byte buffer into a buffered image.
        BufferedImage frameImage = ScreenCaptureUtils.createBufferedImage(width, height);

        DataBuffer imageBuffer = frameImage.getRaster().getDataBuffer();
        DataBufferByte byteBuffer = (DataBufferByte) imageBuffer;

        frame.buffer.get(byteBuffer.getData());

        // Scale frame image to the size of the image view.
        Rectangle rect = getScaledBounds(width, height, viewWidth,
                viewHeight);

        if (isNull(image) || image.getWidth() != rect.width || image.getHeight() != rect.height) {
            image = ScreenCaptureUtils.createBufferedImage(rect.width, rect.height);
        }

        scaleOpenCv(frameImage, rect.width, rect.height);
    }

    private void scaleOpenCv(BufferedImage src, int w, int h) {
        byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        byte[] pixels = ((DataBufferByte) src.getRaster().getDataBuffer()).getData();

        Mat srcMat = new Mat(src.getHeight(), src.getWidth(), CvType.CV_8UC4);
        srcMat.put(0, 0, pixels);

        Mat dstMat = new Mat(h, w, CvType.CV_8UC4);
        Size sz = new Size(w, h);

        Imgproc.resize(srcMat, dstMat, sz, 0, 0, Imgproc.INTER_AREA);

        dstMat.get(0, 0, targetPixels);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (nonNull(image)) {
            g.drawImage(image, 0, 0, this);
        }
    }

    private Rectangle getScaledBounds(int imageWidth,
                                             int imageHeight, int viewWidth, int viewHeight) {
        int width = imageWidth;
        int height = imageHeight;

        if (imageWidth > viewWidth) {
            width = viewWidth;
            height = (width * imageHeight) / imageWidth;
        }
        if (height > viewHeight) {
            height = viewHeight;
            width = (height * imageWidth) / imageHeight;
        }

        int x = (viewWidth - width) / 2;
        int y = (viewHeight - height) / 2;

        return new Rectangle(x, y, width, height);
    }
}
