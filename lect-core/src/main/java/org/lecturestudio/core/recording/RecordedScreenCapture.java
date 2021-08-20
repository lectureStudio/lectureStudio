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

package org.lecturestudio.core.recording;

import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.ScreenCaptureDataEvent;
import org.lecturestudio.core.screencapture.RandomAccessScreenCaptureStream;
import org.lecturestudio.core.screencapture.ScreenCaptureData;
import org.lecturestudio.core.screencapture.ScreenCaptureDataParser;

import java.io.IOException;

public class RecordedScreenCapture extends RecordedObjectBase {

    private RandomAccessScreenCaptureStream screenCaptureStream;
    private ScreenCaptureData screenCaptureData;

    public RecordedScreenCapture(RandomAccessScreenCaptureStream screenCaptureStream) {
        this.screenCaptureStream = screenCaptureStream;
    }

    public void setScreenCaptureStream(RandomAccessScreenCaptureStream stream) throws IOException {
        if (this.screenCaptureStream != null) {
            this.screenCaptureStream.close();
        }

        this.screenCaptureStream = stream;
    }

    public RandomAccessScreenCaptureStream getScreenCaptureStream() {
        return screenCaptureStream;
    }

    public void parseStream(ScreenCaptureDataParser.ProgressCallback callback) throws IOException {
        if (screenCaptureStream != null && screenCaptureStream.available() > 0) {
            // TODO: Perform parsing in async thread
            screenCaptureData = ScreenCaptureDataParser.parseStream(screenCaptureStream, callback);
            ApplicationBus.post(new ScreenCaptureDataEvent(screenCaptureData));
        }
    }

    public ScreenCaptureData getScreenCaptureData() {
        return screenCaptureData;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return null;
    }

    @Override
    public void parseFrom(byte[] input) throws IOException {}
}
