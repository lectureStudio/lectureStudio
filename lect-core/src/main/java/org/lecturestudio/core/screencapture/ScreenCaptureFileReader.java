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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ScreenCaptureFileReader {

    private final static ColorModel colorModel = new DirectColorModel(32,
            0x0000ff00,   // Red
            0x00ff0000,   // Green
            0xff000000,   // Blue
            0x000000ff    // Alpha
    );

    public static void parseStream(RandomAccessScreenCaptureStream stream, ProgressCallback callback) throws IOException {
        requireNonNull(stream);

        // TODO: Load screen capture in chunks to improve memory usage?

        // Read bytes from stream
        byte[] bytes = stream.readAllBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // Parse FORM header
        int formatHeaderMarker = buffer.getInt();
        if (formatHeaderMarker != ScreenCaptureFileWriter.FORMAT_MARKER) {
            throw new RuntimeException("Unable to parse format header from screen capture stream.");
        }

        int frameRate = buffer.getInt();

        ScreenCaptureFormat screenCaptureFormat = new ScreenCaptureFormat(frameRate);
        ScreenCaptureData screenCaptureData = new ScreenCaptureData(screenCaptureFormat);

        // Parse channel headers
        int channelMarker = buffer.getInt();

        while (buffer.hasRemaining() && channelMarker == ScreenCaptureFileWriter.CHANNEL_MARKER) {

            long sourceId = buffer.getLong();
            long startTime = buffer.getLong();
            long endTime = buffer.getLong();

            int sourceTitleLength = buffer.getInt();
            byte[] sourceTitleBytes = new byte[sourceTitleLength];
            buffer.get(sourceTitleBytes);
            String sourceTitle = new String(sourceTitleBytes, StandardCharsets.UTF_8);

            ScreenCaptureSequence sequence = new ScreenCaptureSequence(new DesktopSource(sourceTitle, sourceId));
            sequence.setStartTime(startTime);
            sequence.setEndTime(endTime);

            screenCaptureData.addSequence(sequence);

            if (buffer.remaining() >= 4) {
                channelMarker = buffer.getInt();
            }
        }

        // Notify requester about completion of header parsing
        callback.onScreenCaptureData(screenCaptureData);

        if (channelMarker != ScreenCaptureFileWriter.DATA_MARKER) {
            throw new RuntimeException("Unable to parse data header from screen capture stream.");
        }

        long frameBytesLength = buffer.remaining();

        // Only parse frames if at least one sequence exists
        if (screenCaptureData.getSequences().size() > 0) {
            ScreenCaptureSequence currentSequence = screenCaptureData.getSequences().firstEntry().getValue();

            // Process bytes while remaining in buffer
            while (buffer.hasRemaining()) {

                // Parse frame metadata
                int frameLength = buffer.getInt();
                long frameTimestamp = buffer.getLong();

                byte[] frameBytes = new byte[frameLength];
                buffer.get(frameBytes);

                // Parse buffered image
                InputStream frameStream = new ByteArrayInputStream(frameBytes);
                BufferedImage frame = ImageIO.read(frameStream);
                frameStream.close();

                // TODO: Fix color model during export to skip this step
                // Convert frame to correct color model
                frame = convertColorModel(frame);

                // Add frame to current sequence if frame belong to sequence
                if (currentSequence.containsTime(frameTimestamp)) {
                    callback.onFrame(frame, frameTimestamp, currentSequence.getStartTime());
                }
                // Try to find next sequence which contains frame
                else {
                    Map.Entry<Long, ScreenCaptureSequence> entry = screenCaptureData.getSequences().floorEntry(frameTimestamp);
                    if (entry != null) {
                        ScreenCaptureSequence sequence = entry.getValue();
                        if (sequence.containsTime(frameTimestamp)) {
                            currentSequence = sequence;
                            callback.onFrame(frame, frameTimestamp, currentSequence.getStartTime());
                        }
                    }
                }

                callback.onFrameProgress(1 - (float) buffer.remaining() / frameBytesLength);
            }

            System.out.println("Frames: " + currentSequence.getFrames().size());
        }
    }

    private static BufferedImage convertColorModel(BufferedImage image) {
        WritableRaster raster = colorModel.createCompatibleWritableRaster(image.getWidth(), image.getHeight());
        BufferedImage converted = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        Graphics2D g2d = converted.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return converted;
    }

    public interface ProgressCallback {

        void onScreenCaptureData(ScreenCaptureData data);
        void onFrame(BufferedImage frame, long frameTime, long sequence);
        void onFrameProgress(float progress);
    }
}
