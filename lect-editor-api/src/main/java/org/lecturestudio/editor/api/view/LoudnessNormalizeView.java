package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface LoudnessNormalizeView extends View {
    void setLUFSValue(Double lufsValue);

    void onSubmit(ConsumerAction<Double> action);
}
