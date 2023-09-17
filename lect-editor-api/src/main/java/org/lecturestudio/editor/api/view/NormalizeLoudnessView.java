package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface NormalizeLoudnessView extends View {
    void setLUFSValue(Double lufsValue);

    void onSubmit(ConsumerAction<Double> action);

    void onClose(Action action);
}
