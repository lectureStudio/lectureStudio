package org.lecturestudio.web.api.message;

public class CourseFeatureMessengerParticipantMessage extends WebMessage {

	private Boolean connected;


	/**
	 * Get whether the participant connected or disconnected to/from the current
	 * messenger session.
	 *
	 * @return True if connected.
	 */
	public Boolean getConnected() {
		return connected;
	}

	/**
	 * Set whether the participant connected or disconnected to/from the current
	 * messenger session.
	 *
	 * @param connected True if connected.
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}
