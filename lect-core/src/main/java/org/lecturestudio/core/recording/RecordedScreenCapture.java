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
import org.lecturestudio.core.screencapture.ScreenCaptureFileReader;
import org.lecturestudio.core.screencapture.ScreenCaptureSequence;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RecordedScreenCapture extends RecordedObjectBase {

    private final RandomAccessScreenCaptureStream stream;

    private ScreenCaptureData screenCaptureData;

    public RecordedScreenCapture(RandomAccessScreenCaptureStream stream) {
        this.stream = stream;
        // parseStreamAsync(null);
    }

    public RandomAccessScreenCaptureStream getScreenCaptureStream() {
        return stream;
    }

    public void parseStreamAsync(ScreenCaptureFileReader.ProgressCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                ScreenCaptureFileReader.parseStream(stream, new ScreenCaptureFileReader.ProgressCallback() {
                    @Override
                    public void onScreenCaptureData(ScreenCaptureData data) {
                        screenCaptureData = data;
                        ApplicationBus.post(new ScreenCaptureDataEvent(data));

                        if (callback != null)
                            callback.onScreenCaptureData(data);
                    }

                    @Override
                    public void onFrame(BufferedImage frame, long frameTime, long sequenceKey) {
                        ScreenCaptureSequence sequence = screenCaptureData.getSequences().get(sequenceKey);
                        sequence.addFrame(frame, frameTime);

                        if (callback != null)
                            callback.onFrame(frame, frameTime, sequenceKey);
                    }

                    @Override
                    public void onFrameProgress(float progress) {
                        System.out.println("Frame Progress: " + progress);

                        if (callback != null)
                            callback.onFrameProgress(progress);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
