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

package org.lecturestudio.core.recording.edit;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedScreenCapture;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.screencapture.ScreenCaptureData;

public class DeleteScreenCaptureAction extends RecordedObjectAction<RecordedScreenCapture> {

    private final Interval<Integer> cutInterval;

    public DeleteScreenCaptureAction(RecordedScreenCapture lectureObject, Interval<Integer> interval) {
        super(lectureObject);

        cutInterval = interval;
    }

    @Override
    public void undo() throws RecordingEditException {
        RecordedScreenCapture screenCapture = getRecordedObject();
        ScreenCaptureData data = screenCapture.getScreenCaptureData();

        if (data != null) {
            data.removeExclusion(cutInterval);
        }
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void execute() {
        RecordedScreenCapture screenCapture = getRecordedObject();
        ScreenCaptureData data = screenCapture.getScreenCaptureData();

        if (data != null) {
            data.addExclusion(cutInterval);
        }
    }
}
