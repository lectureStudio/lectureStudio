package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.nonNull;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.ProgressPresenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ProgressView;
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

	/**
	 * Opens the file chooser and preselects a filename with -part-{n} appended to the filename.
	 * Saves the selected interval into a new file and removes it from the recording
	 *
	 * @param interval The interval to be cut and saved into a separate recording
	 */
	private void onSubmit(Interval<Long> interval) {
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

			context.getEventBus().post(new ShowPresenterCommand<>(ProgressPresenter.class) {
				@Override
				public void execute(ProgressPresenter presenter) {
					ProgressView progressView = presenter.getView();
					progressView.setTitle(context.getDictionary().get("save.recording"));
					progressView.setOnViewShown(() -> {
						try {
							recordingService.savePartialRecording(file, interval, progressView::setProgress)
									.thenRun(() -> {
										progressView.setTitle(context.getDictionary().get("save.recording.success"));
									})
									.thenRun(() -> {
										// Set slider position.
										double pos = interval.getStart() == 0 ? 0 : 1.0;

										EditorContext editorContext = (EditorContext) context;
										editorContext.setPrimarySelection(pos);
									})
									.exceptionally(throwable -> {
										progressView.setError(MessageFormat.format(context.getDictionary().get("save.recording.error"),
												file.getPath()));
										return null;
									});
						}
						catch (RecordingEditException e) {
							progressView.setError(MessageFormat.format(context.getDictionary().get("save.recording.error"),
									file.getPath()));
						}
					});
				}
			});
		}

		close();
	}

	/**
	 * Sets the Intervals that can be selected.
	 *
	 * @param begin The first interval
	 * @param end   The second interval
	 */
	public void setIntervals(Interval<Long> begin, Interval<Long> end) {
		view.setIntervals(begin, end);
	}
}
