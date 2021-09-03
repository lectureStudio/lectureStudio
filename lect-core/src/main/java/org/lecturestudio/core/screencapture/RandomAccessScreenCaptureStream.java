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

import java.io.*;

public class RandomAccessScreenCaptureStream extends BufferedInputStream {

    private ScreenCaptureFormat screenCaptureFormat;
    private final long length;

    public RandomAccessScreenCaptureStream(File detailsFile, File framesFile) throws FileNotFoundException {
        super(new SequenceInputStream(new FileInputStream(detailsFile), new FileInputStream(framesFile)));
        length = detailsFile.length() + framesFile.length();
    }

    public RandomAccessScreenCaptureStream(InputStream stream, long length) {
        super(stream);
        this.length = length;
    }

    public ScreenCaptureFormat getScreenCaptureFormat() {
        return screenCaptureFormat;
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat screenCaptureFormat) {
        this.screenCaptureFormat = screenCaptureFormat;
    }

    public long getLength() {
        return length;
    }
}
