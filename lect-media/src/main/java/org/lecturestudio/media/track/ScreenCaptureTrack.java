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

public class ScreenCaptureTrack extends MediaTrackBase<ScreenCaptureData> {

    private ScreenCaptureData screenCaptureData;

    @Override
    public void setData(ScreenCaptureData data) {
        screenCaptureData = data;

//        // Parse screen capture stream asynchronously
//        CompletableFuture.runAsync(() -> {
//            try {
//                ScreenCaptureFileReader.parseStream(data, new ScreenCaptureFileReader.ProgressCallback() {
//                    @Override
//                    public void onScreenCaptureData(ScreenCaptureData data) {
//                        screenCaptureData = data;
//                        ApplicationBus.post(new ScreenCaptureDataEvent(data));
//                    }
//
//                    @Override
//                    public void onFrame(BufferedImage frame, long frameTime, long sequenceKey) {
//                        ScreenCaptureSequence sequence = screenCaptureData.getSequences().get(sequenceKey);
//                        sequence.addFrame(frame, frameTime);
//                    }
//
//                    @Override
//                    public void onFrameProgress(float progress) {
//                        // TODO: Display progress indicator in editor
//
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

//    public void setData(ScreenCaptureData screenCaptureData) {
//        this.screenCaptureData = screenCaptureData;
//    }

    @Override
    public void dispose() {
//        try {
//            getData().close();
//        } catch (IOException e) {
//            LOG.error("Failed to close screen capture stream.", e);
//        }
//        super.dispose();
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
