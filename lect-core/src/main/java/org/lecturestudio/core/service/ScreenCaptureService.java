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
import org.lecturestudio.core.util.ImageUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class provides methods to request screen capture frames as well as broadcasting events,
 * if new {@link DesktopSource DesktopSources} are available.
 *
 * @author Maximilian Felix Ratzke
 */
@Singleton
public class ScreenCaptureService {

    // Interval of the refresh timer to check for updates in the DesktopSources (in ms)
    private final static int SOURCE_REFRESH_INTERVAL = 3000;

    private final EventBus eventBus;

    private final WindowCapturer windowCapturer = new WindowCapturer();
    private final ScreenCapturer screenCapturer = new ScreenCapturer();

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

    /**
     * Returns a list of all available {@link DesktopSource DesktopSources} based on the given type.
     *
     * @param type The {@link DesktopSourceType} to get the sources for.
     * @return A list of all available {@link DesktopSource DesktopSources}
     */
    public List<DesktopSource> getDesktopSources(DesktopSourceType type) {
        return type == DesktopSourceType.WINDOW ? windowSources : screenSources;
    }

    /**
     * Performs a frame request for a given {@link DesktopSource} and {@link DesktopSourceType}.
     * The provided {@link ScreenCaptureCallback} will be called if the requested frame was captured.
     *
     * @param source The {@link DesktopSource} to capture the frame from.
     * @param type The {@link DesktopSourceType} of the {@link DesktopSource}
     * @param callback The {@link ScreenCaptureCallback} to notify about the capture.
     */
    public void requestFrame(DesktopSource source, DesktopSourceType type, ScreenCaptureCallback callback) {
        ScreenCapture capture = new ScreenCapture(source, type);
        capture.addListener(callback);
        capture.requestFrame();
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


    /**
     * This interface is used to notify listeners about the progress of a screen capture.
     */
    public interface ScreenCaptureCallback {

        /**
         * Provides the capture frame as well as the {@link DesktopSource} used for the capture.
         * Needs to be implemented by listener.
         *
         * @param source The {@link DesktopSource} used for the capture.
         * @param image The captured frame.
         */
        void onFrameCapture(DesktopSource source, BufferedImage image);
    }


    /**
     * This class is used as a wrapper to simplify the handling of screen captures.
     */
    public static class ScreenCapture {

        private final List<ScreenCaptureCallback> listeners = new ArrayList<>();

        private final DesktopSource source;
        private final DesktopCapturer capturer;

        /**
         * Creates a new instance of a {@link ScreenCapture}.
         *
         * @param source The {@link DesktopSource} to be used for screen captures.
         * @param type The {@link DesktopSourceType} of the {@link DesktopSource}.
         */
        public ScreenCapture(DesktopSource source, DesktopSourceType type) {
            this.source = source;

            if (type == DesktopSourceType.WINDOW) {
                capturer = new WindowCapturer();
            } else {
                capturer = new ScreenCapturer();
            }

            initialize();
        }

        /**
         * Requests a new frame from the underlying {@link DesktopCapturer}.
         */
        public void requestFrame() {
            capturer.captureFrame();
        }

        /**
         * Adds a {@link ScreenCaptureCallback} as listener to the {@link ScreenCapture}.
         *
         * @param listener The {@link ScreenCaptureCallback} used to notify about frame captures.
         */
        public void addListener(ScreenCaptureCallback listener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        /**
         * Removes an existing {@link ScreenCaptureCallback} from the {@link ScreenCapture}.
         *
         * @param listener The {@link ScreenCaptureCallback} to remove.
         */
        public void removeListener(ScreenCaptureCallback listener) {
            listeners.remove(listener);
        }

        private void notifyListeners(DesktopSource source, BufferedImage image) {
            for (ScreenCaptureCallback listener : listeners) {
                listener.onFrameCapture(source, image);
            }
        }

        private void initialize() {
            capturer.selectSource(source);
            capturer.start((result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    BufferedImage image = ImageUtils.convertDesktopFrame(frame, frame.frameSize.width, frame.frameSize.height);
                    notifyListeners(source, image);
                }
            });
        }
    }
}
