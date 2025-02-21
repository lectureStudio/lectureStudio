package org.lecturestudio.presenter.api.event;

public class ExternalParticipantVideoViewEvent extends ExternalViewEvent {

	public ExternalParticipantVideoViewEvent(boolean enabled) {
		super(enabled);
	}

	public ExternalParticipantVideoViewEvent(boolean enabled, boolean show) {
		super(enabled, show);
	}
}
