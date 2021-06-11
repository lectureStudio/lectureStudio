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

import org.lecturestudio.core.io.BitConverter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

public class ScreenCaptureOutputStream {

    // Make sure to have a fixed number of characters for each header which matches the respective header size
    private static final String FORMAT_HEADER = "FORM";
    private static final String DATA_HEADER = "DATA";
    private static final String CHANNEL_HEADER = "CHAN";

    private final SeekableByteChannel channel;
    private ScreenCaptureFormat recordingFormat;

    private int lastFrameChannelId = -1;
    private int activeChannelId = 0;

    public ScreenCaptureOutputStream(File outputFile) throws FileNotFoundException {
        FileOutputStream stream = new FileOutputStream(outputFile);
        channel = stream.getChannel();
    }

    public ScreenCaptureOutputStream(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat format) {
        this.recordingFormat = format;
    }

    /**
     * Set the id of the active channel. All frames written after will be associated with the active channel id.
     *
     * @param channelId the id of the channel.
     */
    public void setActiveChannelId(int channelId) {
        this.activeChannelId = channelId;
    }

    /**
     * Writes a new frame to the output stream including its data size.
     *
     * @param image The image frame to be written.
     * @return The number of bytes written to the stream.
     */
    public int writeFrame(BufferedImage image) throws IOException {
        if (channel.isOpen()) {
            DataBufferByte imageBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
            byte[] imageData = imageBuffer.getData();

            // Add offset + image data to byte buffer
            ByteBuffer buffer = ByteBuffer.wrap(imageData, 4, imageData.length + 4);
            buffer.putInt(0, imageData.length);

            // Handle change of channel id
            if (activeChannelId != lastFrameChannelId) {
                writeChannelHeader(activeChannelId);
            }

            lastFrameChannelId = activeChannelId;
            return channel.write(buffer);
        }
        return 0;
    }

    public void reset() throws IOException {
        // Prevent header from being overwritten
        channel.position(12);
    }

    public void close() throws IOException {
        writeHeader();
        channel.close();
    }

    /**
     * Inserts a channel header chunk into the output stream to indicate a channel switch.
     *
     * @param channelId The id of the channel to be switched to.
     */
    private void writeChannelHeader(int channelId) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8);
        header.put(CHANNEL_HEADER.getBytes(StandardCharsets.UTF_8));
        header.put(BitConverter.getLittleEndianBytes(channelId));

        channel.write(header);
    }

    private void writeHeader() throws IOException {
        int frameRate = this.recordingFormat.getFrameRate();

        ByteBuffer header = ByteBuffer.allocate(12);
        header.put(FORMAT_HEADER.getBytes(StandardCharsets.UTF_8));
        header.put(BitConverter.getLittleEndianBytes(frameRate));
        header.put(DATA_HEADER.getBytes(StandardCharsets.UTF_8));

        channel.position(0);
        channel.write(header);
    }
}
