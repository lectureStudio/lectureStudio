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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.model.Stopwatch;

/**
 * Interface to configure a stopwatch visually.
 *
 * @author Dustin Ringel
 * @author Alex Andres
 */
public interface StopwatchConfigView extends View {

    /**
     * Sets the action to be executed when the stopwatch type is selected or changed.
     *
     * @param action The consumer action that accepts a StopwatchType parameter.
     */
    void setOnStopwatchType(ConsumerAction<Stopwatch.StopwatchType> action);

    /**
     * Sets the type of the stopwatch.
     *
     * @param type The stopwatch type to set.
     */
    void setStopwatchType(Stopwatch.StopwatchType type);

    /**
     * Sets the time value to be displayed or used by the stopwatch.
     *
     * @param path The string property containing the stopwatch time value.
     */
    void setStopwatchTime(StringProperty path);

    /**
     * Sets the error message to be displayed in the view.
     *
     * @param error The string property containing the error message.
     */
    void setError(StringProperty error);

    /**
     * Sets the action to be executed when the stopwatch configuration view is closed.
     *
     * @param action The action to execute on close.
     */
    void setOnClose(Action action);

    /**
     * Sets the action to be executed when the stopwatch is started.
     *
     * @param action The action to execute on start.
     */
    void setOnStart(Action action);

}
