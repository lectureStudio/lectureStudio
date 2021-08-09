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

package org.lecturestudio.core.model.shape;

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ScreenCaptureShape extends Shape {

    private BufferedImage frame;
    private DesktopSource source;
    private DesktopSourceType type;

    public ScreenCaptureShape(DesktopSource source, DesktopSourceType type, BufferedImage frame) {
        this.source = source;
        this.type = type;
        this.frame = frame;
    }

    public BufferedImage getFrame() {
        return frame;
    }

    public DesktopSource getSource() {
        return source;
    }

    public DesktopSourceType getType() {
        return type;
    }

    @Override
    public Shape clone() {
        BufferedImage clonedFrame = new BufferedImage(frame.getWidth(), frame.getHeight(), frame.getType());

        Graphics2D g = clonedFrame.createGraphics();
        g.drawImage(frame, 0, 0, null);
        g.dispose();

        return new ScreenCaptureShape(new DesktopSource(source.title, source.id), type, clonedFrame);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        DataBufferByte frameBuffer = (DataBufferByte) frame.getRaster().getDataBuffer();
        byte[] frameData = frameBuffer.getData();
        int frameDataLength = frameData.length;

        byte[] titleData = source.title.getBytes(StandardCharsets.UTF_8);
        int titleDataLength = titleData.length;

        // Buffer = 6 integer + 1 long + frame data + title string
        int bufferLength = 24 + 8 + frameDataLength + titleDataLength;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);

        // Set frame metadata
        buffer.putInt(frame.getWidth());
        buffer.putInt(frame.getHeight());
        buffer.putInt(frame.getType());
        buffer.putInt(frameDataLength);

        // Set desktop source metadata
        buffer.putLong(source.id);
        buffer.putInt(type.ordinal());
        buffer.putInt(titleDataLength);

        // Set variable length data
        buffer.put(frameData);
        buffer.put(titleData);

        return buffer.array();
    }

    @Override
    protected void parseFrom(byte[] input) throws IOException {
        ByteBuffer buffer = createBuffer(input);

        // Get frame metadata
        int frameWidth = buffer.getInt();
        int frameHeight = buffer.getInt();
        int frameType = buffer.getInt();
        int frameDataLength = buffer.getInt();

        // Get desktop source metadata
        long sourceId = buffer.getLong();
        int typeOrdinal = buffer.getInt();
        int titleDataLength = buffer.getInt();

        // Get frame data
        byte[] frameData = new byte[frameDataLength];
        buffer.get(frameData);

        // Get source title
        byte[] titleData = new byte[titleDataLength];
        buffer.get(titleData);

        // Create image
        frame = new BufferedImage(frameWidth, frameHeight, frameType);
        frame.getRaster().setDataElements(0, 0, frameWidth, frameHeight, frameData);

        // Create desktop source + type
        source = new DesktopSource(new String(titleData, StandardCharsets.UTF_8), sourceId);
        type = DesktopSourceType.values()[typeOrdinal];
    }
}
