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
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.service.ScreenCaptureService;
import org.lecturestudio.core.util.ImageUtils;
import org.lecturestudio.core.util.ScreenCaptureUtils;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * The ScreenCaptureDocument implements all methods required to handle a screen capture of a certain DesktopSource.
 */
public class ScreenCaptureDocument implements ScreenCaptureService.ScreenCaptureCallback {

    private final DesktopSource source;
    private final DesktopSourceType type;
    private final DocumentRenderer renderer;

    private final List<PageFrameListener> listeners = new ArrayList<>();
    private final List<ScreenCapturePage> pages = new ArrayList<>();

    private DocumentOutline outline;

    public ScreenCaptureDocument(byte[] byteArray) {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(bis);
            Object object = objectInputStream.readObject();

            if (object instanceof ScreenCaptureDocument) {
                ScreenCaptureDocument screenCaptureDocument = (ScreenCaptureDocument) object;
                source = screenCaptureDocument.getSource();
                type = screenCaptureDocument.getType();
                renderer = screenCaptureDocument.renderer;

                listeners.addAll(screenCaptureDocument.listeners);
                pages.addAll(screenCaptureDocument.pages);

                outline = screenCaptureDocument.outline;
                return;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Failed to serialize ScreenCaptureDocument from byte array.");
    }

    public ScreenCaptureDocument(DesktopSource source, DesktopSourceType type) {
        this.source = source;
        this.type = type;
        this.renderer = new ScreenCaptureRenderer();
    }

    public long getId() {
        return source.id;
    }

    public String getTitle() {
        return source.title;
    }

    public DesktopSource getSource() {
        return source;
    }

    public DesktopSourceType getType() {
        return type;
    }

    public DocumentRenderer getDocumentRenderer() {
        return renderer;
    }

    public DocumentOutline getDocumentOutline() {
        if (isNull(outline)) {
            outline = new DocumentOutline();
        }
        return outline;
    }

    public void addPageFrameListener(PageFrameListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public int createPage(Dimension2D size) {
        int pageNumber = getPageCount();

        // Create empty image of requested size
        ScreenCapturePage page = new ScreenCapturePage(size, pageNumber);
        BufferedImage image = ImageUtils.createBufferedImage((int) size.getWidth(), (int) size.getHeight());
        page.setImage(image);
        pages.add(page);

        // Add image to pageFrames and request actual frame of page
//        pageFrames.add(image);

        ScreenCaptureUtils.requestFrame(source, type, frame -> {
            try {
                BufferedImage scaledFrame = ImageUtils.cropAndScale(frame, (int) size.getWidth(), (int) size.getHeight());
                page.setImage(scaledFrame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
//        if (nonNull(pageRect)) {
//            AffineTransform transform = new AffineTransform();
//            transform.translate(pageRect.getX(), pageRect.getY());
//            transform.scale(1 / pageRect.getWidth(), 1 / pageRect.getHeight());
//            pageFrame = transformBufferedImage(pageFrame, transform);
//        }

        if (pageNumber < getPageCount()) {
            pages.get(pageNumber).setImage(pageFrame);
        } else {
            pages.add(new ScreenCapturePage(new Dimension2D(pageRect.getWidth(), pageRect.getHeight()), pageNumber));
        }
        return getPageCount() - 1;
    }

    public BufferedImage transformBufferedImage(BufferedImage image, AffineTransform transform) {

        // TODO: Add transformation

        return image;
    }

    public void removePage(int pageIndex) {
        pages.remove(pageIndex);
    }

    public void replacePage(int pageNumber, ScreenCaptureDocument srcDocument, int replacementPageNumber) {
        if (pageNumber < getPageCount()) {
            BufferedImage image = srcDocument.getPageFrame(replacementPageNumber);
            if (image != null && pageNumber < getPageCount()) {
                pages.get(pageNumber).setImage(image);
            }
        }
    }

    public BufferedImage getPageFrame(int pageNumber) {
        if (pageNumber < getPageCount()) {
            return pages.get(pageNumber).getImage();
        }
        return null;
    }

    public int getPageCount() {
        return pages.size();
    }

    public Rectangle2D getPageRect(int pageNumber) {
        if (pageNumber < getPageCount()) {
            BufferedImage frame = pages.get(pageNumber).getImage();
            return new Rectangle2D(0, 0, frame.getWidth(), frame.getHeight());
        }
        return null;
    }

    public String getPageText(int pageNumber) {
        return "Page " + pageNumber + ": " + getTitle();
    }

    public void close() {}

    public void toOutputStream(OutputStream stream) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(stream);
            outputStream.writeObject(this);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrameCapture(DesktopSource source, BufferedImage frame) {
        try {
            BufferedImage croppedFrame = ImageUtils.cropAndScale(frame, 640, 480);
            for (ScreenCapturePage page : pages) {
                notifyListeners(page.getPageNumber(), croppedFrame);
                page.setImage(croppedFrame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Frame Captured!!!");
    }

    private void notifyListeners(int pageNumber, BufferedImage frame) {
        for (PageFrameListener listener : listeners) {
            listener.onPageFrameChange(pageNumber, frame);
        }
    }



    public interface PageFrameListener {
        void onPageFrameChange(int pageNumber, BufferedImage frame);
    }



    private static class ScreenCapturePage {

        private final Dimension2D pageSize;
        private final int pageNumber;
        private BufferedImage image;

        public ScreenCapturePage(Dimension2D pageSize, int pageNumber) {
            this.pageSize = pageSize;
            this.pageNumber = pageNumber;
        }

        public Dimension2D getPageSize() {
            return pageSize;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public BufferedImage getImage() {
            return image;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }
    }
}
