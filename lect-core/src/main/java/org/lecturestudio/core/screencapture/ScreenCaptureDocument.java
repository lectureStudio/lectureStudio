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
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.util.ScreenCaptureUtils;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * The ScreenCaptureDocument implements all methods required to handle a screen capture of a certain DesktopSource.
 */
public class ScreenCaptureDocument {

    private final DesktopSource source;
    private final DocumentRenderer renderer;

    private final List<BufferedImage> pageFrames = new ArrayList<>();

    private DocumentOutline outline;

    public ScreenCaptureDocument(DesktopSource source) {
        this.source = source;
        this.renderer = new ScreenCaptureRenderer();
    }

    public long getId() {
        return source.id;
    }

    public String getTitle() {
        return source.title;
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

    public int createPage(Dimension2D size) {
        int pageNumber = getPageCount();

        // Create empty image of requested size
        BufferedImage image = ScreenCaptureUtils.createBufferedImage((int) size.getWidth(), (int) size.getHeight());

        // Add image to pageFrames and request actual frame of page
        pageFrames.add(image);
        ScreenCaptureUtils.requestFrame(source, frame -> pageFrames.set(pageNumber, frame));

        return pageNumber;
    }

    /**
     * Imports a page of a given source document at the same index in the target document, if the page exists.
     * Otherwise, a new page will be appended to the target document.
     *
     * @return The index of the imported page at the target document or -1 if the page was not found in the source document.
     */
    public int importPage(ScreenCaptureDocument srcDocument, int pageNumber, Rectangle2D pageRect) {
        BufferedImage pageFrame = srcDocument.getPageFrame(pageNumber);
        if (pageFrame == null) {
            return -1;
        }

        // Transform page frame if rect was set
        if (nonNull(pageRect)) {
            AffineTransform transform = new AffineTransform();
            transform.translate(pageRect.getX(), pageRect.getY());
            transform.scale(1 / pageRect.getWidth(), 1 / pageRect.getHeight());
            pageFrame = transformBufferedImage(pageFrame, transform);
        }

        if (pageNumber < getPageCount()) {
            pageFrames.set(pageNumber, pageFrame);
        } else {
            pageFrames.add(pageFrame);
        }
        return getPageCount() - 1;
    }

    public BufferedImage transformBufferedImage(BufferedImage image, AffineTransform transform) {

        // TODO: Add transformation

        return image;
    }

    public void removePage(int pageIndex) {
        pageFrames.remove(pageIndex);
    }

    public void replacePage(int pageNumber, ScreenCaptureDocument srcDocument, int replacementPageNumber) {
        BufferedImage image = srcDocument.getPageFrame(replacementPageNumber);
        if (image != null && pageNumber < getPageCount()) {
            pageFrames.set(pageNumber, image);
        }
    }

    public BufferedImage getPageFrame(int pageNumber) {
        if (pageNumber < getPageCount()) {
            return pageFrames.get(pageNumber);
        }
        return null;
    }

    public int getPageCount() {
        return pageFrames.size();
    }

    public Rectangle2D getPageRect(int pageNumber) {
        BufferedImage frame = pageFrames.get(pageNumber);
        if (frame != null) {
            return new Rectangle2D(0, 0, frame.getWidth(), frame.getHeight());
        }
        return null;
    }

    public String getPageText(int pageNumber) {
        return "Page " + pageNumber + ": " + getTitle();
    }

    public void close() {}
}
