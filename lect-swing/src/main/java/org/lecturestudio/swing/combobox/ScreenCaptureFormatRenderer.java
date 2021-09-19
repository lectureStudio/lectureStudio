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

package org.lecturestudio.swing.combobox;

import org.lecturestudio.core.screencapture.ScreenCaptureFormat;

import javax.swing.*;
import java.awt.*;

/**
 * This class is used to render the {@link ScreenCaptureFormat} in the settings page.
 *
 * @author Maximilian Felix Ratzke
 */
public class ScreenCaptureFormatRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String text = "";

        ScreenCaptureFormat format = (ScreenCaptureFormat) value;
        if (format != null) {
            float fpsRate = format.getFrameRate();
            text = String.format("%s", fpsRate);
        }

        setText(text);
        return this;
    }
}
