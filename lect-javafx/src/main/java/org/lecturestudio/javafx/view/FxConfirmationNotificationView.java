package org.lecturestudio.javafx.view;

import javax.inject.Inject;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConfirmationNotificationView;
import org.lecturestudio.javafx.control.NotificationPane;
import org.lecturestudio.javafx.util.FxUtils;

public class FxConfirmationNotificationView extends NotificationPane implements ConfirmationNotificationView {

	private Button discardButton;

	private Button confirmButton;

	@Inject
	public FxConfirmationNotificationView(ResourceBundle resources) {
		super(resources);
	}

	@Override
	public void setOnConfirm(Action action) {
		FxUtils.bindAction(confirmButton, action);
	}

	@Override
	public void setOnDiscard(Action action) {
		FxUtils.bindAction(discardButton, action);
	}

	@Override
	public void setConfirmButtonText(String confirmButtonText) {
		FxUtils.invoke(() -> confirmButton.setText(confirmButtonText));
	}

	@Override
	public void setDiscardButtonText(String discardButtonText) {
		FxUtils.invoke(() -> discardButton.setText(discardButtonText));
	}

	@FXML
	@Override
	protected void initialize() {
		super.initialize();

		discardButton = new Button(getResourceBundle().getString("button.close"));
		confirmButton = new Button(getResourceBundle().getString("button.confirm"));

		getButtons().addAll(confirmButton, discardButton);
	}
}
