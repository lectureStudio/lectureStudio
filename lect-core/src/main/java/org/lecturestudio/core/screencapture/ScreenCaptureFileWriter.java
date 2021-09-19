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

import com.pngencoder.PngEncoder;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

/**
 * This class is used to write screen capture recordings to temporary files.
 *
 * @author Maximilian Felix Ratzke
 */
public class ScreenCaptureFileWriter {

    /** Header marker 'FORM' represented as integer value. */
    public final static int FORMAT_MARKER = 1179603533;

    /** Header marker 'CHAN' represented as integer value. */
    public final static int CHANNEL_MARKER = 1128808782;

    /** Header marker 'DATA' represented as integer value. */
    public final static int DATA_MARKER = 1145132097;

    private static final int FORMAT_HEADER_SIZE = 8;
    private static final int DATA_HEADER_SIZE = 4;

    private final SeekableByteChannel detailsChannel;
    private final SeekableByteChannel framesChannel;

    private ScreenCaptureSequence currentSequence;
    private ScreenCaptureFormat recordingFormat;

    private final PngEncoder encoder;

    private long latestFrameTimestamp = 0;
    private long totalBytesWritten = 0;

    /**
     * Creates a new {@link ScreenCaptureFileWriter} instance.
     *
     * @param detailsFile The file which should be used to write the metadata
     * @param framesFile The file which should be used to write the frame data
     *
     * @throws IOException Thrown if one of the provided files could not be opened.
     */
    public ScreenCaptureFileWriter(File detailsFile, File framesFile) throws IOException {
        this.detailsChannel = FileChannel.open(detailsFile.toPath(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        this.framesChannel = FileChannel.open(framesFile.toPath(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

        encoder = new PngEncoder();
    }

    /**
     * Sets the {@link ScreenCaptureFormat} of the export.
     *
     * @param format The format used for exporting
     *
     * @throws IOException Thrown if the file headers could not be written.
     */
    public void setScreenCaptureFormat(ScreenCaptureFormat format) throws IOException {
        this.recordingFormat = format;

        // Update written header to match format
        writeFormatHeader();
        writeDataHeader();

        reset();
    }

    /**
     * Sets the {@link DesktopSource} of the screen capture to be exported.
     *
     * @param source The {@link DesktopSource} used for export.
     *
     * @throws IOException Thrown if the channel header could not be written.
     */
    public void setDesktopSource(DesktopSource source) throws IOException {
        // Finish current sequence if exists
        if (currentSequence != null) {
            currentSequence.setEndTime(latestFrameTimestamp);
            writeChannelHeader(currentSequence);
        }

        currentSequence = new ScreenCaptureSequence(source);
    }

    /**
     * Returns the number of bytes which were written by the file writer so far.
     */
    public long getTotalBytesWritten() {
        return totalBytesWritten;
    }

    /**
     * Create a frame data chunk and writes it to the temporary file.
     *
     * @param frame The {@link BufferedImage} to write.
     * @param timestamp The timestamp of the frame during recording
     * @return The number of written bytes.
     *
     * @throws IOException Thrown if the frame could not be written.
     */
    public int writeFrame(BufferedImage frame, long timestamp) throws IOException {

        byte[] bytes = compressFrame(frame);

        return writeFrameBytes(bytes, timestamp);
    }

    /**
     * Resets the positions of the metadata and frames streams to the start.
     *
     * @throws IOException Thrown if the file channel position could not be updated.
     */
    public void reset() throws IOException {
        // Prevent headers from being overwritten
        detailsChannel.position(FORMAT_HEADER_SIZE);
        framesChannel.position(DATA_HEADER_SIZE);
    }

    /**
     * Closes the stream and writes the headers of the last sequence, if exists.
     * @throws IOException Thrown if the files could not be written.
     */
    public void close() throws IOException {
        writeFormatHeader();

        // Finish current sequence
        if (currentSequence != null) {
            if (!currentSequence.hasEndTime()) {
                currentSequence.setEndTime(latestFrameTimestamp);
            }
            writeChannelHeader(currentSequence);
        }

        detailsChannel.close();
        framesChannel.close();
    }

    private byte[] compressFrame(BufferedImage frame) {
        return encoder.withBufferedImage(frame).toBytes();
    }

    private int writeFrameBytes(byte[] frameBytes, long timestamp) throws IOException {
        int bytesWritten = 0;
        if (framesChannel.isOpen()) {
            // Add offset + image data to byte buffer
            ByteBuffer buffer = ByteBuffer.allocate(frameBytes.length + 12);
            buffer.putInt(frameBytes.length);
            buffer.putLong(timestamp);
            buffer.put(frameBytes);
            buffer.flip();

            // Repeat until buffer is completely written to the channel
            while (buffer.hasRemaining()) {
                bytesWritten += framesChannel.write(buffer);
            }

            // Set frame timestamp as start time if sequence is not initialized already
            if (!currentSequence.hasStartTime()) {
                currentSequence.setStartTime(timestamp);
            }
            latestFrameTimestamp = timestamp;
        }

        totalBytesWritten += bytesWritten;
        return bytesWritten;
    }

    /**
     * Writes the header containing the ScreenCaptureFormat to the details channel.
     */
    private void writeFormatHeader() throws IOException {
        if (!detailsChannel.isOpen()) {
            throw new RuntimeException("Unable to write format header, details channel is not open.");
        }

        // Make sure to write format header only once at start of details file
        if (detailsChannel.size() > 0) {
            return;
        }

        int frameRate = this.recordingFormat.getFrameRate();

        ByteBuffer header = ByteBuffer.allocate(FORMAT_HEADER_SIZE);
        header.putInt(FORMAT_MARKER);
        header.putInt(frameRate);
        header.flip();

        // Write header at start of file
        long position = detailsChannel.position();
        detailsChannel.position(0);
        detailsChannel.write(header);
        detailsChannel.position(position);
    }

    /**
     * Writes a channel header chunk into the details file to indicate a channel switch.
     */
    private void writeChannelHeader(ScreenCaptureSequence sequence) throws IOException {
        if (!detailsChannel.isOpen()) {
            throw new RuntimeException("Unable to write channel header, details channel is not open.");
        }

        String sourceTitle = sequence.getSource().title;
        if (sourceTitle == null)
            sourceTitle = "";

        byte[] sourceTitleBytes = sourceTitle.getBytes(StandardCharsets.UTF_8);

        ByteBuffer header = ByteBuffer.allocate(32 + sourceTitleBytes.length);
        header.putInt(CHANNEL_MARKER);
        header.putLong(sequence.getSource().id);
        header.putLong(sequence.getStartTime());
        header.putLong(sequence.getEndTime());
        header.putInt(sourceTitleBytes.length);
        header.put(sourceTitleBytes);
        header.flip();

        System.out.println(sequence);

        detailsChannel.write(header);
    }

    /**
     * Writes a data header into the frames file.
     */
    private void writeDataHeader() throws IOException {
        if (!framesChannel.isOpen()) {
            throw new RuntimeException("Unable to write data header, frames channel is not open.");
        }

        // Make sure to write data header only once at start of frames file
        if (framesChannel.size() > 0) {
            return;
        }

        ByteBuffer header = ByteBuffer.allocate(4);
        header.putInt(DATA_MARKER);
        header.flip();

        framesChannel.write(header);
    }
}
