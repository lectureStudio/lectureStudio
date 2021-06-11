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

import java.io.Serializable;

public class ScreenCaptureFormat implements Serializable {

    public final static int[] DEFAULT_FRAME_RATES = new int[]{ 10, 20, 24, 30, 60 };

    private final int frameRate;

    public ScreenCaptureFormat(int frameRate) {
        this.frameRate = frameRate;
    }

    /**
     * Returns the number of frames per second which should be used during screen capture recording.
     *
     * @return the frame rate in fps.
     */
    public int getFrameRate() {
        return frameRate;
    }
}
