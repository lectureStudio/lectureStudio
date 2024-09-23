package org.lecturestudio.editor.api.presenter;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.ProgressPresenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.ImportRecordingView;

public class ImportRecordingPresenter extends Presenter<ImportRecordingView> {

	private final RecordingFileService recordingService;
	private final ViewContextFactory viewFactory;
	private BooleanProperty normalizeNewAudio;
	private File file;


	@Inject
	protected ImportRecordingPresenter(ApplicationContext context, ImportRecordingView view,
									   RecordingFileService recordingService, ViewContextFactory viewFactory) {
		super(context, view);
		this.recordingService = recordingService;
		this.viewFactory = viewFactory;
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void submit() {
		EditorContext editorContext = (EditorContext) context;

		context.getEventBus().post(new ShowPresenterCommand<>(ProgressPresenter.class) {
			@Override
			public void execute(ProgressPresenter presenter) {
				ProgressView progressView = presenter.getView();
				progressView.setTitle(context.getDictionary().get("import.recording"));
				progressView.setOnViewShown(() -> {
					recordingService.importRecording(file, editorContext.getPrimarySelection(), normalizeNewAudio.get(), progressView::setProgress)
						.thenCompose((ignored) -> {
							progressView.setTitle(context.getDictionary().get("import.recording.success"));
							return CompletableFuture.completedFuture(null);
						})
						.exceptionally(throwable -> {
							progressView.setError(MessageFormat.format(context.getDictionary().get("import.recording.error"),
									file.getPath()));
							return null;
						});
				});
			}
		});

		close();
	}

	@Override
	public void initialize() {
		normalizeNewAudio = new BooleanProperty(true);

		view.setOnSubmit(this::submit);
		view.setOnCancel(this::close);
		view.bindNormalize(normalizeNewAudio);

	}

	public void setFile(File file) {
		this.file = file;
	}
}
