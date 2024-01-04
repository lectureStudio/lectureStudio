package org.lecturestudio.editor.api.presenter;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.LoudnessNormalizeView;

public class NormalizeLoudnessPresenter extends Presenter<LoudnessNormalizeView> {
    private final RecordingFileService recordingService;
    private final ViewContextFactory viewFactory;
    private DoubleProperty lufsValue;

    @Inject
    protected NormalizeLoudnessPresenter(ApplicationContext context, LoudnessNormalizeView view,
                                         RecordingFileService recordingService, ViewContextFactory viewFactory) {
        super(context, view);
        this.recordingService = recordingService;
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize() {
        lufsValue = new DoubleProperty(-14);
        lufsValue.addListener((observable, oldValue, newValue) -> {
            if (newValue > -5) {
                lufsValue.set(-5.);
            }
            else if (newValue < -70) {
                lufsValue.set(-70.);
            }
        });

        view.setLUFSValue(lufsValue.get());
        view.onSubmit(this::normalizeLoudness);
    }

    private void normalizeLoudness(Double lufsValue) {
        ProgressDialogView progressView = viewFactory.getInstance(ProgressDialogView.class);
        progressView.setMessageTitle(context.getDictionary().get("normalize.loudness.audio"));
        progressView.setParent(view);
        progressView.open();

        recordingService.normalizeAudioLoudness(lufsValue, progressView::setProgress)
                .thenCompose((ignored) -> {
                    progressView.setMessageTitle(context.getDictionary().get("normalize.loudness.audio.success"));
                    return CompletableFuture.completedFuture(null);
                })
                .exceptionally(throwable -> {
                    progressView.setError(context.getDictionary().get("normalize.loudness.audio.error"), throwable.getMessage());
                    return null;
                });
    }
}
