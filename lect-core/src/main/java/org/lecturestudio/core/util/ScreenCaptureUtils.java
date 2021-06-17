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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ScreenCaptureUtils {

    private static final WindowCapturer capturer = new WindowCapturer();

    public static void requestFrame(DesktopSource source, ScreenCaptureCallback callback) {
        if (source != null) {

            // TODO: Find a way to capture preview frames asynchronous

            capturer.selectSource(source);
            capturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    BufferedImage image = convertFrame(frame, frame.frameSize.width, frame.frameSize.height);
                    callback.onFrameCapture(image);
                }
            });
            capturer.captureFrame();
        }
    }

    public static BufferedImage convertFrame(DesktopFrame frame, int width, int height) {
        BufferedImage image = ImageUtils.createBufferedImage(width, height);
        DataBufferByte byteBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        frame.buffer.get(byteBuffer.getData());

        // TODO: Implement scaling of frame to requested dimensions

        return image;
    }

//    public boolean startCapture() {
//        if (selectedSource != null) {
//            frameCounter = 0;
//            windowCapturer.selectSource(selectedSource);
//
//            windowCapturer.start((result, frame) -> {
//                if (result == DesktopCapturer.Result.SUCCESS) {
//                    // TODO: Call callback to draw view, store frames etc
//                    saveFrame(frame, frameCounter);
////                    try {
////                        addFrameToVideo(frame);
////                    } catch (IOException | JavaFFmpegException e) {
////                        e.printStackTrace();
////                    }
//                    frameCounter++;
//                }
//            });
//
//            isRecording.set(true);
//
//            new Thread(() -> {
//                long startTime = System.currentTimeMillis();
//
//                System.out.println("Start Recording of source '" + selectedSource.title + "'");
//                System.out.println("Save frames to: " + outputPath);
//                while (isRecording.get()) {
//                    windowCapturer.captureFrame();
//                    try {
//                        TimeUnit.MICROSECONDS.sleep(1000 / frameRate);
//                    } catch (InterruptedException ignored) {
//                        System.err.println("Execution of screen capture was interrupted");
//                    }
//                }
//
//                long duration = System.currentTimeMillis() - startTime;
//                System.out.println("Stopped Recording: " + frameCounter + " frames recorded in " + duration + "ms");
//            }).start();
//            return true;
//        }
//        return false;
//    }

    public interface ScreenCaptureCallback {

        void onFrameCapture(BufferedImage image);

    }
}
