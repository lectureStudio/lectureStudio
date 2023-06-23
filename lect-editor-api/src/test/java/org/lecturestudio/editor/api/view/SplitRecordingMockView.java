package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;

public class SplitRecordingMockView implements SplitRecordingView {
	public Action onCloseAction;
	public ConsumerAction<Interval<Long>> onSubmitAction;
	public Interval<Long> beginning;
	public Interval<Long> end;

	@Override
	public void setIntervals(Interval<Long> beginning, Interval<Long> end) {
		this.beginning = beginning;
		this.end = end;
	}

	@Override
	public void setOnSubmit(ConsumerAction<Interval<Long>> action) {
		this.onSubmitAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		this.onCloseAction = action;
	}
}
