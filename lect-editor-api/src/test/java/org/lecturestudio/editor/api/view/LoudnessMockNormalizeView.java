package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.view.ConsumerAction;

public class LoudnessMockNormalizeView implements LoudnessNormalizeView {
    public Double lufsValue;
    public ConsumerAction<Double> onSubmitAction;

    @Override
    public void setLUFSValue(Double lufsValue) {
        this.lufsValue = lufsValue;
    }

    @Override
    public void onSubmit(ConsumerAction<Double> action) {
        this.onSubmitAction = action;
    }
}
