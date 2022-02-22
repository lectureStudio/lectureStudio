package org.lecturestudio.web.api.model;

public class UserConnectionState {

    private boolean streamConnected;

    private boolean messengerConnected;

    public UserConnectionState(boolean streamConnected, boolean messengerConnected) {
        this.streamConnected = streamConnected;
        this.messengerConnected = messengerConnected;
    }

    public boolean isStreamConnected() {
        return streamConnected;
    }

    public boolean isMessengerConnected() {
        return messengerConnected;
    }

    public boolean isfullyConnected() {
        return streamConnected && messengerConnected;
    }

    public boolean isNotConnected() {
        return !streamConnected && !messengerConnected;
    }

    public void setStreamConnected(boolean streamConnected) {
        this.streamConnected = streamConnected;
    }

    public void setMessengerConnected(boolean messengerConnected) {
        this.messengerConnected = messengerConnected;
    }
}
