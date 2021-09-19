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
import org.lecturestudio.core.model.Interval;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class acts as container to store and search the frames captured during a screen capture session.
 * It is related to exactly one {@link DesktopSource} and is meant for continuous screen captures.
 *
 * If one {@link DesktopSource} should be captured multiple times with interruptions in between,
 * separate {@link ScreenCaptureSequence ScreenCaptureSequences} should be created.
 *
 * @author Maximilian Felix Ratzke
 */
public class ScreenCaptureSequence {

    private final DesktopSource source;
    private final TreeMap<Long, BufferedImage> frames = new TreeMap<>();
    private final List<Interval<Integer>> exclusions = new ArrayList<>();

    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;

    private long startPadding = 0;
    private long endPadding = 0;


    public ScreenCaptureSequence(DesktopSource source) {
        this.source = source;
    }

    /**
     * Adds a frame to the sequence at a specific timestamp.
     *
     * @param frame The frame to add.
     * @param timestamp The timestamp of the frame capture (in ms).
     */
    public void addFrame(BufferedImage frame, long timestamp) {
        frames.put(timestamp, frame);
        updateTimestamps(timestamp);
    }

    /**
     * Search for a frame by a given timestamp.
     *
     * @param seekTime The search timestamp (in ms).
     * @return The least frame with a timestamp less or equal the requested timestamp or null, if no such frame was found.
     */
    public BufferedImage seekFrame(long seekTime) {
        if (!containsTime(seekTime)) {
            return null;
        }

        long excludedSeekTime = getExcludedTime(seekTime);
        Map.Entry<Long, BufferedImage> entry = frames.floorEntry(excludedSeekTime);
        return entry != null ? entry.getValue() : null;
    }

    /**
     * Adds an exclusion interval to the {@link ScreenCaptureSequence}.
     *
     * These intervals are used to take section in the video stream into account which were cut during editing and
     * should therefore be skipped during screen capture replay.
     *
     * @param exclusion The interval which should be excluded from the screen capture.
     */
    public void addExclusion(Interval<Integer> exclusion) {
        // Only add exclusions which start before the sequence ends
        if (exclusion.lengthLong() > 0 && exclusion.getStart() < getExcludedEndTime() && !exclusions.contains(exclusion)) {
            exclusions.add(exclusion);

            // Update paddings
            startPadding = getStartPadding();
            endPadding = getEndPadding();
        }
    }

    /**
     * Removes an exclusion interval from the {@link ScreenCaptureSequence} if exists.
     *
     * @param exclusion The exclusion interval to remove.
     */
    public void removeExclusion(Interval<Integer> exclusion) {
        if (exclusions.remove(exclusion)) {

            // Update paddings
            startPadding = getStartPadding();
            endPadding = getEndPadding();
        }
    }

    private long getStartPadding() {
        long padding = 0;

        for (Interval<Integer> exclusion : exclusions) {
            if (exclusion.getStart() < startTime - padding) {
                long start = Math.min(exclusion.getEnd(), startTime - padding) - exclusion.getStart();
                padding += Math.max(start, 0);
            }
        }

        return padding;
    }

    private long getEndPadding() {
        long padding = 0;

        for (Interval<Integer> exclusion : exclusions) {
            long end = Math.min(exclusion.getEnd(), endTime - padding);
            padding += Math.max(end - exclusion.getStart(), 0);
        }

        return padding;
    }

    /**
     * Returns the {@link DesktopSource} of the current {@link ScreenCaptureSequence}.
     */
    public DesktopSource getSource() {
        return source;
    }

    /**
     * Returns all frames of the current {@link ScreenCaptureSequence}.
     * The timestamp of each frame is used as key.
     */
    public TreeMap<Long, BufferedImage> getFrames() {
        return frames;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Get the start time of the sequence with already subtracted exclusions
     *
     * @return The excluded start time
     */
    public long getExcludedStartTime() {
        // System.out.println("StartTime: " + startTime + " padding: " + startPadding + " method: " + getStartPadding());
        return startTime - startPadding;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean hasStartTime() {
        return startTime < Long.MAX_VALUE;
    }

    public long getEndTime() {
        return endTime;
    }

    /**
     * Get the end time of the sequence with already subtracted exclusions.
     *
     * @return The excluded end time
     */
    public long getExcludedEndTime() {
        return endTime - endPadding;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean hasEndTime() {
        return endTime > Long.MIN_VALUE;
    }

    public long getLength() {
        return Math.max(endTime, startTime);
    }

    /**
     * Returns the length of the {@link ScreenCaptureSequence} after all exclusions were applied.
     */
    public long getExcludedLength() {
        return Math.max(getExcludedEndTime() - getExcludedStartTime(), 0);
    }

    /**
     * Checks whether a given timestamp lies in the period of the {@link ScreenCaptureSequence}.
     * This also takes potential exclusions into account.
     *
     * @param time The timestamp to check.
     * @return True, if the timestamp lies in the period, otherwise false.
     */
    public boolean containsTime(long time) {
        return time >= getExcludedStartTime() && time <= getExcludedEndTime();
    }

    private void updateTimestamps(long frameTime) {
        if (frameTime < startTime) {
            startTime = frameTime;
        }
        if (frameTime > endTime) {
            endTime = frameTime;
        }
    }

    /**
     * Calculates and adds a padding to a given timestamp based on the registered exclusions.
     */
    private long getExcludedTime(long time) {
        long padding = 0;
        long skipped = 0;

        // Calculate padding for all registered exclusions
        for (Interval<Integer> exclusion : exclusions) {
            long start = exclusion.getStart() + padding;

            if (exclusion.getStart() < time) {
                padding += exclusion.lengthLong() + skipped;
                skipped = 0;
            } else {
                skipped += exclusion.lengthLong();
            }
        }

        return time + padding;
    }

    @Override
    public String toString() {
        return "ScreenCaptureSequence{" +
                "sourceId=" + source.id +
                ", sourceTitle=" + source.title +
                ", frames=" + frames.size() +
                ", startTime=" + startTime +
                ", excludedStartTime=" + getExcludedStartTime() +
                ", endTime=" + endTime +
                ", excludedEndTime=" + getExcludedEndTime() +
                '}';
    }
}
