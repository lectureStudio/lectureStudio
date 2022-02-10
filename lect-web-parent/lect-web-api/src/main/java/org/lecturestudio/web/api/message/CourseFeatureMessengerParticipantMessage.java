package org.lecturestudio.web.api.message;

public class CourseFeatureMessengerParticipantMessage {

    private String firstName, familyName;
    private String username;

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
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
}
