package org.lecturestudio.presenter.api.event;

public class ExternalSpeechViewEvent extends ExternalViewEvent {

	public ExternalSpeechViewEvent(boolean enabled) {
		super(enabled);
	}

	public ExternalSpeechViewEvent(boolean enabled, boolean show) {
		super(enabled, show);
	}
}
