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

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;

import javax.swing.*;
import java.awt.*;

public class ScreenCaptureThumbnailPanel extends EditableThumbnailPanel {

    private final RecordButton startRecordingButton;
    private final JButton stopRecordingButton;

    public ScreenCaptureThumbnailPanel() {
        super();

        // Configure start recording button
        startRecordingButton = new RecordButton();
        startRecordingButton.setIcon(AwtResourceLoader.getIcon("record-tool.svg", 25));
        startRecordingButton.setBlinkIcon(AwtResourceLoader.getIcon("record-blink-tool.svg", 25));
        startRecordingButton.setPauseIcon(AwtResourceLoader.getIcon("record-pause-tool.svg", 25));
        startRecordingButton.setPausedIcon(AwtResourceLoader.getIcon("record-resume-tool.svg", 25));
        startRecordingButton.setContentAreaFilled(false);
        startRecordingButton.setBorderPainted(false);

        // Configure stop recording button
        stopRecordingButton = new JButton();
        stopRecordingButton.setIcon(AwtResourceLoader.getIcon("record-stop-tool.svg", 25));
        stopRecordingButton.setEnabled(false);
        stopRecordingButton.setContentAreaFilled(false);
        stopRecordingButton.setBorderPainted(false);

        container.add(startRecordingButton);
        container.add(stopRecordingButton);
    }

    public void setOnStartRecording(Action action) {
        SwingUtils.bindAction(startRecordingButton, action);
    }

    public void setOnStopRecording(Action action) {
        SwingUtils.bindAction(stopRecordingButton, action);
    }

    public void setRecordingState(ExecutableState state) {
        boolean started = state == ExecutableState.Started || state == ExecutableState.Suspended;

        startRecordingButton.setState(state);
        stopRecordingButton.setEnabled(started);

        // Update border color of selected preview if recording
        Color borderColor;
        switch (state) {
            case Started:
                borderColor = Color.RED;
                break;
            case Suspended:
                borderColor = Color.ORANGE;
                break;
            default:
                borderColor = PageRenderer.DEFAULT_BORDER_COLOR;
        }

        pageRenderer.setSelectedBorderColor(borderColor);
        SwingUtils.invoke(list::repaint);
    }
}
