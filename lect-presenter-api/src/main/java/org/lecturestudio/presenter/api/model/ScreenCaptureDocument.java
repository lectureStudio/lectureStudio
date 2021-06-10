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

package org.lecturestudio.presenter.api.model;

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.pdf.DocumentRenderer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ScreenCaptureDocument extends Document {

    private final List<ScreenCapture> captures = new ArrayList<>();

    private final ScreenCaptureDocumentRenderer documentRenderer;

    public ScreenCaptureDocument(DesktopSource source) throws IOException {
        documentRenderer = new ScreenCaptureDocumentRenderer();
        initScreenCapture(source);
    }

    @Override
    public String getTitle() {
        return "Screen Captures";
    }

    @Override
    public Rectangle2D getPageRect(int pageIndex) {
        if (captures.size() > pageIndex) {
            ScreenCapture capture = captures.get(pageIndex);

            // TODO: Get actual size from screen capture

            return new Rectangle2D(0, 0, 250, 150);
        }
        return null;
    }

    @Override
    public String getPageText(int pageIndex) {
        if (captures.size() > pageIndex) {
            ScreenCapture capture = captures.get(pageIndex);
            return capture.getTitle();
        }
        return null;
    }

    @Override
    public Page getPage(int index) {
        if (index >= 0 && getPageCount() > index) {
            return captures.get(index);
        }
        return null;
    }

    @Override
    public int getPageCount() {
        return captures.size();
    }

    @Override
    public Page createPage() {
        int pageIndex = getPageCount();

        ScreenCapture capture = new ScreenCapture(this, pageIndex);
        addPage(capture);

        return capture;
    }

    @Override
    protected void addPage(Page page) {
        if (page instanceof ScreenCapture) {
            captures.add((ScreenCapture) page);
            fireAddChange(page);
        }
    }

    @Override
    protected synchronized Page importPage(Page page, Rectangle2D pageRect) throws IOException {
        if (page instanceof ScreenCapture) {
            ScreenCapture capture = (ScreenCapture) page;
            int pageIndex = page.getPageNumber();

            if (pageIndex < 0) {
                return null;
            }

            ScreenCapture newCapture = new ScreenCapture(this, pageIndex);
            newCapture.setSource(capture.getSource());

            addPage(newCapture);
            return newCapture;
        }
        return null;
    }

    @Override
    public List<URI> getUriActions(int pageIndex) {
        // TODO: Investigate whether uri actions are required?
        return new ArrayList<>();
    }

    @Override
    public List<File> getLaunchActions(int pageIndex) {
        // TODO: Investigate whether launch actions are required?
        return new ArrayList<>();
    }

    @Override
    public DocumentRenderer getDocumentRenderer() {
        return documentRenderer;
    }

    public void selectScreenCapture(DesktopSource source) {
        ScreenCapture capture = (ScreenCapture) createPage();
        capture.setSource(source);
        selectPage(capture.getPageNumber());
    }

    private void initScreenCapture(DesktopSource source) {
        ScreenCapture capture = (ScreenCapture) createPage();
        capture.setSource(source);
    }
}
