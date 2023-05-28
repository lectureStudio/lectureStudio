package org.lecturestudio.core.presenter;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConfirmationNotificationView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ViewLayer;


/**
 * Generic notification class used for notification windows with both an accept and decline option.
 */
public class ConfirmationNotificationPresenter extends Presenter<ConfirmationNotificationView> {

	@Inject
	public ConfirmationNotificationPresenter(ApplicationContext context, ConfirmationNotificationView view) {
		super(context, view);
	}

	public void setNotificationType(NotificationType type) {
		view.setType(type);
	}

	public void setTitle(String title) {
		view.setTitle(title);
	}

	public void setMessage(String message) {
		view.setMessage(message);
	}

	public void setConfirmationAction(Action action) {
		view.setOnConfirm(() -> {
			action.execute();
			close();
		});
	}

	public void setDiscardAction(Action action) {
		view.setOnDiscard(() -> {
			action.execute();
			close();
		});
	}

	@Override
	public void initialize() {
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Notification;
	}

	public void setConfirmButtonText(String confirmButtonText) {
		view.setConfirmButtonText(confirmButtonText);
	}

	public void setDiscardButtonText(String closeButtonText) {
		view.setDiscardButtonText(closeButtonText);
	}
}
