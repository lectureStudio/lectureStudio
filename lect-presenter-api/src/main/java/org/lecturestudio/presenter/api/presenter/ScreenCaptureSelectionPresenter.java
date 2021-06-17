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
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.service.ScreenCaptureService;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.view.ScreenCaptureSourceSelectionView;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScreenCaptureSelectionPresenter extends Presenter<ScreenCaptureSourceSelectionView> implements ScreenCaptureService.DesktopSourceListListener {

    private final DocumentService documentService;
    private final ScreenCaptureService screenCaptureService;

    private List<DesktopSource> sources = new ArrayList<>();

    @Inject
    public ScreenCaptureSelectionPresenter(ApplicationContext context, ScreenCaptureSourceSelectionView view, DocumentService documentService, ScreenCaptureService screenCaptureService) {
        super(context, view);
        this.documentService = documentService;
        this.screenCaptureService = screenCaptureService;

        // Populate view with window sources once at startup
        // OnDesktopSourceListChange(capturer.getDesktopSources(), DesktopSourceType.WINDOW);
        // OnDesktopSourceListChange(service.getScreenSources(), DesktopSourceType.SCREEN);
    }

    @Override
    public void initialize() {
        view.setOnOk(this::confirmSelection);
        view.setOnClose(this::close);

        System.out.println("Initialize");

        screenCaptureService.removeSourceListListener(this);
        screenCaptureService.addSourceListListener(this);

        // onDesktopSourceListChange(screenCaptureService.getDesktopSources());
    }

    @Override
    public ViewLayer getViewLayer() {
        return ViewLayer.Notification;
    }


    private void confirmSelection() {
        DesktopSource selectedSource = view.getSelectedSource();
        if (selectedSource != null) {
            documentService.addScreenCapture(selectedSource).join();
        }
        close();
    }

    @Override
    public void onDesktopSourceListChange(List<DesktopSource> newSources) {
        // Find all source which were opened since the last update
        List<DesktopSource> sourcesToAdd = new ArrayList<>(newSources);
        sourcesToAdd.removeAll(sources);

        List<CompletableFuture<Void>> captureTasks = new ArrayList<>();

        // Add new sources to the view
        for (DesktopSource source : sourcesToAdd) {
            view.addDesktopSource(source, DesktopSourceType.WINDOW);
            screenCaptureService.addScreenCaptureListener(source, view::updateSourcePreviewImage);
            captureTasks.add(screenCaptureService.requestFrame(source));
        }

        // Find all sources which were closed since the last update
        List<DesktopSource> sourcesToRemove = new ArrayList<>(sources);
        sourcesToRemove.removeAll(newSources);

        // Remove old sources from the view
        for (DesktopSource source : sourcesToRemove) {
            view.removeDesktopSource(source, DesktopSourceType.WINDOW);
            screenCaptureService.removeScreenCaptureListener(source, view::updateSourcePreviewImage);
        }

        sources = newSources;

        // Wait for all capture tasks to complete
        CompletableFuture.allOf(captureTasks.toArray(new CompletableFuture[0])).join();
    }
}
