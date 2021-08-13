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

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

public class ScreenCaptureSequence {

    private final DesktopSource source;
    private final TreeMap<Long, BufferedImage> frames = new TreeMap<>();

    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;

    public ScreenCaptureSequence(DesktopSource source) {
        this.source = source;
    }

    public void addFrame(BufferedImage frame, long timestamp) {
        frames.put(timestamp, frame);
        updateTimestamps(timestamp);
    }

    public BufferedImage seekFrame(long seekTime) {
        Map.Entry<Long, BufferedImage> entry = frames.floorEntry(seekTime);
        return entry != null ? entry.getValue() : null;
    }

    public DesktopSource getSource() {
        return source;
    }

    public TreeMap<Long, BufferedImage> getFrames() {
        return frames;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    private void updateTimestamps(long frameTime) {
        if (frameTime < startTime) {
            startTime = frameTime;
        }
        if (frameTime > endTime) {
            endTime = frameTime;
        }
    }

    @Override
    public String toString() {
        return "ScreenCaptureSequence{" +
                "sourceId=" + source.id +
                ", sourceTitle=" + source.title +
                ", frames=" + frames.size() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
