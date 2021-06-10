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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

public class ScreenCaptureUtils {

    private static final WindowCapturer capturer = new WindowCapturer();

    public static void requestFrame(DesktopSource source, ScreenCaptureCallback callback) {
        if (source != null) {

            // TODO: Find a way to capture preview frames asynchronous

            capturer.selectSource(source);
            capturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    BufferedImage image = createBufferedImage(frame.frameSize.width, frame.frameSize.height);
                    DataBufferByte byteBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
                    frame.buffer.get(byteBuffer.getData());

                    callback.onFrameCapture(image);
                }
            });
            capturer.captureFrame();
        }
    }

    public static BufferedImage convertFrame(DesktopFrame frame, int width, int height) {

        // TODO: Implement scaling of frame to requested dimensions

        return createBufferedImage(width, height);
    }

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

    public interface ScreenCaptureCallback {

        void onFrameCapture(BufferedImage image);

    }
}
