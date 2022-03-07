package org.lecturestudio.presenter.api.event;

import org.lecturestudio.presenter.api.model.MessageBarPosition;

public class MessageBarPositionEvent {
	public final MessageBarPosition position;

	public MessageBarPositionEvent(MessageBarPosition position) {
		this.position = position;
	}
}
