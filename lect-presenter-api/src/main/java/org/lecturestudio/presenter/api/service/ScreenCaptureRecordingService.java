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

package org.lecturestudio.presenter.api.service;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.screencapture.ScreenCaptureRecorder;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.ScreenCaptureRecordingStateEvent;
import org.lecturestudio.presenter.api.recording.RecordingBackup;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

import static java.util.Objects.nonNull;

@Singleton
public class ScreenCaptureRecordingService extends ExecutableBase {

    private final ApplicationContext context;

    private RecordingBackup backup;
    private ScreenCaptureRecorder recorder;

    @Inject
    public ScreenCaptureRecordingService(ApplicationContext context) {
        this.context = context;

        if (context instanceof PresenterContext) {
            PresenterContext presenterContext = (PresenterContext) context;

            try {
                backup = new RecordingBackup(presenterContext.getRecordingDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("PresenterContext required to initialize ScreenCaptureRecordingService.");
        }
    }

    @Override
    protected void initInternal() {}

    @Override
    protected void startInternal() {
        ExecutableState prevState = getPreviousState();

        if (prevState == ExecutableState.Initialized || prevState == ExecutableState.Stopped) {
            System.out.println("Start Screen Capture");

            backup.open();

            try {
                // Stop previously running screen capture recorder if exists
                if (nonNull(recorder)) {
                    recorder.stop();
                }

                recorder = new ScreenCaptureRecorder(new File(backup.getScreenCaptureFile()));
                recorder.setScreenCaptureFormat(context.getConfiguration().getScreenCaptureConfig().getRecordingFormat());

                // Configure desktop source for recorder if currently selected document is screen capture
                Document document = context.getDocumentService().getDocuments().getSelectedDocument();
                if (document != null && document.isScreenCapture()) {
                    recorder.setActiveSource(document.getScreenCaptureDocument().getSource(), document.getScreenCaptureDocument().getType());
                }

                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (prevState == ExecutableState.Suspended) {
            System.out.println("Continue Screen Capture");

            try {
                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void suspendInternal(){
        if (getPreviousState() == ExecutableState.Started) {
            System.out.println("Suspend Screen Capture");

            recorder.pause();
        }
    }

    @Override
    protected void stopInternal() {
        System.out.println("Stop Screen Capture");

        try {
            recorder.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

        backup.close();
    }

    @Override
    protected void destroyInternal() {}

    @Override
    protected void fireStateChanged() {
        // Notify about change is screen capture recording state
        ApplicationBus.post(new ScreenCaptureRecordingStateEvent(getState()));
    }
}
