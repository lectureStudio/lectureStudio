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

import org.lecturestudio.core.recording.RecordedObjectBase;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenCaptureFormatHeader extends RecordedObjectBase {

    /** Format marker 'FORM' represented as integer value. */
    private final static int FORMAT_MARKER = 1179603533;

    private ScreenCaptureFormat format;

    private int dataOffset;

    public ScreenCaptureFormat getFormat() {
        return format;
    }

    public void setFormat(ScreenCaptureFormat format) {
        this.format = format;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public int getHeaderLength() {
        return 4;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(getHeaderLength());
        buffer.putInt(FORMAT_MARKER);
        buffer.putInt(format.getFrameRate());
        buffer.putInt(dataOffset);

        return buffer.array();
    }

    @Override
    public void parseFrom(byte[] input) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(input);

        int marker = buffer.getInt();
        if (marker != FORMAT_MARKER) {
            throw new IOException("Invalid screen capture format header");
        }

        int frameRate = buffer.getInt();
        setFormat(new ScreenCaptureFormat(frameRate));
        setDataOffset(buffer.getInt());
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "]\n" +
                "Frame Rate: " + format.getFrameRate() + "\n";
    }
}
