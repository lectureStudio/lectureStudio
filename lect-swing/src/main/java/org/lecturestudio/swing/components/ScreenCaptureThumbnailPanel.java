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

package org.lecturestudio.swing.components;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.util.SwingUtils;

import javax.swing.*;

public class ScreenCaptureThumbnailPanel extends EditableThumbnailPanel {

    private final JButton pauseCaptureButton;

    public ScreenCaptureThumbnailPanel() {
        super();

        pauseCaptureButton = new JButton("Pause");
        container.add(pauseCaptureButton);
    }

    public void setScreenCaptureRenderer() {

    }

    public void setOnScreenCapturePause(Action action) {
        SwingUtils.bindAction(pauseCaptureButton, action);
    }
}
