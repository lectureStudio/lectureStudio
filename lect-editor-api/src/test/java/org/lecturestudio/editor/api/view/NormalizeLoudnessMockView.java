package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;

public class NormalizeLoudnessMockView implements NormalizeLoudnessView {
    public Double lufsValue;
    public ConsumerAction<Double> onSubmitAction;
    public Action onCloseAction;

    @Override
    public void setLUFSValue(Double lufsValue) {
        this.lufsValue = lufsValue;
    }

    @Override
    public void onSubmit(ConsumerAction<Double> action) {
        this.onSubmitAction = action;
    }

    @Override
    public void onClose(Action action) {
        this.onCloseAction = action;
    }
}
