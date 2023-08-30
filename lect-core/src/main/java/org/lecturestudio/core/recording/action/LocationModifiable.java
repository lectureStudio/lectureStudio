package org.lecturestudio.core.recording.action;

import org.lecturestudio.core.geometry.Point2D;

public interface LocationModifiable {

    /**
     * Moves the points associated with this action by the amount delta.
     * Modifies the original points and therefore the initial placement.
     *
     * @param delta the amount by how much to move the points
     */
    void moveByDelta(Point2D delta);
}