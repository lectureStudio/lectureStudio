package org.lecturestudio.editor.javafx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.view.ImportRecordingView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "recording-import", presenter = org.lecturestudio.editor.api.presenter.ImportRecordingPresenter.class)
public class FxImportRecordingView extends StackPane implements ImportRecordingView {

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox normalizeNewAudioCheckBox;

    @Inject
    public FxImportRecordingView() {
        super();
    }

    @Override
    public void setOnSubmit(Action action) {
        FxUtils.bindAction(submitButton, action);
    }

    @Override
    public void setOnCancel(Action action) {
        FxUtils.bindAction(cancelButton, action);
    }

    @Override
    public void bindNormalize(BooleanProperty normalizeNewAudio) {
        normalizeNewAudioCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(normalizeNewAudio));
    }
}
