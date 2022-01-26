package org.lecturestudio.web.api.model.messenger;

public class MessengerConfig {

    public enum MessengerMode {
        BIDIRECTIONAL, UNIDIRECTIONAL
    }

    private final MessengerMode messengerMode;

    public MessengerConfig(MessengerMode mode) {
        this.messengerMode = mode;
    }

    public MessengerMode getMessengerMode() {
        return messengerMode;
    }
}
