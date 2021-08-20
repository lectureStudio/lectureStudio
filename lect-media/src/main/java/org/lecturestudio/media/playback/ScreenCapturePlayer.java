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

public class ScreenCapturePlayer extends ExecutableBase {

    private final static int UPDATE_INTERVAL_IN_MS = 1;

    private final SyncState syncState;

    private ScreenCaptureData data;
    private Thread thread;

    private ScreenCaptureSequence sequence;

    public ScreenCapturePlayer(SyncState syncState) {
        this.syncState = syncState;
    }

    public long getElapsedTime() {
        return syncState.getAudioTime();
    }

    public void setData(ScreenCaptureData data) {
        this.data = data;
    }

    public void seek(int seekTime) {
        if (data == null)
            return;

        // Try to find sequence at given time
        if (sequence == null) {
            sequence = data.seekSequence(seekTime);
        }

        if (sequence != null) {
            // Try to find frame if sequence is active
            if (seekTime < sequence.getEndTime()) {
                BufferedImage frame = sequence.seekFrame(seekTime);
                if (frame != null) {
                    ApplicationBus.post(new ScreenCaptureFrameEvent(frame));
                }
            }
            else {
                ApplicationBus.post(new ScreenCaptureSequenceEndEvent(sequence));
                sequence = null;
            }
        }
    }

    @Override
    protected void initInternal() throws ExecutableException {

    }

    @Override
    protected void startInternal() throws ExecutableException {
        if (getPreviousState() == ExecutableState.Suspended) {
            synchronized (thread) {
                thread.notify();
            }
        }
        else {
            thread = new Thread(new ScreenCaptureReaderTask());
            thread.start();
        }
    }

    @Override
    protected void stopInternal() throws ExecutableException {

    }

    @Override
    protected void destroyInternal() throws ExecutableException {

    }

    private class ScreenCaptureReaderTask implements Runnable {

        @Override
        public void run() {
            ExecutableState state;

            long elapsedTime;
            long lastUpdate = 0;

            while (true) {
                state = getState();
                elapsedTime = getElapsedTime();

                // Restrict loop to update interval to reduce CPU usage
                if (data == null || elapsedTime < lastUpdate + UPDATE_INTERVAL_IN_MS) {
                    continue;
                }

                if (state == ExecutableState.Started) {
                    // Seek sequence if not currently set
                    if (sequence == null) {
                        sequence = data.seekSequence(elapsedTime);
                    }
                    else if (elapsedTime < sequence.getEndTime()) {
                        BufferedImage frame = sequence.seekFrame(elapsedTime);
                        if (frame != null) {
                            ApplicationBus.post(new ScreenCaptureFrameEvent(frame));
                        }
                    }
                    // Sequence finished, reset to null
                    else {
                        ApplicationBus.post(new ScreenCaptureSequenceEndEvent(sequence));
                        sequence = null;
                    }
                }
                else if (state == ExecutableState.Suspended) {
                    synchronized (thread) {
                        try {
                            thread.wait();
                        }
                        catch (Exception ignored) {}
                    }
                }
                else if (state == ExecutableState.Stopped) {
                    return;
                }

                lastUpdate = elapsedTime;
            }
        }
    }
}
