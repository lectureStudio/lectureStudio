package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.nonNull;

import javax.inject.Inject;

import java.io.File;
import java.nio.file.Path;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.SplitRecordingView;

public class SplitRecordingPresenter extends Presenter<SplitRecordingView> {
	private final RecordingFileService recordingService;
	private final ViewContextFactory viewFactory;

	@Inject
	protected SplitRecordingPresenter(ApplicationContext context, SplitRecordingView view,
	                                  RecordingFileService recordingService, ViewContextFactory viewFactory) {
		super(context, view);
		this.recordingService = recordingService;
		this.viewFactory = viewFactory;
	}

	@Override
	public void initialize() {
		view.setOnSubmit(this::onSubmit);
		view.setOnClose(this::close);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void onSubmit(Interval<Integer> interval) {
		final String pathContext = EditorContext.RECORDING_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		String title = FileUtils.stripExtension(recordingService
				.getSelectedRecording().getSourceFile().getName());
		String fileName = title + "-part";
		int counter = 0;
		String fullFileName;

		do {
			fullFileName = fileName + "-" + ++counter + "." + EditorContext.RECORDING_EXTENSION;
		} while (new File(dirPath.toString(), fullFileName).exists());

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter(dict.get("file.description.recording"),
				EditorContext.RECORDING_EXTENSION);
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.setInitialFileName(fullFileName);

		File file = fileChooser.showSaveFile(view);

		if (nonNull(file)) {
			config.getContextPaths().put(EditorContext.RECORDING_CONTEXT,
					file.getParent());

			ProgressDialogView progressView = viewFactory.getInstance(ProgressDialogView.class);
			progressView.setMessageTitle(context.getDictionary().get("save.recording"));
			progressView.setParent(view);
			progressView.open();

			try {
				recordingService.savePartialRecording(file, interval, progressView::setProgress)
						.thenRun(() -> {
							progressView.setMessageTitle(context.getDictionary().get("save.recording.success"));
						})
						.exceptionally(throwable -> {
							handleException(throwable, "Save recording failed", "save.recording.error", file.getPath());
							return null;
						});
			}
			catch (RecordingEditException e) {
				handleException(e, "Save recording failed", "save.recording.error", file.getPath());
			}
		}
		close();
	}

	public void setIntervals(Interval<Integer> begin, Interval<Integer> end) {
		view.setIntervals(begin, end);
	}
}
