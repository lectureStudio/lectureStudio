package org.lecturestudio.editor.javafx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.converter.NumberStringConverter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.NormalizeLoudnessView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "audio-loudness")
public class FxNormalizeLoudnessView extends StackPane implements NormalizeLoudnessView {

    @FXML
    private Button cancelButton;

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

    @Override
    public void onClose(Action action) {
        FxUtils.bindAction(cancelButton, action);
    }
}
