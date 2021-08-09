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

package org.lecturestudio.core.screencapture;

import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessStream;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RandomAccessScreenCaptureStream extends DynamicInputStream {

    private final DynamicInputStream inputStream;

    private ScreenCaptureFormat captureFormat;

    public RandomAccessScreenCaptureStream(File file) throws IOException {
        this(new RandomAccessStream(file));
    }

    public RandomAccessScreenCaptureStream(DynamicInputStream inputStream) {
        super(inputStream);

        this.inputStream = inputStream;
    }

    public ScreenCaptureFormat getCaptureFormat() {
        return captureFormat;
    }

    public void setCaptureFormat(ScreenCaptureFormat captureFormat) {
        this.captureFormat = captureFormat;
    }

    public void writeFrame(BufferedImage frame, int channelId) {
        // TODO: Finish implementing Screen Capture Access Stream
    }

    @Override
    public RandomAccessScreenCaptureStream clone() {
        RandomAccessScreenCaptureStream clone;

        try {
            clone = new RandomAccessScreenCaptureStream(inputStream.clone());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return clone;
    }
}
