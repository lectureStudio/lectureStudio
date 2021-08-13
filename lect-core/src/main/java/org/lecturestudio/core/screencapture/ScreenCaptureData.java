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

import java.util.Map;
import java.util.TreeMap;

public class ScreenCaptureData {

    private final ScreenCaptureFormat format;
    private final TreeMap<Long, ScreenCaptureSequence> sequences = new TreeMap<>();

    public ScreenCaptureData(ScreenCaptureFormat format) {
        this.format = format;
    }

    public void addSequence(ScreenCaptureSequence sequence) {
        sequences.put(sequence.getStartTime(), sequence);
    }

    public ScreenCaptureSequence seekSequence(long seekTime) {
        Map.Entry<Long, ScreenCaptureSequence> entry = sequences.floorEntry(seekTime);
        return entry != null && entry.getValue().getEndTime() > seekTime ? entry.getValue() : null;
    }

    public ScreenCaptureFormat getFormat() {
        return format;
    }

    public TreeMap<Long, ScreenCaptureSequence> getSequences() {
        return sequences;
    }
}

