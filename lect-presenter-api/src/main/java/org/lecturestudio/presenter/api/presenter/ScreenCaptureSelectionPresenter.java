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

import com.google.common.eventbus.Subscribe;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.ScreenCaptureSourceEvent;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.service.ScreenCaptureService;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.view.ScreenCaptureSourceSelectionView;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a {@link Presenter} for the {@link ScreenCaptureSourceSelectionView} and handles the selection of new screen capture sources.
 */
public class ScreenCaptureSelectionPresenter extends Presenter<ScreenCaptureSourceSelectionView> implements ScreenCaptureService.ScreenCaptureCallback {

    private final EventBus eventBus;
    private final DocumentService documentService;
    private final ScreenCaptureService screenCaptureService;

    private List<DesktopSource> windowSources = new ArrayList<>();
    private List<DesktopSource> screenSources = new ArrayList<>();

    @Inject
    public ScreenCaptureSelectionPresenter(ApplicationContext context, ScreenCaptureSourceSelectionView view,
                    DocumentService documentService, ScreenCaptureService screenCaptureService) {
        super(context, view);
        this.documentService = documentService;
        this.screenCaptureService = screenCaptureService;
        this.eventBus = context.getEventBus();
    }

    @Override
    public void initialize() {
        view.setOnOk(this::confirmSelection);
        view.setOnClose(this::close);

        eventBus.register(this);

        updateDesktopSources(screenCaptureService.getDesktopSources(DesktopSourceType.WINDOW), DesktopSourceType.WINDOW);
        updateDesktopSources(screenCaptureService.getDesktopSources(DesktopSourceType.SCREEN), DesktopSourceType.SCREEN);
    }

    @Override
    public ViewLayer getViewLayer() {
        return ViewLayer.Dialog;
    }

    @Override
    public void destroy() {
        reset();
    }

    @Override
    public void onFrameCapture(DesktopSource source, BufferedImage image) {
        view.updateSourcePreviewImage(source, image);
    }

    @Subscribe
    public final void onEvent(ScreenCaptureSourceEvent event) {
        updateDesktopSources(event.getSources(), event.getType());
    }

    private void updateDesktopSources(List<DesktopSource> newSources, DesktopSourceType type) {
        List<DesktopSource> sources = (type == DesktopSourceType.WINDOW) ? windowSources : screenSources;

        // Find all source which were opened since the last update
        List<DesktopSource> sourcesToAdd = new ArrayList<>(newSources);
        sourcesToAdd.removeAll(sources);

        // Add new sources to the view
        for (DesktopSource source : sourcesToAdd) {
            view.addDesktopSource(source, type);
            screenCaptureService.requestFrame(source, type, this);
        }

        // Find all sources which were closed since the last update
        List<DesktopSource> sourcesToRemove = new ArrayList<>(sources);
        sourcesToRemove.removeAll(newSources);

        // Remove old sources from the view
        for (DesktopSource source : sourcesToRemove) {
            view.removeDesktopSource(source, type);
        }

        if (type == DesktopSourceType.WINDOW)
            windowSources = newSources;
        else
            screenSources = newSources;
    }

    private void confirmSelection() {
        ScreenCaptureSourceSelectionView.SelectedDesktopSource selectedSource = view.getSelectedDesktopSource();
        if (selectedSource != null) {
            BufferedImage frame = selectedSource.getFrame();
            documentService.addScreenCapture(selectedSource.getSource(), selectedSource.getType(), frame);
        }
        close();
    }

    private void reset() {
        eventBus.unregister(this);
        windowSources = null;
    }
}
