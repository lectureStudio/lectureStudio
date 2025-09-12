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

package org.lecturestudio.presenter.api.model;

/**
 * Defines the possible positions where notes can be displayed in relation to slides.
 * This enum is used to configure the layout and display preferences for notes
 * in the presenter interface.
 *
 * @author Alex Andres
 */
public enum NoteSlidePosition {

    /**
     * Notes are displayed below the slide preview in the main interface.
     */
    BELOW_SLIDE_PREVIEW,

    /**
     * Notes are not displayed.
     */
    NONE,

    /**
     * Notes are displayed in an external window or component.
     */
    EXTERNAL

}
