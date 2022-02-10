package org.lecturestudio.web.api.message;

public class CourseFeatureMessengerParticipantMessage extends WebMessage {

    private boolean connected;


    /**
     * Get whether the participant connected or disconnected to/from the current
     * messenger session.
     *
     * @return True if connected.
     */
    public boolean getConnected() {
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

    public String getUsername() {
        return getRemoteAddress();
    }

    public void setUsername(String username) {
        setRemoteAddress(username);
    }

    public String getFirstName() {
        return getFirstName();
    }

    public String getFamilyName() {
        return getFamilyName();
    }

    public void setFirstName(String firstName) {
        setFirstName(firstName);
    }

    public void setFamilyName(String familyName) {
        setFamilyName(familyName);
    }
}
