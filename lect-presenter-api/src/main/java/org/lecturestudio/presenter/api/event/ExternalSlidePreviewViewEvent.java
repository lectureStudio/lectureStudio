package org.lecturestudio.presenter.api.event;

public class ExternalSlidePreviewViewEvent extends ExternalViewEvent {
    public ExternalSlidePreviewViewEvent(boolean enabled) {
        super(enabled);
    }

    public ExternalSlidePreviewViewEvent(boolean enabled, boolean show) {
        super(enabled, show);
    }
}
