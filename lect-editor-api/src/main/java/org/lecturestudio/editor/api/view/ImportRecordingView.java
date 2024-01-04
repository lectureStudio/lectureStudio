package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;

public interface ImportRecordingView extends View {
    void setOnSubmit(Action action);

    void setOnCancel(Action action);

    void bindNormalize(BooleanProperty normalizeNewAudio);
}
