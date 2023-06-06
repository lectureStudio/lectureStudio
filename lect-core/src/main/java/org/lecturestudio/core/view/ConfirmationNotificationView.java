package org.lecturestudio.core.view;


/**
 * Generic notification class used for notification windows with both an accept and decline option.
 */
public interface ConfirmationNotificationView extends View {

	void setOnConfirm(Action action);

	void setType(NotificationType type);

	void setTitle(String title);

	void setMessage(String message);

	void setOnDiscard(Action action);

	void setConfirmButtonText(String confirmButtonText);

	void setDiscardButtonText(String discardButtonText);
}