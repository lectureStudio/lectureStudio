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

package org.lecturestudio.presenter.swing.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.inject.Inject;
import javax.swing.JFrame;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.swing.components.MessageView;
import org.lecturestudio.swing.components.SpeechRequestView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;

@SwingView(name = "messenger-window")
public class SwingMessengerWindow extends JFrame implements MessengerWindow {

	private final Dictionary dict;

	private Container messageViewContainer;


	@Inject
	SwingMessengerWindow(Dictionary dictionary) {
		super();

		this.dict = dictionary;
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		SwingUtils.invoke(() -> {
			MessageView messageView = new MessageView(this.dict);
			messageView.setUserName(String.format("%s %s", message.getFirstName(), message.getFamilyName()));
			messageView.setDate(message.getDate());
			messageView.setMessage(message.getMessage().getText());
			messageView.setOnDiscard(() -> {
				removeMessageView(messageView);
			});
			messageView.pack();

			messageViewContainer.add(messageView);
			messageViewContainer.revalidate();
		});
	}

	@Override
	public void setSpeechRequestMessage(SpeechRequestMessage message) {
		SwingUtils.invoke(() -> {
			SpeechRequestView requestView = new SpeechRequestView(this.dict);
			requestView.setRequestId(message.getRequestId());
			requestView.setUserName(String.format("%s %s", message.getFirstName(), message.getFamilyName()));
			requestView.setDate(message.getDate());
			requestView.setOnAccept(() -> {
				removeMessageView(requestView);
			});
			requestView.setOnReject(() -> {
				removeMessageView(requestView);
			});
			requestView.pack();

			messageViewContainer.add(requestView);
			messageViewContainer.revalidate();
		});
	}

	@Override
	public void setTextSize(double size) {

	}

	@Override
	public void close() {
		SwingUtils.invoke(() -> {
			setVisible(false);
			dispose();
		});
	}

	@Override
	public void open() {
		SwingUtils.invoke(() -> setVisible(true));
	}

	@Override
	public void setOnClose(Action action) {
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				executeAction(action);
			}
		});
	}

	private void removeMessageView(Component view) {
		for (Component c : messageViewContainer.getComponents()) {
			if (c == view) {
				messageViewContainer.remove(view);
				messageViewContainer.validate();
				messageViewContainer.repaint();
				break;
			}
		}
	}
}
