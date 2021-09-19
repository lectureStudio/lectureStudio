/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.presenter.javafx.view;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.control.MessageView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;

public class FxMessengerWindow implements MessengerWindow {

	private final Lock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	@FXML
	private Pane container;

	private Stage stage;


	@Inject
	public FxMessengerWindow(ResourceBundle resources) {
		if (Platform.isFxApplicationThread()) {
			load(resources);
		}
		else {
			// Since the Stage has to be instantiated in the JavaFX Thread,
			// wait until the window and its content is loaded and then
			// return back to the running thread.

			lock.lock();

			try {
				Platform.runLater(() -> load(resources));

				condition.await();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		FxUtils.invoke(() -> {
			MessageView messageView = new MessageView();
			messageView.setDate(message.getDate());
			messageView.setHost(message.getRemoteAddress());
			messageView.setMessage(message.getMessage().getText());

			container.getChildren().add(messageView);
		});
	}

	@Override
	public void setSpeechRequestMessage(SpeechRequestMessage message) {

	}

	@Override
	public void setTextSize(double size) {
		stage.getScene().getRoot().setStyle(String.format(Locale.US, "-fx-font-size: %.2fpt;", size));
	}

	@Override
	public void setTitle(String title) {
		stage.setTitle(title);
	}

	@Override
	public void close() {
		FxUtils.invoke(() -> stage.close());
	}

	@Override
	public void open() {
		FxUtils.invoke(() -> stage.show());
	}

	@Override
	public void setOnClose(Action action) {
		stage.setOnCloseRequest(event -> {
			event.consume();

			executeAction(action);
		});
	}

	private void load(ResourceBundle resources) {
		Parent root = FxUtils.load("/resources/views/messenger-window/messenger-window.fxml", resources, this);

		Scene scene = new Scene(root);

		stage = new Stage();
		stage.setScene(scene);

		resume();
	}

	private void resume() {
		lock.lock();

		try {
			condition.signal();
		}
		finally {
			lock.unlock();
		}
	}

}
