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

package org.lecturestudio.core.service;

import com.github.javaffmpeg.*;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.media.video.desktop.*;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.codec.VideoEncoder;
import org.lecturestudio.core.codec.h264.H264StreamEncoder;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.io.VideoSink;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class ScreenCaptureService {

    public enum CaptureMode {
        WINDOW, SCREEN
    }

    private final WindowCapturer windowCapturer;
    private final ScreenCapturer screenCapturer;

    private final AtomicBoolean isRecording = new AtomicBoolean();

    private final int frameRate = 30;
    private int frameCounter = 0;

    private final ApplicationContext context;
    private final PeerConnectionFactory connectionFactory;

    private final String outputPath;

    private DesktopSource selectedSource;

    private final VideoEncoder videoEncoder;
    private final VideoSink videoSink;
    private Muxer muxer;

    @Inject
    public ScreenCaptureService(ApplicationContext context) {
        this.context = context;

        outputPath = context.getDataLocator().toAppDataPath("recording");

        // Create directory for temp frames
        File tempDir = new File(outputPath);
        tempDir.mkdirs();

        JavaFFmpeg.loadLibrary();

        File videoFile = new File(outputPath + File.separator + "screen-capture.mp4");
        if (!videoFile.exists()) {
            try {
                videoFile.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        initializeMuxer(videoFile.getAbsolutePath());

        VideoCodecConfiguration videoConfig = new VideoCodecConfiguration();
        videoConfig.setFrameRate(frameRate);
        videoConfig.setViewRect(new Rectangle2D(0, 0, 1920, 1080));

        videoEncoder = new H264StreamEncoder(videoConfig);
        videoSink = new ScreenCaptureVideoSink(outputPath + File.separator + "temp");

        // This needs to be called before the first initialization of any native webrtc call
        connectionFactory = new PeerConnectionFactory();
        windowCapturer = new WindowCapturer();
        screenCapturer = new ScreenCapturer();
    }

    private void initializeMuxer(String filePath) {
        System.out.println("Init Muxer: " + filePath);

        // Initialize muxer
        muxer = new Muxer(filePath);
        try {
            muxer.setVideoCodec(Codec.getEncoderById(CodecID.H264));
            muxer.setAudioCodec(Codec.getEncoderById(CodecID.MP3));
            muxer.setImageWidth(1280);
            muxer.setImageHeight(720);
            muxer.setPixelFormat(PixelFormat.YUV420P);
            muxer.setBitrate(2000000);
            muxer.setFramerate(frameRate);
            muxer.setAudioBitrate(128000);
            muxer.setSampleRate(22050);
            muxer.setAudioChannels(2);

            Options videoOptions = new Options();
            muxer.setVideoOptions(videoOptions);

            muxer.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<DesktopSource> getWindowSources() {
        return windowCapturer.getDesktopSources();
    }

    public List<DesktopSource> getScreenSources() {
        return screenCapturer.getDesktopSources();
    }

    public void setSelectedSource(DesktopSource source) {
        if (selectedSource != null) {
            // TODO: Stop recording with previous source
        }

        selectedSource = source;
    }

    public boolean isRecording() {
        return isRecording.get();
    }

    public void startWindowCapture(DesktopSource source) {
        startDesktopCapture(source, CaptureMode.WINDOW);
    }

    public void startScreenCapture(DesktopSource source) {
        startDesktopCapture(source, CaptureMode.SCREEN);
    }

    public void stopCapture() {
        isRecording.set(false);
    }

    public void captureWindowScreenshot(DesktopSource source, DesktopCaptureCallback callback) {
        captureDesktopScreenshot(source, CaptureMode.WINDOW, callback);
    }
    
    public void captureScreenScreenshot(DesktopSource source, DesktopCaptureCallback callback) {
        captureDesktopScreenshot(source, CaptureMode.SCREEN, callback);
    }

    public boolean startCapture() {
        if (selectedSource != null) {
            frameCounter = 0;
            windowCapturer.selectSource(selectedSource);

            windowCapturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    // TODO: Call callback to draw view, store frames etc
                    saveFrame(frame, frameCounter);
//                    try {
//                        addFrameToVideo(frame);
//                    } catch (IOException | JavaFFmpegException e) {
//                        e.printStackTrace();
//                    }
                    frameCounter++;
                }
            });

            isRecording.set(true);

            new Thread(() -> {
                long startTime = System.currentTimeMillis();

                System.out.println("Start Recording of source '" + selectedSource.title + "'");
                System.out.println("Save frames to: " + outputPath);
                while (isRecording.get()) {
                    windowCapturer.captureFrame();
                    try {
                        TimeUnit.MICROSECONDS.sleep(1000 / frameRate);
                    } catch (InterruptedException ignored) {
                        System.err.println("Execution of screen capture was interrupted");
                    }
                }

                long duration = System.currentTimeMillis() - startTime;
                System.out.println("Stopped Recording: " + frameCounter + " frames recorded in " + duration + "ms");
            }).start();
            return true;
        }
        return false;
    }

    private void startDesktopCapture(DesktopSource source, CaptureMode mode) {
        final DesktopCapturer capturer = (mode == CaptureMode.WINDOW) ? windowCapturer : screenCapturer;
    }
    
    private void captureDesktopScreenshot(DesktopSource source, CaptureMode mode, DesktopCaptureCallback callback) {
        final DesktopCapturer capturer = (mode == CaptureMode.WINDOW) ? windowCapturer : screenCapturer;
        capturer.selectSource(source);
        capturer.start(callback);
        capturer.captureFrame();
    }

    private void saveFrame(DesktopFrame frame, int frameCount) {
        String fileName = outputPath + File.separator + "temp" + File.separator + "frame" + frameCount + ".png";

        BufferedImage image = new BufferedImage(frame.frameSize.width, frame.frameSize.height, BufferedImage.TYPE_4BYTE_ABGR);
        videoSink.onVideoFrame(videoEncoder.encode(image));

//        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Path.of(fileName), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
//            channel.write(frame.buffer, 0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void addFrameToVideo(DesktopFrame frame) throws IOException, JavaFFmpegException {
        VideoFrame videoFrame = new VideoFrame(frame.buffer, frame.frameSize.width, frame.frameSize.height, PixelFormat.YUV420P);
        muxer.addImage(videoFrame);

        // System.out.println("Duration: " + videoEncoder.getTimestamp());

        //MediaPacket packet = videoEncoder.encodeVideo(videoFrame);
    }

//    private BufferedImage createViewImage(int width, int height) {
//        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
//        int[] nBits = { 8, 8, 8, 8 };
//        int[] bOffs = { 2, 1, 0, 3 }; // bgra
//        ColorModel colorModel = new ComponentColorModel(cs, nBits, true, false,
//                Transparency.TRANSLUCENT,
//                DataBuffer.TYPE_BYTE);
//
//        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
//                width, height,
//                width * 4, 4,
//                bOffs, null);
//
//        return new BufferedImage(colorModel, wr, false, null);
//    }


    private static class ScreenCaptureVideoSink implements VideoSink {

        private final String outputPath;
        private int frameCount = 0;

        public ScreenCaptureVideoSink(String outputPath) {
            File output = new File(outputPath);
            output.mkdirs();

            this.outputPath = outputPath;
        }

        @Override
        public void onVideoFrame(ByteBuffer data) {
            // Write frame to file
            try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(nextFramePath(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                channel.write(data, 0);
                frameCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Path nextFramePath() {
            return Path.of(outputPath, "frame" + frameCount + ".png");
        }
    }
}
