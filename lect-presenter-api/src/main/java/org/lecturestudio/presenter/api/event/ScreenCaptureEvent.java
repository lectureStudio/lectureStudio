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

package org.lecturestudio.presenter.api.event;

import org.lecturestudio.presenter.api.model.ScreenCapture;

public class ScreenCaptureEvent {

    public enum Type { CREATED, REMOVED, SELECTED }

    private final ScreenCapture oldCapture;
    private final ScreenCapture capture;
    private final Type type;

    public ScreenCaptureEvent(ScreenCapture capture, Type type) {
        this(null, capture, type);
    }

    public ScreenCaptureEvent(ScreenCapture oldCapture, ScreenCapture capture, Type type) {
        this.oldCapture = oldCapture;
        this.capture = capture;
        this.type = type;
    }

    public ScreenCapture getOldCapture() {
        return oldCapture;
    }

    public ScreenCapture getCapture() {
        return capture;
    }

    public Type getType() {
        return type;
    }

    public boolean created() {
        return type == Type.CREATED;
    }

    public boolean closed() {
        return type == Type.CREATED;
    }

    public boolean selected() {
        return type == Type.SELECTED;
    }
}
