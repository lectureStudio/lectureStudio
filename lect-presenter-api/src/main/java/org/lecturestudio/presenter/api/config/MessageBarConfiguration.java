package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.presenter.api.model.MessageBarPosition;

public class MessageBarConfiguration {
	private final ObjectProperty<MessageBarPosition> messageBarPosition =
			new ObjectProperty<>(MessageBarPosition.BOTTOM);

	/**
	 * @return Message bar's position
	 */
	public MessageBarPosition getMessageBarPosition() {
		return messageBarPosition.get();
	}

	/**
	 * @param position Message bar's position
	 */
	public void setMessageBarPosition(MessageBarPosition position) {
		messageBarPosition.set(position);
	}
}
