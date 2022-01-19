package org.lecturestudio.presenter.api.event;

public abstract class ExternalViewEvent {
    private final boolean enabled;

    private final boolean show;

    private final boolean persistent;

    public ExternalViewEvent(boolean enabled) {
        this.enabled = enabled;
        this.show = true;
        this.persistent = true;
    }

    public ExternalViewEvent(boolean enabled, boolean show) {
        this.enabled = enabled;
        this.show = show;
        this.persistent = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isShow() {
        return show;
    }

    public boolean isPersistent() {
        return persistent;
    }
}
