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
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.core.screencapture.ScreenCaptureFileWriter;
import org.lecturestudio.core.screencapture.ScreenCaptureFormat;
import org.lecturestudio.presenter.api.event.ScreenCaptureRecordingStateEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class ScreenCaptureRecorder extends LectureRecorder {

    private final static Logger LOG = Logger.getLogger(ScreenCaptureRecorder.class.getName());

    private final Queue<CaptureEvent> eventQueue;

    private final File detailsFile;
    private final File framesFile;

    private DesktopCapturer capturer;

    private ScreenCaptureFileWriter writer;
    private ScreenCaptureTask captureTask;
    private ScreenCaptureFormat format;

    private CaptureEventProcessor processor;

    private boolean initialized = false;

    private long startTime = -1;
    private long pauseTime;
    private long haltDuration = 0;

    private long timeOffset = 0;

    public ScreenCaptureRecorder(File detailsFile, File framesFile) {
        this.detailsFile = detailsFile;
        this.framesFile = framesFile;

        eventQueue = new ConcurrentLinkedQueue<>();
    }

    private void initScreenCaptureWriter() {
        try {
            writer = new ScreenCaptureFileWriter(detailsFile, framesFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open screen capture files.");
        }
    }

    @Override
    protected void initInternal() {}

    @Override
    protected void startInternal() {
        if (!initialized) {
            LOG.warning("Recorder is not initialized yet.");
            return;
        }

        ExecutableState state = getPreviousState();
        if (state == ExecutableState.Initialized || state == ExecutableState.Stopped) {
            startTime = System.currentTimeMillis();

            // Create new writer
            initScreenCaptureWriter();

            try {
                writer.setScreenCaptureFormat(format);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (state == ExecutableState.Suspended) {
            haltDuration += System.currentTimeMillis() - pauseTime;
        }
        else {
            LOG.warning("Invalid state transition.");
            return;
        }

        // Start frame processor in separate thread
        processor = new CaptureEventProcessor();
        new Thread(processor).start();

        captureTask = new ScreenCaptureTask(format.getFrameRate());
        captureTask.start();

        // Reset pause time
        pauseTime = 0;
    }

    @Override
    protected void suspendInternal() {
        if (getPreviousState() == ExecutableState.Started) {
            pauseTime = System.currentTimeMillis();

            captureTask.stop();
            processor.stop(false);
        }
    }

    @Override
    protected void stopInternal() {
        captureTask.stop();
        processor.stop(true);

        startTime = -1;
        haltDuration = 0;
    }

    @Override
    protected void destroyInternal() {}

    @Override
    protected void fireStateChanged() {
        // Notify about change is screen capture recording state
        ApplicationBus.post(new ScreenCaptureRecordingStateEvent(getState()));
    }

    @Override
    public long getElapsedTime() {
        if (startTime == -1)
            return 0;

        if (started())
            return System.currentTimeMillis() - startTime - haltDuration;

        if (suspended())
            return pauseTime - startTime - haltDuration;

        return 0;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat format) {
        if (started() || suspended()) {
            throw new RuntimeException("Cannot update screen capture format, recording already started.");
        }
        this.format = format;
    }

    public void setActiveSource(DesktopSource source, DesktopSourceType type) {
        if (source != null) {
            // Initialize desktop capturer
            capturer = (type == DesktopSourceType.WINDOW) ? new WindowCapturer() : new ScreenCapturer();
            capturer.selectSource(source);
            capturer.start(this::onScreenCaptureFrame);

            SourceSwitchEvent event = new SourceSwitchEvent(source, type, getElapsedTime() + timeOffset);
            if (!eventQueue.offer(event)) {
                LOG.warning("Failed to add SourceSwitchEvent to recording.");
            }

            initialized = true;
        }
    }

    private void onScreenCaptureFrame(DesktopCapturer.Result result, DesktopFrame frame) {
        // Add successfully captured frames to queue for further processing
        if (result == DesktopCapturer.Result.SUCCESS && started()) {
            // Convert frame buffer to int array
            IntBuffer buffer = frame.buffer.asIntBuffer();
            int[] pixelBuffer = new int[buffer.remaining()];
            buffer.get(pixelBuffer);

            // Create buffered image from pixels
            BufferedImage image = PngEncoderBufferedImageConverter.createFromIntArgb(pixelBuffer, frame.frameSize.width, frame.frameSize.height);

            long elapsedTime = getElapsedTime();
            // Prevent frames with wrong timestamp at end of recording
            if (elapsedTime == 0) {
                return;
            }

            FrameCaptureEvent event = new FrameCaptureEvent(image, elapsedTime + timeOffset);

            // Add buffered image to queue
            if (!eventQueue.offer(event)) {
                LOG.warning("Failed to add FrameCaptureEvent to recording.");
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

    private static class CaptureEvent {

        private final long timestamp;

        public CaptureEvent(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private static class SourceSwitchEvent extends CaptureEvent {

        private final DesktopSource source;
        private final DesktopSourceType type;

        public SourceSwitchEvent(DesktopSource source, DesktopSourceType type, long timestamp) {
            super(timestamp);
            this.source = source;
            this.type = type;
        }

        public DesktopSource getSource() {
            return source;
        }

        public DesktopSourceType getType() {
            return type;
        }
    }

    private static class FrameCaptureEvent extends CaptureEvent {

        private final BufferedImage frame;

        public FrameCaptureEvent(BufferedImage frame, long timestamp) {
            super(timestamp);
            this.frame = frame;
        }

        public BufferedImage getFrame() {
            return frame;
        }
    }

    private class CaptureEventProcessor implements Runnable {

        private boolean isRunning = false;
        private int frameCounter = 0;

        private boolean closeStream = false;

        @Override
        public void run() {
            isRunning = true;
            System.out.println("Started Screen Capture Event Processor");

            // Make sure to process remaining frames in the queue even if the task was stopped.
            while (isRunning || !eventQueue.isEmpty()) {
                CaptureEvent event = eventQueue.poll();

                if (event instanceof SourceSwitchEvent) {
                    SourceSwitchEvent sourceSwitchEvent = (SourceSwitchEvent) event;
                    processSourceSwitch(sourceSwitchEvent.getSource());
                }
                else if (event instanceof FrameCaptureEvent) {
                    FrameCaptureEvent frameCaptureEvent = (FrameCaptureEvent) event;
                    processFrame(frameCaptureEvent.getFrame(), frameCaptureEvent.getTimestamp());
                }
            }

            if (closeStream) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Stopped Screen Capture Event Processor");
        }

        public void stop(boolean closeStream) {
            this.closeStream = closeStream;
            isRunning = false;
        }

        private void processFrame(BufferedImage frame, long timestamp) {
            // Compress frame and convert it to bytes array
            byte[] compressedImage = new PngEncoder().withBufferedImage(frame).toBytes();
            try {
                int bytesWritten = writer.writeFrameBytes(compressedImage, timestamp);
                System.out.println("Frame " + frameCounter + ": Bytes Processed: " + bytesWritten + " Total: " + writer.getTotalBytesWritten() + " Timestamp: " + timestamp);
                frameCounter++;
            } catch (IOException e) {
                System.err.println("Failed to write frame to screen capture stream.");
                e.printStackTrace();
            }
        }

        private void processSourceSwitch(DesktopSource source) {
            try {
                writer.setDesktopSource(source);
                System.out.println("Screen Capture Switch Source To: " + source.title);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
