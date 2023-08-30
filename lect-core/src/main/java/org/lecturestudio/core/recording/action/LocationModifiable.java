package org.lecturestudio.core.recording.action;

import org.lecturestudio.core.geometry.Point2D;

public interface LocationModifiable {

    void moveByDelta(Point2D delta);
}