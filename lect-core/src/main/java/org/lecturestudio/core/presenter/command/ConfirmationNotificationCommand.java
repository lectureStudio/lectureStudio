package org.lecturestudio.core.presenter.command;

import org.lecturestudio.core.presenter.ConfirmationNotificationPresenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;


public class ConfirmationNotificationCommand extends ShowPresenterCommand<ConfirmationNotificationPresenter> {
	private final NotificationType type;
	private final String title;
	private final String message;
	private final Action confirmAction;
	private final String confirmButtonText;
	private final String closeButtonText;
	private final Action discardAction;

	public ConfirmationNotificationCommand(NotificationType type, String title, String message, Action confirmAction, Action discardAction, String confirmButtonText, String closeButtonText) {
		super(ConfirmationNotificationPresenter.class);

		this.type = type;
		this.title = title;
		this.message = message;
		this.confirmAction = confirmAction;
		this.discardAction = discardAction;
		this.confirmButtonText = confirmButtonText;
		this.closeButtonText = closeButtonText;
	}


	@Override
	public void execute(ConfirmationNotificationPresenter presenter) {
		presenter.initialize();
		presenter.setNotificationType(type);
		presenter.setTitle(title);
		presenter.setMessage(message);
		presenter.setConfirmationAction(confirmAction);
		presenter.setDiscardAction(discardAction);
		presenter.setConfirmButtonText(confirmButtonText);
		presenter.setDiscardButtonText(closeButtonText);
	}
}
