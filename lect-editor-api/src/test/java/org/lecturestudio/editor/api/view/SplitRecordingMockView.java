package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;

public class SplitRecordingMockView implements SplitRecordingView {
	public Action onCloseAction;
	public ConsumerAction<Interval<Integer>> onSubmitAction;
	public Interval<Integer> beginning;
	public Interval<Integer> end;

	@Override
	public void setIntervals(Interval<Integer> beginning, Interval<Integer> end) {
		this.beginning = beginning;
		this.end = end;
	}

	@Override
	public void setOnSubmit(ConsumerAction<Interval<Integer>> action) {
		this.onSubmitAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		this.onCloseAction = action;
	}
}
