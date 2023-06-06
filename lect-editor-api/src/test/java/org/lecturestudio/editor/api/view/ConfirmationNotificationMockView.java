package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConfirmationNotificationView;
import org.lecturestudio.core.view.NotificationType;

public class ConfirmationNotificationMockView implements ConfirmationNotificationView {
	public Action setOnConfirmAction;
	public String setConfirmButtonTextString;
	public String setDiscardButtonTextString;
	public NotificationType setTypeType;
	public String setTitleString;
	public String setMessageString;
	public Action setOnDiscardAction;

	@Override
	public void setOnConfirm(Action action) {
		this.setOnConfirmAction = action;
	}

	@Override
	public void setConfirmButtonText(String confirmButtonText) {
		this.setConfirmButtonTextString = confirmButtonText;
	}

	@Override
	public void setDiscardButtonText(String discardButtonText) {
		this.setDiscardButtonTextString = discardButtonText;
	}

	@Override
	public void setType(NotificationType type) {
		this.setTypeType = type;
	}

	@Override
	public void setTitle(String title) {
		this.setTitleString = title;
	}

	@Override
	public void setMessage(String message) {
		this.setMessageString = message;
	}

	@Override
	public void setOnDiscard(Action action) {
		this.setOnDiscardAction = action;
	}
}
