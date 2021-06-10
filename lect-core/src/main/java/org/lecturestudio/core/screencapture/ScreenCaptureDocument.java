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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.util.ScreenCaptureUtils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * The ScreenCaptureDocument implements all methods required to handle a screen capture of a certain DesktopSource.
 */
public class ScreenCaptureDocument {

    private final DesktopSource source;
    private final DocumentRenderer renderer;

    private final Map<Integer, BufferedImage> pageFrames = new HashMap<>();

    private DocumentOutline outline;

    public ScreenCaptureDocument(DesktopSource source) {
        this.source = source;
        this.renderer = new ScreenCaptureRenderer(this);
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

        // captureFrame(pageNumber);

        BufferedImage image = ScreenCaptureUtils.createBufferedImage((int) size.getWidth(), (int) size.getHeight());
        pageFrames.put(pageNumber, image);

        ScreenCaptureUtils.requestFrame(source, frame -> {
            pageFrames.put(pageNumber, frame);
        });

        return pageNumber;
    }

    private void captureFrame(int pageNumber) {
        WindowCapturer capturer = new WindowCapturer();
        capturer.selectSource(source);
        capturer.start((result, frame) -> {
            if (result == DesktopCapturer.Result.SUCCESS) {
                BufferedImage image = ScreenCaptureUtils.convertFrame(frame, frame.frameSize.width, frame.frameSize.height);
                pageFrames.put(pageNumber, image);
                System.out.println("Captured frame (" + frame.frameSize.width + "x" + frame.frameSize.height + ") for: " + source.title);
            }
        });
        capturer.captureFrame();
    }

//    private int createPage(BufferedImage image) {
//        int pageNumber = getPageCount();
//        System.out.println("PC2: " + pageNumber);
//        pageFrames.put(pageNumber, image);
//        return pageNumber;
//    }

    public int importPage(ScreenCaptureDocument srcDocument, int pageNumber, Rectangle2D pageRect) {
        BufferedImage previewImage = srcDocument.getPageFrame(pageNumber);
        pageFrames.put(pageNumber, previewImage);
        return pageNumber;
    }

    public void removePage(int pageNumber) {
        pageFrames.remove(pageNumber);
    }

    public void replacePage(int pageNumber, ScreenCaptureDocument srcDocument, int replacementPageNumber) {
        BufferedImage image = srcDocument.getPageFrame(replacementPageNumber);
        if (image != null) {
            pageFrames.put(pageNumber, image);
        }
    }

    public BufferedImage getPageFrame(int pageNumber) {
        return pageFrames.get(pageNumber);
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
        return getTitle();
    }

    public void close() {}
}
