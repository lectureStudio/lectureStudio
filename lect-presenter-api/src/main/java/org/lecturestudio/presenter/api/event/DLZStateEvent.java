package org.lecturestudio.presenter.api.event;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.event.ExecutableEvent;

public class DLZStateEvent extends ExecutableEvent {

    public DLZStateEvent(ExecutableState state) {
        super(state);
    }

}