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

package org.lecturestudio.core.screencapture;

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.util.ScreenCaptureUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenCaptureRecorder {

    private final WindowCapturer capturer = new WindowCapturer();
    private final ScreenCaptureOutputStream stream;

    private ScreenCaptureTask captureTask;
    private ScreenCaptureFormat format;

    private ExecutableState state = ExecutableState.Stopped;
    private boolean initialized = false;

    public ScreenCaptureRecorder(File outputFile) throws IOException {
        stream = new ScreenCaptureOutputStream(outputFile);
        capturer.start(this::onScreenCaptureFrame);
    }

    public ScreenCaptureOutputStream getStream() {
        return stream;
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat format) {
        if (state != ExecutableState.Stopped) {
            throw new RuntimeException("Cannot update screen capture format, recording already started.");
        }
        this.format = format;
        captureTask = new ScreenCaptureTask(format.getFrameRate());
    }

    public void start() throws IOException {
        if (state == ExecutableState.Started || !initialized) {
            return;
        }

        System.out.println("Start recording of screen capture");

        stream.setScreenCaptureFormat(format);
        stream.reset();
        captureTask.start();

        state = ExecutableState.Started;
    }

    public void pause() {
        if (state != ExecutableState.Started) {
            return;
        }

        captureTask.stop();
        state = ExecutableState.Suspended;
    }

    public void stop() throws IOException {
        if (state == ExecutableState.Stopped) {
            return;
        }

        // Closes stream and writes remaining bytes to disk
        stream.close();

        captureTask.stop();
        state = ExecutableState.Stopped;
    }

    public void setActiveSource(DesktopSource source) {
        if (source != null) {
            capturer.selectSource(source);
            stream.setActiveChannelId((int) source.id);
            initialized = true;
        }
    }

    private void onScreenCaptureFrame(DesktopCapturer.Result result, DesktopFrame frame) {
        BufferedImage image = ScreenCaptureUtils.convertFrame(frame, frame.frameSize.width, frame.frameSize.height);
        try {
            int bytesWritten = stream.writeFrame(image);
            System.out.println("Bytes Captured: " + bytesWritten + " Total: " + stream.getBytesWritten());
        } catch (IOException e) {
            System.err.println("Failed to write frame to screen capture stream.");
            e.printStackTrace();
        }
    }

    private class ScreenCaptureTask extends Timer {

        private TimerTask task;
        private final int period;

        public ScreenCaptureTask(int frameRate) {
            period = 1000 / frameRate;
        }

        void start() {
            task = new TimerTask() {
                @Override
                public void run() {
                    capturer.captureFrame();
                }
            };
            scheduleAtFixedRate(task, 0, period);
        }

        public void stop() {
            cancel();
            purge();

            task = null;
        }

    }
}
