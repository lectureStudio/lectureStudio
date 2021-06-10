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

package org.lecturestudio.presenter.api.presenter;

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.ScreenCaptureUtils;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.service.ScreenCaptureService;
import org.lecturestudio.presenter.api.view.ScreenCaptureSourceSelectionView;

import javax.inject.Inject;
import java.util.List;

public class ScreenCaptureSelectionPresenter extends Presenter<ScreenCaptureSourceSelectionView> implements ScreenCaptureService.ScreenCaptureSourceListChangeListener {

    private final DocumentService documentService;

    @Inject
    public ScreenCaptureSelectionPresenter(ApplicationContext context, ScreenCaptureSourceSelectionView view, DocumentService documentService) {
        super(context, view);
        this.documentService = documentService;

        WindowCapturer capturer = new WindowCapturer();

        // Populate view with window sources once at startup
        OnDesktopSourceListChange(capturer.getDesktopSources(), DesktopSourceType.WINDOW);
        // OnDesktopSourceListChange(service.getScreenSources(), DesktopSourceType.SCREEN);
    }

    @Override
    public void initialize() {
        view.setOnOk(this::confirmSelection);
        view.setOnClose(this::close);
    }

    @Override
    public ViewLayer getViewLayer() {
        return ViewLayer.Notification;
    }

    @Override
    public void OnDesktopSourceListChange(List<DesktopSource> sources, DesktopSourceType type) {
        for (DesktopSource source : sources) {
            view.addDesktopSource(source, type);

            // TODO: Find a way to capture preview frames asynchronous
            ScreenCaptureUtils.requestFrame(source, image -> view.updateSourcePreviewImage(source, image));
        }
    }

    private void confirmSelection() {
        DesktopSource selectedSource = view.getSelectedSource();
        if (selectedSource != null) {
            documentService.addScreenCapture(selectedSource).join();
        }
        close();
    }
}
