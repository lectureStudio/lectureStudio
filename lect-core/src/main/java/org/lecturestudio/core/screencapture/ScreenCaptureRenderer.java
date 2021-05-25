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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.util.WindowUtils;
import org.lecturestudio.core.view.PresentationParameter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenCaptureRenderer implements DocumentRenderer {

    private static final Logger LOG = LogManager.getLogger(ScreenCaptureRenderer.class);

    private final Object lock = new Object();

    private final ScreenCaptureDocument document;

    public ScreenCaptureRenderer(ScreenCaptureDocument document) {
        this.document = document;
    }

    @Override
    public void render(Page page, PresentationParameter parameter, BufferedImage image) throws IOException {
        if (page.getDocument().isScreenCapture()) {
            synchronized (lock) {

                ScreenCapture screenCapture = document.getScreenCapture(page.getPageNumber());
                LOG.info("Render screen capture for window: " + screenCapture.getWindowInfo().getTitle());

                // TODO: Implement rendering of screen capture

                Graphics2D g = image.createGraphics();
                g.setColor(Color.WHITE);

                try {
                    // Create screenshot from window and draw it to graphic
                    image = WindowUtils.createScreenCapture(screenCapture.getWindowInfo().getHandle());
                    int imageWidth = image.getWidth();
                    int imageHeight = image.getHeight();

                    g.drawImage(image, 0, 0, imageWidth, imageHeight, null);
                } catch (AWTException e) {
                    e.printStackTrace();
                }

                g.dispose();
            }
        }
    }
}
