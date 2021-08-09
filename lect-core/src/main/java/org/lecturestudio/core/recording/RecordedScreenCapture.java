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

import org.lecturestudio.core.screencapture.RandomAccessScreenCaptureStream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordedScreenCapture extends RecordedObjectBase {

    private final RandomAccessScreenCaptureStream screenCaptureStream;

    public RecordedScreenCapture(RandomAccessScreenCaptureStream screenCaptureStream) {
        this.screenCaptureStream = screenCaptureStream;
    }

    public RandomAccessScreenCaptureStream getScreenCaptureStream() {
        return screenCaptureStream;
    }

    public List<BufferedImage> getFrames() throws IOException {
        List<BufferedImage> frames = new ArrayList<>();

        byte[] bytes = screenCaptureStream.readAllBytes();
        System.out.println("Bytes in Stream: " + bytes.length);

        return frames;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return null;
    }

    @Override
    public void parseFrom(byte[] input) throws IOException {

    }
}
