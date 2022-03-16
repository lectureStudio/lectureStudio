package org.lecturestudio.presenter.api.presenter.command;

import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.presenter.CreateQuizPresenter;

public class CreateQuizCommand extends ShowPresenterCommand<CreateQuizPresenter> {
	private final Action startAction;

	private final Action closeAction;

	public CreateQuizCommand(Action startAction, Action closeAction) {
		super(CreateQuizPresenter.class);

		this.startAction = startAction;
		this.closeAction = closeAction;
	}

	@Override
	public void execute(CreateQuizPresenter presenter) {
		presenter.setOnStartQuiz(startAction);
		presenter.setOnClose(closeAction);
	}
}
