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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.service.ScreenCaptureService;
import org.lecturestudio.presenter.api.view.ScreenCaptureSourceSelectionView;

import javax.inject.Inject;
import java.util.List;

public class ScreenCaptureSelectionPresenter extends Presenter<ScreenCaptureSourceSelectionView> implements ScreenCaptureService.ScreenCaptureSourceListChangeListener {

    private final ScreenCaptureService service;

    @Inject
    public ScreenCaptureSelectionPresenter(ApplicationContext context, ScreenCaptureSourceSelectionView view, ScreenCaptureService service) {
        super(context, view);
        this.service = service;

        // Populate view with window sources once at startup
        OnDesktopSourceListChange(service.getWindowSources(), DesktopSourceType.WINDOW);
        // OnDesktopSourceListChange(service.getScreenSources(), DesktopSourceType.SCREEN);
    }

    @Override
    public void initialize() {
        service.registerSourceListChangeListener(this);

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
            service.captureFrame(source, (result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    view.updateSourcePreviewFrame(source, frame);
                }
            });
        }
    }

    private void confirmSelection() {
        DesktopSource selectedSource = view.getSelectedSource();
        if (selectedSource != null) {
            service.setSelectedSource(selectedSource);
        }
        close();
    }
}
