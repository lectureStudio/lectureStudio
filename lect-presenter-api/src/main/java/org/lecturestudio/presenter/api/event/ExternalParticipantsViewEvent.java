package org.lecturestudio.presenter.api.event;

public class ExternalParticipantsViewEvent extends ExternalViewEvent {

	public ExternalParticipantsViewEvent(boolean enabled) {
		super(enabled);
	}

	public ExternalParticipantsViewEvent(boolean enabled, boolean show) {
		super(enabled, show);
	}
}
