package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface SplitRecordingView extends View {
	void setIntervals(Interval<Long> beginning, Interval<Long> end);

	void setOnSubmit(ConsumerAction<Interval<Long>> action);

	void setOnClose(Action action);
}
