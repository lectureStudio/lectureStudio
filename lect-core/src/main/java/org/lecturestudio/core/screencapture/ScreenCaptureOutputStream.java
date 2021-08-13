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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class ScreenCaptureOutputStream {

    // Make sure to have a fixed number of characters for each header which matches the respective header size
    public static final String FORMAT_HEADER = "FORM";
    public static final String CHANNEL_HEADER = "CHAN";


    // Byte Size: 4 * 2 bytes per header + 3 * 4 bytes per integer = 2 * 8 + 12 = 28 bytes
    private static final int FORMAT_HEADER_SIZE = 8;

    private final SeekableByteChannel channel;

    private ScreenCaptureFormat recordingFormat;
    private DesktopSource source;

    private long totalBytesWritten = 0;

    public ScreenCaptureOutputStream(File outputFile) throws IOException {
        this(FileChannel.open(outputFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE));
    }

    public ScreenCaptureOutputStream(SeekableByteChannel channel) throws IOException {
        this.channel = channel;
        reset();
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat format) throws IOException {
        this.recordingFormat = format;

        // Update written header to match format
        writeHeader();
        reset();
    }

    public void setDesktopSource(DesktopSource source) throws IOException {
        this.source = source;

        writeChannelHeader();
    }

    public long getTotalBytesWritten() {
        return totalBytesWritten;
    }

    public int writeFrameBytes(byte[] frameBytes, long timestamp) throws IOException {
        int bytesWritten = 0;
        if (channel.isOpen()) {
            // Add offset + image data to byte buffer
            ByteBuffer buffer = ByteBuffer.allocate(frameBytes.length + 12);
            buffer.putInt(frameBytes.length);
            buffer.putLong(timestamp);
            buffer.put(frameBytes);
            buffer.flip();

            // Repeat until buffer is completely written to the channel
            while (buffer.hasRemaining()) {
                bytesWritten += channel.write(buffer);
            }
        }

        totalBytesWritten += bytesWritten;
        return bytesWritten;
    }

    public void reset() throws IOException {
        // Prevent header from being overwritten
        channel.position(FORMAT_HEADER_SIZE);
    }

    public void close() throws IOException {
        writeHeader();

        channel.close();
    }

    /**
     * Inserts a channel header chunk into the output stream to indicate a channel switch.
     */
    private void writeChannelHeader() throws IOException {
        byte[] sourceTitleBytes = source.title.getBytes(StandardCharsets.UTF_8);

        ByteBuffer header = ByteBuffer.allocate(16 + sourceTitleBytes.length);
        header.put(CHANNEL_HEADER.getBytes(StandardCharsets.UTF_8));
        header.putLong(source.id);
        header.putInt(sourceTitleBytes.length);
        header.put(sourceTitleBytes);
        header.flip();

        channel.write(header);
    }

    /**
     * Writes the header containing the ScreenCaptureFormat to the channel.
     */
    private void writeHeader() throws IOException {
        int frameRate = this.recordingFormat.getFrameRate();

        ByteBuffer header = ByteBuffer.allocate(FORMAT_HEADER_SIZE);
        header.put(FORMAT_HEADER.getBytes(StandardCharsets.UTF_8));
        header.putInt(frameRate);
        header.flip();

        // Write header at start of file
        long position = channel.position();
        channel.position(0);
        channel.write(header);
        channel.position(position);
    }
}
