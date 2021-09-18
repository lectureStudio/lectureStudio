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

package org.lecturestudio.media.playback;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.screencapture.ScreenCaptureData;
import org.lecturestudio.core.screencapture.ScreenCaptureSequence;
import org.lecturestudio.media.event.ScreenCaptureFrameEvent;
import org.lecturestudio.media.event.ScreenCaptureSequenceEndEvent;

import java.awt.image.BufferedImage;

/**
 * This class handles the playback of a {@link ScreenCaptureData} instance.
 */
public class ScreenCapturePlayer extends ExecutableBase {

    // Allow for up to 60 fps
    private final static int UPDATE_INTERVAL_IN_MS = 16;

    private final SyncState syncState;
    private final Object suspendLock = new Object();

    private ScreenCaptureData data;
    private ScreenCaptureSequence previousSequence;

    private long lastUpdate;

    public ScreenCapturePlayer(SyncState syncState) {
        this.syncState = syncState;
    }

    public long getElapsedTime() {
        return syncState.getAudioTime();
    }

    public void setData(ScreenCaptureData data) {
        this.data = data;
    }

    public void seek(long seekTime) {
        if (data == null)
            return;

        ScreenCaptureSequence sequence  = data.seekSequence(seekTime);

        // Search for frame if sequence was found
        if (sequence != null) {
            BufferedImage frame = sequence.seekFrame(seekTime);
            if (frame != null) {
                ApplicationBus.post(new ScreenCaptureFrameEvent(frame));
                previousSequence = sequence;
            } else {
                notifySequenceEnd();
            }
        }
        // Notify about end of sequence if previous sequence was present but
        else if (previousSequence != null) {
           notifySequenceEnd();
        }
    }

    private void notifySequenceEnd() {
        ApplicationBus.post(new ScreenCaptureSequenceEndEvent(previousSequence));
        previousSequence = null;
    }

    @Override
    protected void initInternal() throws ExecutableException {

    }

    @Override
    protected void startInternal() throws ExecutableException {
        // Notify suspend lock if reader task is already running
        if (getPreviousState() == ExecutableState.Suspended) {
            synchronized (suspendLock) {
                lastUpdate = 0;
                suspendLock.notifyAll();
            }
        }
        // Create a new thread with reader task
        else {
            Thread thread = new Thread(new ScreenCaptureReaderTask());
            thread.start();
        }
    }

    @Override
    protected void stopInternal() throws ExecutableException {}

    @Override
    protected void destroyInternal() throws ExecutableException {}

    private class ScreenCaptureReaderTask implements Runnable {

        @Override
        public void run() {
            long elapsedTime;
            lastUpdate = 0;

            while (true) {
                elapsedTime = getElapsedTime();

                // Restrict loop to update interval to reduce CPU usage
                if (data == null || elapsedTime < lastUpdate + UPDATE_INTERVAL_IN_MS) {
                    continue;
                }

                if (started()) {
                    seek(elapsedTime);
                }
                else if (suspended()) {
                    try {
                        synchronized (suspendLock) {
                            suspendLock.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if (stopped()) {
                    return;
                }

                // Fetch elapsed time again to prevent wrong time if seek occurred during suspend time
                lastUpdate = getElapsedTime();
            }
        }
    }
}
