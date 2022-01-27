package org.lecturestudio.presenter.api.event;

public class ExternalMessagesViewEvent extends ExternalViewEvent {
	public ExternalMessagesViewEvent(boolean enabled) {
		super(enabled);
	}

	public ExternalMessagesViewEvent(boolean enabled, boolean show) {
		super(enabled, show);
	}
}
