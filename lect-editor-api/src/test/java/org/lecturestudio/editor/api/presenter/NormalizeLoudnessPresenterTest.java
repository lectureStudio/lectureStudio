package org.lecturestudio.editor.api.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import javax.inject.Singleton;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.LoudnessMockNormalizeView;
import org.lecturestudio.editor.api.view.LoudnessNormalizeView;
import org.lecturestudio.editor.api.view.ProgressDialogMockView;

public class NormalizeLoudnessPresenterTest extends PresenterTest {

    private LoudnessMockNormalizeView normalizeLoudnessMockView;
    private RecordingFileService recordingService;
    private NormalizeLoudnessPresenter normalizeLoudnessPresenter;

    private ProgressDialogMockView progressDialogMockView;
    private NotificationMockView notificationView;

    @BeforeEach
    @Override
    void setupInjector() throws Exception {
        normalizeLoudnessMockView = new LoudnessMockNormalizeView();
        progressDialogMockView = new ProgressDialogMockView();
        notificationView = new NotificationMockView();

        injector = new GuiceInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ViewContextFactory.class).to(DIViewContextFactory.class);
            }

            @Provides
            @Singleton
            ApplicationContext provideApplicationContext() {
                return context;
            }

            @Provides
            @Singleton
            AudioSystemProvider provideAudioSystemProvider() {
                return audioSystemProvider;
            }

            @Provides
            @Singleton
            LoudnessNormalizeView provideNormalizeLoudnessMockView() {
                return normalizeLoudnessMockView;
            }

            @Provides
            @Singleton
            ProgressDialogView provideProgressDialogView() {
                return progressDialogMockView;
            }

            @Provides
            @Singleton
            NotificationView provideNotificationView() {
                return notificationView;
            }

        });

        recordingService = injector.getInstance(RecordingFileService.class);
        String recordingPath = getResourcePath("empty_pages_recording.presenter").toString();
        recordingService.openRecording(new File(recordingPath)).get();

        normalizeLoudnessPresenter = injector.getInstance(NormalizeLoudnessPresenter.class);
        normalizeLoudnessPresenter.initialize();

        context.getConfiguration().getContextPaths().put(EditorContext.RECORDING_CONTEXT, testPath.toString());
    }

    @Test
    public void testSetLUFSValue() {
        assertEquals(-14, normalizeLoudnessMockView.lufsValue);
    }

    @Test
    public void testNormalizeLoudness() {
        RandomAccessAudioStream audioStreamBefore = recordingService.getSelectedRecording().getRecordedAudio().getAudioStream().clone();
        long lengthBefore = audioStreamBefore.getLengthInMillis();
        long sampleRateBefore = audioStreamBefore.getAudioFormat().getSampleRate();

        normalizeLoudnessMockView.onSubmitAction.execute(normalizeLoudnessMockView.lufsValue);
        try {
            awaitTrue(() -> 1 == progressDialogMockView.progress, 60);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(lengthBefore, recordingService.getSelectedRecording().getRecordedAudio().getAudioStream().getLengthInMillis());
        assertEquals(sampleRateBefore, recordingService.getSelectedRecording().getRecordedAudio().getAudioStream().getAudioFormat().getSampleRate());
        assertEquals(1, progressDialogMockView.progress);
    }
}