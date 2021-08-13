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

package org.lecturestudio.media.track;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.screencapture.RandomAccessScreenCaptureStream;
import org.lecturestudio.core.screencapture.ScreenCaptureData;

import java.io.IOException;

public class ScreenCaptureTrack extends MediaTrackBase<RandomAccessScreenCaptureStream> {

    private static final Logger LOG = LogManager.getLogger(AudioTrack.class);

    private ScreenCaptureData screenCaptureData;

    public void setData(ScreenCaptureData screenCaptureData) {
        this.screenCaptureData = screenCaptureData;
    }

    @Override
    public void dispose() {
        try {
            getData().close();
        } catch (IOException e) {
            LOG.error("Failed to close screen capture stream.", e);
        }
        super.dispose();
    }

    @Override
    public void recordingChanged(RecordingChangeEvent event) {
        switch (event.getContentType()) {
            case ALL:
            case SCREEN_CAPTURE:
                dispose();
                setData(event.getRecording().getRecordedScreenCapture().getScreenCaptureData());
                break;
        }
    }

    public ScreenCaptureData getScreenCaptureData() {
        return screenCaptureData;
    }
}
