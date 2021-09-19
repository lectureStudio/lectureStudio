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

package org.lecturestudio.core.bus.event;

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;

import java.util.List;

/**
 * This event notifies about an update in the list of available {@link DesktopSource DesktopSources}.
 *
 * @author Maximilian Felix Ratzke
 */
public class ScreenCaptureSourceEvent extends BusEvent {

    private final List<DesktopSource> sources;
    private final DesktopSourceType type;

    public ScreenCaptureSourceEvent(List<DesktopSource> sources, DesktopSourceType type) {
        this.sources = sources;
        this.type = type;
    }

    public List<DesktopSource> getSources() {
        return sources;
    }

    public DesktopSourceType getType() {
        return type;
    }
}
