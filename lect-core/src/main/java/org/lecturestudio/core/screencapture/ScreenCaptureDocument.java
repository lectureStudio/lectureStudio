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

package org.lecturestudio.core.screencapture;

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.util.WindowInfo;
import org.lecturestudio.core.util.WindowUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class ScreenCaptureDocument {

    private final DocumentRenderer renderer;

    private final List<ScreenCapture> screenCaptures = new ArrayList<>();

    private Map<Long, DesktopSource> captureSources;

    private DocumentOutline outline;

    public ScreenCaptureDocument() {
        renderer = new ScreenCaptureRenderer(this);
    }

    public DocumentRenderer getDocumentRenderer() {
        return renderer;
    }

    public DocumentOutline getDocumentOutline() {
        if (isNull(outline)) {
            outline = new DocumentOutline();
            outline.getChildren().add(new DocumentOutlineItem("test1", 0));
            outline.getChildren().add(new DocumentOutlineItem("test2", 1));

            // TODO: Load outline
        }
        return outline;
    }

    public int addDesktopSource(DesktopSource source) {
        return 0;
    }

    public int createScreenCapture() {
        List<WindowInfo> activeWindows = WindowUtils.listActiveWindows();

        if (activeWindows.size() > 0) {
            WindowInfo window = activeWindows.get(0);
            ScreenCapture screenCapture = new ScreenCapture(window);
            screenCaptures.add(screenCapture);
        }
        return screenCaptures.size() - 1;
    }

    public ScreenCapture getScreenCapture(int index) {
        return screenCaptures.get(index);
    }

    public Rectangle2D getScreenCaptureRect(int screenCaptureIndex) {
        ScreenCapture capture = screenCaptures.get(screenCaptureIndex);
        Rectangle windowBounds = capture.getWindowInfo().getBounds();
        return new Rectangle2D(windowBounds.x, windowBounds.y, windowBounds.width, windowBounds.height);
    }

    public String getScreenCaptureText(int screenCaptureIndex) {
        ScreenCapture capture = screenCaptures.get(screenCaptureIndex);
        return capture.getWindowInfo().getTitle();
    }

    public int importScreenCapture(ScreenCaptureDocument srcDocument, int screenCaptureIndex, Rectangle2D pageRect) {
        // TODO: Implement import of new screen capture

        return screenCaptures.size() - 1;
    }

    public int getScreenCaptureCount() {
        return screenCaptures.size();
    }

    public void close() {

    }
}
