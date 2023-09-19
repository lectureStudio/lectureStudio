package org.lecturestudio.editor.javafx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.converter.NumberStringConverter;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.LoudnessNormalizeView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "loudness-normalize", presenter = org.lecturestudio.editor.api.presenter.NormalizeLoudnessPresenter.class)
public class FxLoudnessNormalizeView extends StackPane implements LoudnessNormalizeView {

    @FXML
    private Button submitButton;

    @FXML
    private TextField lufsValueTextField;

    @Override
    public void setLUFSValue(Double lufsValue) {
        lufsValueTextField.setText(String.format("%1$,.2f", lufsValue));
    }

    @Override
    public void onSubmit(ConsumerAction<Double> action) {
        FxUtils.bindAction(submitButton, () -> action.execute(new NumberStringConverter<>().from(lufsValueTextField.getText()).doubleValue()));
    }
}
