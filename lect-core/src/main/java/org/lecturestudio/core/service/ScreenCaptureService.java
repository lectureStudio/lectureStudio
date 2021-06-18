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

import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.media.video.desktop.*;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.ScreenCaptureSourceEvent;
import org.lecturestudio.core.screencapture.ScreenCaptureFormat;
import org.lecturestudio.core.util.ImageUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.*;

@Singleton
public class ScreenCaptureService {

    // Interval of the refresh timer to check for updates in the DesktopSources (in ms)
    private final static int SOURCE_REFRESH_INTERVAL = 3000;

    private final Map<Long, ScreenCapture> screenCaptures = new HashMap<>();
    private final EventBus eventBus;

    private final WindowCapturer windowCapturer = new WindowCapturer();
    private final ScreenCapturer screenCapturer = new ScreenCapturer();

    private final List<ScreenCaptureCallback> listeners = new ArrayList<>();

    private List<DesktopSource> windowSources;
    private List<DesktopSource> screenSources;

    static {
        // Make sure to load native library
        new PeerConnectionFactory();
    }

    @Inject
    public ScreenCaptureService(ApplicationContext context) {
        eventBus = context.getEventBus();
        windowSources = windowCapturer.getDesktopSources();
        screenSources = screenCapturer.getDesktopSources();
        setupSourceRefreshTimer();
    }

    public List<DesktopSource> getDesktopSources(DesktopSourceType type) {
        return type == DesktopSourceType.WINDOW ? windowSources : screenSources;
    }

    public void startCapture(DesktopSource source, DesktopSourceType type, ScreenCaptureFormat format) {
        ScreenCapture capture = screenCaptures.getOrDefault(source.id, new ScreenCapture(source, type));
        capture.startCapture(format);
        screenCaptures.put(source.id, capture);
    }

    public void stopCapture(DesktopSource source) {
        ScreenCapture capture = screenCaptures.getOrDefault(source.id, null);
        if (capture != null) {
            capture.stopCapture();
        }
    }



    public void addScreenCaptureListener(ScreenCaptureCallback listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

//        ScreenCapture capture = screenCaptures.getOrDefault(source.id, new ScreenCapture(source, type));
//        capture.addListener(listener);
//        screenCaptures.put(source.id, capture);
    }

    public void removeScreenCaptureListener(ScreenCaptureCallback listener) {
        listeners.remove(listener);

//        ScreenCapture capture = screenCaptures.getOrDefault(source.id, null);
//        if (capture != null) {
//            capture.removeListener(listener);
//        }
//        screenCaptures.put(source.id, capture);
    }

    public void requestFrame(DesktopSource source, DesktopSourceType type) {
        ScreenCapture capture = screenCaptures.getOrDefault(source.id, new ScreenCapture(source, type));
        capture.requestFrame();
        screenCaptures.put(source.id, capture);
    }

    public void requestFrame(DesktopSource source, DesktopSourceType type, ScreenCaptureCallback callback) {
        DesktopCapturer capturer = (type == DesktopSourceType.WINDOW) ? new WindowCapturer() : new ScreenCapturer();
        capturer.selectSource(source);
        capturer.start((result, frame) -> {
            if (result == DesktopCapturer.Result.SUCCESS) {
                BufferedImage image = ImageUtils.convertDesktopFrame(frame, frame.frameSize.width, frame.frameSize.height);
                callback.onFrameCapture(source, image);
            }
        });
        capturer.captureFrame();
    }

    private void setupSourceRefreshTimer() {
        Timer sourceRefreshTimer = new Timer();
        sourceRefreshTimer.schedule((new TimerTask() {
            @Override
            public void run() {
                // Post events for updated window sources
                List<DesktopSource> newWindowSources = windowCapturer.getDesktopSources();
                if (newWindowSources.size() != windowSources.size() || !newWindowSources.containsAll(windowSources)) {
                    eventBus.post(new ScreenCaptureSourceEvent(newWindowSources, DesktopSourceType.WINDOW));
                    windowSources = newWindowSources;
                }

                // Post events for updated screen sources
                List<DesktopSource> newScreenSources = screenCapturer.getDesktopSources();
                if (newScreenSources.size() != screenSources.size() || !newScreenSources.containsAll(screenSources)) {
                    eventBus.post(new ScreenCaptureSourceEvent(newScreenSources, DesktopSourceType.SCREEN));
                    screenSources = newScreenSources;
                }
            }
        }), 0, SOURCE_REFRESH_INTERVAL);
    }

    private void notifyListeners(DesktopSource source, BufferedImage image) {
        for (ScreenCaptureCallback listener : listeners) {
            listener.onFrameCapture(source, image);
        }
    }



    public interface ScreenCaptureCallback {
        void onFrameCapture(DesktopSource source, BufferedImage image);
    }



    private class ScreenCapture {

        private final DesktopSource source;
        private final DesktopCapturer capturer;

        private BufferedImage lastFrame;
        private Timer timer;

        private long lastFrameUpdate;
        private boolean isCapturing;

        public ScreenCapture(DesktopSource source, DesktopSourceType type) {
            this.source = source;

            if (type == DesktopSourceType.WINDOW) {
                capturer = new WindowCapturer();
            } else {
                capturer = new ScreenCapturer();
            }

            initialize();
        }

        public void requestFrame() {
            capturer.captureFrame();
        }

        public BufferedImage getLastFrame() {
            return lastFrame;
        }

        public void startCapture(ScreenCaptureFormat format) {
            if (!isCapturing) {
                int frameDuration = 1000 / format.getFrameRate();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        capturer.captureFrame();
                    }
                }, 0, frameDuration);
                isCapturing = true;
            }
        }

        public void stopCapture() {
            if (isCapturing) {
                timer.cancel();
            }
        }

        public void addListener(ScreenCaptureCallback listener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        public void removeListener(ScreenCaptureCallback listener) {
            listeners.remove(listener);
        }

        private void initialize() {
            capturer.selectSource(source);
            capturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    BufferedImage image = ImageUtils.convertDesktopFrame(frame, frame.frameSize.width, frame.frameSize.height);
                    notifyListeners(source, image);

                    lastFrame = image;
                    lastFrameUpdate = System.currentTimeMillis();
                }
            });
        }
    }
}
