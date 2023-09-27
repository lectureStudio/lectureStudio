package org.lecturestudio.editor.javafx.view;

import org.lecturestudio.editor.api.presenter.AudioEffectsPresenter;
import org.lecturestudio.editor.api.view.AudioEffectsView;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "audio-effects", presenter = AudioEffectsPresenter.class)
public class FxAudioEffectsView extends ContentPane implements AudioEffectsView {
    public FxAudioEffectsView() {
        super();
    }
}
