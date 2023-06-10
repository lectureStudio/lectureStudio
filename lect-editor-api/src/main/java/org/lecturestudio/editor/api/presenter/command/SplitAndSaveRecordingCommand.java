package org.lecturestudio.editor.api.presenter.command;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.editor.api.presenter.SplitRecordingPresenter;

public class SplitAndSaveRecordingCommand extends ShowPresenterCommand<SplitRecordingPresenter> {
	private final Interval<Integer> begin;
	private final Interval<Integer> end;

	/**
	 * Create a new {@link ShowPresenterCommand} with the specified presenter class.
	 */
	public SplitAndSaveRecordingCommand(Interval<Integer> begin, Interval<Integer> end) {
		super(SplitRecordingPresenter.class);
		this.begin = begin;
		this.end = end;
	}

	@Override
	public void execute(SplitRecordingPresenter presenter) {
		presenter.setIntervals(begin, end);
	}
}
