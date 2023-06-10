package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface SplitRecordingView extends View {
	void setIntervals(Interval<Integer> beginning, Interval<Integer> end);

	void setOnSubmit(ConsumerAction<Interval<Integer>> action);

	void setOnClose(Action action);
}
