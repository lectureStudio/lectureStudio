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

package org.lecturestudio.presenter.api.model;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.view.PresentationParameter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenCaptureDocumentRenderer implements DocumentRenderer {

    @Override
    public void render(Page page, PresentationParameter parameter, BufferedImage image) throws IOException {
        if (page instanceof ScreenCapture) {
            ScreenCapture capture = (ScreenCapture) page;
            capture.requestFrame();

            System.out.println("Render Screen Capture: " + capture.getTitle());

            Graphics2D g = image.createGraphics();

            // Draw last frame captured from the current ScreenCapture
            if (capture.getLastFrame() != null) {

                g.drawImage(capture.getLastFrame(), 0, 0, null);

//                DesktopFrame frame = capture.getLastFrame();
//
//                if (frame.buffer == null) {
//                    System.out.println("Frame Buffer Empty");
//                } else {
//                    int frameWidth = frame.frameSize.width;
//                    int frameHeight = frame.frameSize.height;
//
//                    if (frame.buffer.hasArray()) {
//                        byte[] bytes = frame.buffer.array();
//                        System.out.println("Frame Bytes: " + bytes.length);
//
//                        BufferedImage frameImage = ImageIO.read(new ByteArrayInputStream(bytes));
//
////                BufferedImage frameImage = createViewImage(frameWidth, frameHeight);
////
////                DataBuffer imageBuffer = frameImage.getRaster().getDataBuffer();
////                DataBufferByte byteBuffer = (DataBufferByte) imageBuffer;
////
////                frame.buffer.get(byteBuffer.getData());
//
//
//                    }


//                }
            } else {
                g.setColor(Color.red);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
            }

            g.dispose();
        }
    }
}
