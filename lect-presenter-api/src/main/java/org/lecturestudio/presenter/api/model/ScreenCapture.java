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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.util.ScreenCaptureUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

public class ScreenCapture extends Page {

    private final WindowCapturer capturer;
    private DesktopSource source;

    private BufferedImage lastFrame;

    public ScreenCapture(ScreenCaptureDocument document, int pageNumber) {
        super(document, pageNumber);
        capturer = new WindowCapturer();
        setRecordable(false);
    }

    public DesktopSource getSource() {
        return source;
    }

    public void setSource(DesktopSource source) {
        this.source = source;

    }

    public Long getId() {
        return source.id;
    }

    public String getTitle() {
        return source.title;
    }

    public BufferedImage getLastFrame() {
        return lastFrame;
    }

    public void requestFrame() {
        if (source != null) {
            capturer.selectSource(source);
            capturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
//                    byte[] buffer = new byte[frame.buffer.capacity()];
//                    frame.buffer.get(buffer);



                    lastFrame = ScreenCaptureUtils.createBufferedImage(frame.frameSize.width, frame.frameSize.height);

                    DataBuffer imageBuffer = lastFrame.getRaster().getDataBuffer();
                    DataBufferByte byteBuffer = (DataBufferByte) imageBuffer;

                    frame.buffer.get(byteBuffer.getData());

//                    System.out.println(buffer.length);

//                    try {
//                        lastFrame = ImageIO.read(new ByteArrayInputStream(buffer));
//                        System.out.println("IS null: " + lastFrame == )
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            });
            capturer.captureFrame();
        }
    }
}
