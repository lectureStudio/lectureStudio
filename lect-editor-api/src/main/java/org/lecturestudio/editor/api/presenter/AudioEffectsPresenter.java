package org.lecturestudio.editor.api.presenter;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.editor.api.view.AudioEffectsView;

public class AudioEffectsPresenter extends Presenter<AudioEffectsView> {
    @Inject
    public AudioEffectsPresenter(ApplicationContext context, AudioEffectsView view) {
        super(context, view);
    }
}
