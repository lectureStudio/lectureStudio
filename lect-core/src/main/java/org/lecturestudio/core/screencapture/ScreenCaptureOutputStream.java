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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class ScreenCaptureOutputStream {

    // Make sure to have a fixed number of characters for each header which matches the respective header size
    private static final String FORMAT_HEADER = "FORM";
    private static final String DATA_HEADER = "DATA";
    private static final String CHANNEL_HEADER = "CHAN";

    private final FileChannel channel;
    private ScreenCaptureFormat recordingFormat;

    private long totalBytesWritten = 0;

    private int lastFrameChannelId = -1;
    private int activeChannelId = 0;

    public ScreenCaptureOutputStream(File outputFile) throws IOException {
        channel = FileChannel.open(outputFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    public void setScreenCaptureFormat(ScreenCaptureFormat format) {
        this.recordingFormat = format;
    }

    public ScreenCaptureOutputStream clone() {
        // TODO: Implement actual clone

        return this;
    }

    /**
     * Set the id of the active channel. All frames written after will be associated with the active channel id.
     *
     * @param channelId the id of the channel.
     */
    public void setActiveChannelId(int channelId) {
        this.activeChannelId = channelId;
    }

    public long getTotalBytesWritten() {
        return totalBytesWritten;
    }

    public int writeFrameBuffer(ByteBuffer frameBuffer) throws IOException {
        int writtenBytes = 0;
        if (channel.isOpen()) {

            // TODO: Find efficient way to compress screen capture frames
            // BufferedImage compressedImage = ImageUtils.compress(image, 0.5f);

//            byte[] frameBytes = new byte[frameBuffer.capacity()];
//            frameBuffer.get(frameBytes);
//
//            byte[] compressedFrameBytes = ImageUtils.compress(frameBytes);
//
//            float compression = compressedFrameBytes.length / (float) frameBytes.length;
//            System.out.println("Compressed from " + frameBytes.length + " to " + compressedFrameBytes.length + " " + compression);

            int frameBufferSize = frameBuffer.capacity();

            // Add offset + image data to byte buffer
            ByteBuffer buffer = ByteBuffer.allocate(frameBufferSize + 4);
            buffer.putInt(frameBufferSize);
            buffer.put(frameBuffer);
            buffer.flip();

            // Handle change of channel id
            if (activeChannelId != lastFrameChannelId) {
                writeChannelHeader(activeChannelId);
            }

            lastFrameChannelId = activeChannelId;

            // Repeat until buffer is completely written to the channel
            while (buffer.hasRemaining()) {
                writtenBytes += channel.write(buffer);
            }
        }

        totalBytesWritten += writtenBytes;
        return writtenBytes;
    }

    /**
     * Writes a new frame to the output stream including its data size.
     *
     * @param image The image frame to be written.
     * @return The number of bytes written to the stream.
     */
    public int writeFrame(BufferedImage image) throws IOException {
        DataBufferByte imageBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        return writeFrameBuffer(ByteBuffer.wrap(imageBuffer.getData()));
    }

    public void reset() throws IOException {
        // Prevent header from being overwritten
        channel.position(12);
    }

    public void close() throws IOException {
        writeHeader();
        channel.force(true);
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
        header.flip();

        channel.write(header);
    }

    private void writeHeader() throws IOException {
        int frameRate = this.recordingFormat.getFrameRate();

        ByteBuffer header = ByteBuffer.allocate(12);
        header.put(FORMAT_HEADER.getBytes(StandardCharsets.UTF_8));
        header.put(BitConverter.getLittleEndianBytes(frameRate));
        header.put(DATA_HEADER.getBytes(StandardCharsets.UTF_8));
        header.flip();

        // Write header at start of file
        long position = channel.position();
        channel.position(0);
        channel.write(header);
        channel.position(position);
    }
}
