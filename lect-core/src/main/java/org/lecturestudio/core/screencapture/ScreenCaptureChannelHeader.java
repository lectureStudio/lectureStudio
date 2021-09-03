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

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import org.lecturestudio.core.recording.RecordedObjectBase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ScreenCaptureChannelHeader extends RecordedObjectBase {

    /** Format marker 'CHAN' represented as integer value. */
    private final static int FORMAT_MARKER = 1128808782;

    /** The desktop source used to record the sequence */
    private DesktopSource source;

    /** The timestamp of the first frame in the sequence */
    private long startTime;
    /** The timestamp of the last frame in the sequence */
    private long endTime;

    public ScreenCaptureChannelHeader() {
        setStartTime(0);
        setEndTime(0);
    }

    public DesktopSource getSource() {
        return source;
    }

    public void setDesktopSource(DesktopSource source) {
        this.source = source;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getHeaderLength() {
        return 36 + source.title.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        byte[] sourceTitleBytes = source.title.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(getHeaderLength());
        buffer.putInt(FORMAT_MARKER);
        buffer.putLong(source.id);
        buffer.putLong(startTime);
        buffer.putLong(endTime);
        buffer.putInt(sourceTitleBytes.length);
        buffer.put(sourceTitleBytes);

        return buffer.array();
    }

    @Override
    public void parseFrom(byte[] input) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(input);

        int marker = buffer.getInt();
        if (marker != FORMAT_MARKER) {
            throw new IOException("Invalid screen capture channel header");
        }

        long sourceId = buffer.getLong();
        setStartTime(buffer.getLong());
        setEndTime(buffer.getLong());

        int sourceTitleLength = buffer.getInt();
        byte[] sourceTitleBytes = new byte[sourceTitleLength];
        buffer.get(sourceTitleBytes);
        String sourceTitle = new String(sourceTitleBytes, StandardCharsets.UTF_8);

        setDesktopSource(new DesktopSource(sourceTitle, sourceId));
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "]\n" +
                "Source Id: " + getSource().id + "\n" +
                "Source Title: " + getSource().title + "\n" +
                "Start Time: " + getStartTime() + "\n" +
                "End Time: " + getEndTime() + "\n";
    }
}
