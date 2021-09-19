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

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;

import java.awt.image.BufferedImage;

/**
 * This interface defines required methods used for the selection view of screen capture sources.
 *
 * @author Maximilian Felix Ratzke
 */
public interface ScreenCaptureSourceSelectionView extends View {

    /**
     * Adds a new {@link DesktopSource} to the view.
     *
     * @param source The {@link DesktopSource} to add.
     * @param type The {@link DesktopSourceType} of the source.
     */
    void addDesktopSource(DesktopSource source, DesktopSourceType type);

    /**
     * Removes an existing {@link DesktopSource} from the view.
     *
     * @param source The {@link DesktopSource} to add.
     * @param type The {@link DesktopSourceType} of the source.
     */
    void removeDesktopSource(DesktopSource source, DesktopSourceType type);

    /**
     * Updates the preview image of a {@link DesktopSource} in the view.
     *
     * @param source The {@link DesktopSource} to update the preview image for.
     * @param image The new preview image.
     */
    void updateSourcePreviewImage(DesktopSource source, BufferedImage image);

    /**
     * Returns the currently selected source.
     */
    SelectedDesktopSource getSelectedDesktopSource();

    /**
     * Sets the action which should be performed when the OK button is pressed.
     * @param action The {@link Action} to perform.
     */
    void setOnOk(Action action);

    /**
     * Sets the action which should be performed when the Close button is pressed.
     * @param action The {@link Action} to perform.
     */
    void setOnClose(Action action);


    /**
     * This interface provides required methods to represent a selected desktop source.
     */
    interface SelectedDesktopSource {

        DesktopSource getSource();
        DesktopSourceType getType();
        BufferedImage getFrame();

    }
}
