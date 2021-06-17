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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;
import org.lecturestudio.core.util.ImageUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class ScreenCaptureService {

    private final static int SOURCE_REFRESH_PERIOD = 3000;

    private final Map<Long, ScreenCapture> screenCaptures = new HashMap<>();

    private final List<DesktopSourceListListener> sourceListListeners = new CopyOnWriteArrayList<>();
    private List<DesktopSource> sources = new ArrayList<>();

    @Inject
    public ScreenCaptureService() {
        setupSourceRefreshTimer();
    }

    public void addScreenCaptureListener(DesktopSource source, ScreenCaptureListener listener) {
        ScreenCapture capture = screenCaptures.getOrDefault(source.id, new ScreenCapture(source));
        capture.addListener(listener);
        screenCaptures.put(source.id, capture);
    }

    public void removeScreenCaptureListener(DesktopSource source, ScreenCaptureListener listener) {
        ScreenCapture capture = screenCaptures.getOrDefault(source.id, null);
        if (capture != null) {
            capture.removeListener(listener);
        }
    }

    public CompletableFuture<Void> requestFrame(DesktopSource source) {
        ScreenCapture capture = screenCaptures.getOrDefault(source.id, null);
        if (capture == null) {
            throw new RuntimeException("No screen capture listener registered for source: " + source.title);
        }
        return CompletableFuture.runAsync(capture::requestFrame);
    }

    private void setupSourceRefreshTimer() {
        WindowCapturer capturer = new WindowCapturer();
        Timer sourceRefreshTimer = new Timer();
        sourceRefreshTimer.schedule((new TimerTask() {
            @Override
            public void run() {
                List<DesktopSource> newSources = capturer.getDesktopSources();
                if (newSources.size() != sources.size() || !newSources.containsAll(sources)) {
                    for (DesktopSourceListListener listener : sourceListListeners) {
                        listener.onDesktopSourceListChange(newSources);
                    }
                    sources = newSources;
                }
            }
        }), 0, SOURCE_REFRESH_PERIOD);
    }

    public List<DesktopSource> getDesktopSources() {
        return sources;
    }

    public void addSourceListListener(DesktopSourceListListener listener) {
        if (!sourceListListeners.contains(listener)) {
            sourceListListeners.add(listener);
        }
    }

    public void removeSourceListListener(DesktopSourceListListener listener) {
        sourceListListeners.remove(listener);
    }

    private static BufferedImage convertFrame(DesktopFrame frame, int width, int height) {
        BufferedImage image = ImageUtils.createBufferedImage(width, height);
        DataBufferByte byteBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        frame.buffer.get(byteBuffer.getData());

        // TODO: Implement scaling of frame to requested dimensions

        return image;
    }


    private static class ScreenCapture {

        private final DesktopSource source;
        private final WindowCapturer capturer;

        private final List<ScreenCaptureListener> listeners;

        private BufferedImage lastFrame;

        public ScreenCapture(DesktopSource source) {
            this.source = source;
            capturer = new WindowCapturer();
            listeners = new ArrayList<>();

            initialize();
        }

        public void requestFrame() {
            capturer.captureFrame();
        }

        public BufferedImage getLastFrame() {
            return lastFrame;
        }

        public void addListener(ScreenCaptureListener listener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        public void removeListener(ScreenCaptureListener listener) {
            listeners.remove(listener);
        }

        private void initialize() {
            capturer.selectSource(source);
            capturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    BufferedImage image = convertFrame(frame, frame.frameSize.width, frame.frameSize.height);

                    // Notify listeners
                    for (ScreenCaptureListener listener : listeners) {
                        listener.onFrameCapture(source, image);
                    }

                    lastFrame = image;
                }
            });
        }
    }

    public interface DesktopSourceListListener {
        void onDesktopSourceListChange(List<DesktopSource> sources);
    }

    public interface ScreenCaptureListener {
        void onFrameCapture(DesktopSource source, BufferedImage image);
    }
}
