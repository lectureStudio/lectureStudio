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

/**
 * This class is used as wrapper around the screen capture stream.
 *
 * @author Maximilian Felix Ratzke
 */
public class RandomAccessScreenCaptureStream extends BufferedInputStream {

    private final long length;

    /**
     * Creates a {@link RandomAccessScreenCaptureStream} from a details and frames file
     *
     * @param detailsFile The file which contains the metadata of a screen capture.
     * @param framesFile The file which contains the frame data of a screen capture.
     */
    public RandomAccessScreenCaptureStream(File detailsFile, File framesFile) throws FileNotFoundException {
        super(new SequenceInputStream(new FileInputStream(detailsFile), new FileInputStream(framesFile)));
        length = detailsFile.length() + framesFile.length();
    }

    /**
     * Creates a {@link RandomAccessScreenCaptureStream} from an input stream.
     *
     * @param stream The input stream to get the data from
     * @param length The length of the stream
     */
    public RandomAccessScreenCaptureStream(InputStream stream, long length) {
        super(stream);
        this.length = length;
    }

    /**
     * Get the length of the stream.
     */
    public long getLength() {
        return length;
    }
}
