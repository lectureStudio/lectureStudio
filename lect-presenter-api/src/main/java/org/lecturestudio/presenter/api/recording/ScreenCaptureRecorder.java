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

package org.lecturestudio.presenter.api.recording;

import com.pngencoder.PngEncoder;
import com.pngencoder.PngEncoderBufferedImageConverter;
import dev.onvoid.webrtc.media.video.desktop.*;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.screencapture.ScreenCaptureFormat;
import org.lecturestudio.core.screencapture.ScreenCaptureOutputStream;
import org.lecturestudio.presenter.api.event.ScreenCaptureRecordingStateEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScreenCaptureRecorder extends ExecutableBase {

    private final Queue<BufferedImage> frameQueue;

    private DesktopCapturer capturer;

    private File backupFile;

    private ScreenCaptureOutputStream stream;
    private ScreenCaptureTask captureTask;
    private ScreenCaptureFormat format;

    private FrameProcessor processor;

    private boolean initialized = false;

    private int activeChannelId;

    public ScreenCaptureRecorder() {
        frameQueue = new ConcurrentLinkedQueue<>();
    }

    public void setBackupFile(File backupFile) throws IOException {
        this.backupFile = backupFile;
    }

    public ScreenCaptureOutputStream getStream() {
        return stream;
    }

    @Override
    protected void initInternal() {}

    @Override
    protected void startInternal() {
        if (!initialized) {
            System.out.println("Screen Capture is not initialized.");
            return;
        }

        try {
            stream = new ScreenCaptureOutputStream(backupFile);
            stream.setScreenCaptureFormat(format);
            stream.setActiveChannelId(activeChannelId);
            stream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }

        processor = new FrameProcessor();
        new Thread(processor).start();

        captureTask = new ScreenCaptureTask(format.getFrameRate());
        captureTask.start();
    }

    @Override
    protected void suspendInternal() {
        captureTask.stop();
        processor.stop(false);
    }

    @Override
    protected void stopInternal() {
        captureTask.stop();
        processor.stop(true);
    }

    @Override
    protected void destroyInternal() {}

    @Override
    protected void fireStateChanged() {
        // Notify about change is screen capture recording state
        ApplicationBus.post(new ScreenCaptureRecordingStateEvent(getState()));
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat format) {
        if (started() || suspended()) {
            throw new RuntimeException("Cannot update screen capture format, recording already started.");
        }
        this.format = format;
    }

    public void setActiveSource(DesktopSource source, DesktopSourceType type) {
        if (source != null) {
            initialized = false;

            // Initialize desktop capturer
            capturer = (type == DesktopSourceType.WINDOW) ? new WindowCapturer() : new ScreenCapturer();
            capturer.selectSource(source);
            capturer.start(this::onScreenCaptureFrame);

            activeChannelId = (int) source.id;

            initialized = true;
        }
    }

    private void onScreenCaptureFrame(DesktopCapturer.Result result, DesktopFrame frame) {
        // Add successfully captured frames to queue for further processing
        if (result == DesktopCapturer.Result.SUCCESS) {
            // Convert frame buffer to int array
            IntBuffer buffer = frame.buffer.asIntBuffer();
            int[] pixelBuffer = new int[buffer.remaining()];
            buffer.get(pixelBuffer);

            // Create buffered image from pixels
            BufferedImage image = PngEncoderBufferedImageConverter.createFromIntArgb(pixelBuffer, frame.frameSize.width, frame.frameSize.height);

            // Add buffered image to queue
            if (!frameQueue.offer(image)) {
                System.out.println("Failed to insert frame into queue.");
            }
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



    private class FrameProcessor implements Runnable {

        private boolean isRunning = false;
        private int frameCounter = 0;

        private boolean closeStream = false;

        @Override
        public void run() {
            isRunning = true;
            System.out.println("Started Frame Processor");

            // Make sure to process remaining frames in the queue even if the task was stopped.
            while (isRunning || !frameQueue.isEmpty()) {
                BufferedImage frame = frameQueue.poll();
                if (frame != null) {
                    processFrame(frame);
                }
            }

            if (closeStream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Stopped Frame Processor");
        }

        public void stop(boolean closeStream) {
            this.closeStream = closeStream;
            isRunning = false;
        }

        private void processFrame(BufferedImage frame) {
            // Compress frame and convert it to bytes array
            byte[] compressedImage = new PngEncoder().withBufferedImage(frame).toBytes();

            try {
                int bytesWritten = stream.writeFrameBytes(compressedImage);
                System.out.println("Frame " + frameCounter + ": Bytes Processed: " + bytesWritten + " Total: " + stream.getTotalBytesWritten());
                frameCounter++;
            } catch (IOException e) {
                System.err.println("Failed to write frame to screen capture stream.");
                e.printStackTrace();
            }
        }
    }
}
