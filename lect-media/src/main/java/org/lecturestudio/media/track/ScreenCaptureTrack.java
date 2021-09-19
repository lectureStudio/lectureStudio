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

import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.screencapture.ScreenCaptureData;

/**
 * This class implements a media track for a {@link ScreenCaptureData} instance.
 *
 * @author Maximilian Felix Ratzke
 */
public class ScreenCaptureTrack extends MediaTrackBase<ScreenCaptureData> {

    private ScreenCaptureData screenCaptureData;

    @Override
    public void setData(ScreenCaptureData data) {
        screenCaptureData = data;

        super.setData(data);
    }

    @Override
    public void dispose() {}

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
