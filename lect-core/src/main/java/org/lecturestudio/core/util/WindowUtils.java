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

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WindowUtils {

    public static List<WindowInfo> listActiveWindows() {
        List<WindowInfo> windows = new ArrayList<>();

        User32.INSTANCE.EnumWindows((handle, data) -> {
            if (User32.INSTANCE.IsWindowVisible(handle)) {
                WindowInfo window = new WindowInfo(handle);
                windows.add(window);
            }
            return true;
        }, null);

        return windows.stream().filter(window -> !window.getTitle().isEmpty()).collect(Collectors.toList());
    }

    public static WinDef.HWND getForegroundWindow() {
        return User32.INSTANCE.GetForegroundWindow();
    }

    public static BufferedImage createScreenCapture(WinDef.HWND handle) throws AWTException {
        WindowInfo window = new WindowInfo(handle);
        WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.SetForegroundWindow(handle);
        BufferedImage image = new Robot().createScreenCapture(window.getBounds());
        // User32.INSTANCE.SetForegroundWindow(foregroundWindow);
        return image;
    }
}
