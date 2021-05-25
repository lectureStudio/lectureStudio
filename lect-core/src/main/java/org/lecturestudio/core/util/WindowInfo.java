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

package org.lecturestudio.core.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.*;

public class WindowInfo {

    private final static int MAX_TITLE_LENGTH = 128;

    private final WinDef.HWND handle;
    private String title;
    private WinDef.RECT rect;
    private WinUser.WINDOWPLACEMENT placement;

    public WindowInfo(WinDef.HWND handle) {
        this.handle = handle;
        initialize();
    }

    private void initialize() {
        // Get window title
        title = getTitle();

        // Get window bounds
        rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(handle, rect);

        // Get window placement
        placement = new WinUser.WINDOWPLACEMENT();
        User32.INSTANCE.GetWindowPlacement(handle, placement);
    }

    public WinDef.HWND getHandle() {
        return handle;
    }

    public String getTitle() {
        if (title == null) {
            char[] titleBuffer = new char[MAX_TITLE_LENGTH];
            User32.INSTANCE.GetWindowText(handle, titleBuffer, MAX_TITLE_LENGTH);
            title = Native.toString(titleBuffer);
        }
        return title;
    }

    public Rectangle getBounds() {
        if (User32.INSTANCE.GetWindowRect(handle, rect)) {
            int width = Math.abs(rect.right - rect.left);
            int height = Math.abs(rect.top - rect.bottom);

            return new Rectangle(rect.left, rect.top, width, height);
        }
        return new Rectangle();
    }

    public boolean isMinimized() {
        if (User32.INSTANCE.GetWindowPlacement(handle, placement).booleanValue()) {
            return placement.showCmd == 2;
        }
        return false;
    }

    public boolean isMaximized() {
        if (User32.INSTANCE.GetWindowPlacement(handle, placement).booleanValue()) {
            return placement.showCmd == 3;
        }
        return false;
    }
}
