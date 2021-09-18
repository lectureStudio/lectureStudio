package org.lecturestudio.presenter.api.event;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.event.ExecutableEvent;

/**
 * Class which represents the messenger events relating to the dlz
 *
 * @author Daniel Schröter
 */
public class DLZStateEvent extends ExecutableEvent {

    public DLZStateEvent(ExecutableState state) {
        super(state);
    }

}