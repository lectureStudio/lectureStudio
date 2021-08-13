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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public class ScreenCaptureDataParser {

    public static ScreenCaptureData parseStream(InputStream stream) throws IOException {
        requireNonNull(stream);

        // TODO: Load screen capture in chunks + asynchronous

        // Read bytes from stream
        byte[] bytes = stream.readAllBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // Parse FORM header
        int frameRate = parseFormatHeader(buffer);

        ScreenCaptureFormat screenCaptureFormat = new ScreenCaptureFormat(frameRate);
        ScreenCaptureData screenCaptureData = new ScreenCaptureData(screenCaptureFormat);
        ScreenCaptureSequence currentSequence = null;

        // Process bytes while remaining in buffer
        while (buffer.hasRemaining()) {
            int currentPosition = buffer.position();

            // Read first 4 bytes to check for CHAN header
            byte[] headerBytes = new byte[4];
            buffer.get(headerBytes);
            String header = new String(headerBytes, StandardCharsets.UTF_8);

            if (header.equals(ScreenCaptureOutputStream.CHANNEL_HEADER)) {
                // Complete previous sequence and add to list if new channel header occurred
                if (currentSequence != null) {
                    screenCaptureData.addSequence(currentSequence);
                }

                long sourceId = buffer.getLong();
                int sourceTitleLength = buffer.getInt();
                byte[] sourceTitleBytes = new byte[sourceTitleLength];
                buffer.get(sourceTitleBytes);
                String sourceTitle = new String(sourceTitleBytes, StandardCharsets.UTF_8);

                // Create new sequence from parsed channel header
                DesktopSource source = new DesktopSource(sourceTitle, sourceId);
                currentSequence = new ScreenCaptureSequence(source);
            } else {
                // Check if channel header was already parsed
                if (currentSequence == null) {
                    throw new RuntimeException("Cannot parse frame before channel header had occurred.");
                }

                // Reset position to previous position
                buffer.position(currentPosition);

                // Parse frame metadata
                int frameLength = buffer.getInt();
                long frameTimestamp = buffer.getLong();
                byte[] frameBytes = new byte[frameLength];
                buffer.get(frameBytes);

                // Parse buffered image
                InputStream frameStream = new ByteArrayInputStream(frameBytes);
                BufferedImage frame = ImageIO.read(frameStream);
                frameStream.close();

                // Add frame to sequence
                currentSequence.addFrame(frame, frameTimestamp);
            }
        }

        // Complete last sequence and add to list if exists
        if (currentSequence != null) {
            screenCaptureData.addSequence(currentSequence);
        }

        return screenCaptureData;
    }

    private static int parseFormatHeader(ByteBuffer buffer) {
        byte[] headerBytes = new byte[4];
        buffer.get(headerBytes);

        String formatHeader = new String(headerBytes, StandardCharsets.UTF_8);
        int frameRate = buffer.getInt();

        if (!formatHeader.equals(ScreenCaptureOutputStream.FORMAT_HEADER)) {
            throw new RuntimeException("Failed to parse format header of screen capture stream.");
        }

        return frameRate;
    }
}
