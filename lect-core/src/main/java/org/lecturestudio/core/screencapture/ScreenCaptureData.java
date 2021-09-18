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

import org.lecturestudio.core.model.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * This class is used to store {@link ScreenCaptureSequence ScreenCaptureSequences} as well as the
 * {@link ScreenCaptureFormat} used during recording. It also provides possibilities to seek for a screen capture frame
 * based on a timestamp.
 *
 * @author Maximilian Felix Ratzke
 */
public class ScreenCaptureData {

    private final ScreenCaptureFormat format;

    // Used to store ScreenCaptureSequences, the start timestamp of each sequence is used as key
    private final TreeMap<Long, ScreenCaptureSequence> sequences = new TreeMap<>();

    // Used to store all exclusions (sections which were cut during editing and therefore should be skipped)
    private final List<Interval<Integer>> exclusions = new ArrayList<>();

    public ScreenCaptureData(ScreenCaptureFormat format) {
        this.format = format;
    }

    public ScreenCaptureFormat getFormat() {
        return format;
    }

    public TreeMap<Long, ScreenCaptureSequence> getSequences() {
        return sequences;
    }

    /**
     * Adds a ScreenCaptureSequence to the ScreenCaptureData instance.
     *
     * @param sequence The ScreenCaptureSequence to add.
     */
    public void addSequence(ScreenCaptureSequence sequence) {
        // Add all previously added exclusions to sequence
        for (Interval<Integer> exclusion : exclusions) {
            sequence.addExclusion(exclusion);
        }

        sequences.put(sequence.getStartTime(), sequence);
    }

    /**
     * Searches the known ScreenCaptureSequence instances based on a given seek time (in ms)
     *
     * @param seekTime The timestamp since start of the recording to search for.
     * @return The sequence which has frame data during the seek time or null, if no matching sequence was found.
     */
    public ScreenCaptureSequence seekSequence(long seekTime) {
        for (ScreenCaptureSequence sequence : sequences.values()) {
            if (sequence.containsTime(seekTime)) {
                return sequence;
            }
        }
        return null;
    }

    /**
     * Adds a time interval to exclude from the screen capture recording
     *
     * @param exclusion The interval to add
     */
    public void addExclusion(Interval<Integer> exclusion) {
        if (exclusion.lengthLong() > 0) {
            exclusions.add(exclusion);

            // Add exclusions to all sequences
            for (ScreenCaptureSequence sequence : sequences.values()) {
                sequence.addExclusion(exclusion);
            }
        }
    }

    /**
     * Removes an existing time interval from the exclusions
     *
     * @param exclusion The interval to remove
     */
    public void removeExclusion(Interval<Integer> exclusion) {
        exclusions.remove(exclusion);

        // Remove exclusions from all sequences
        for (ScreenCaptureSequence sequence : sequences.values()) {
            sequence.removeExclusion(exclusion);
        }
    }
}

