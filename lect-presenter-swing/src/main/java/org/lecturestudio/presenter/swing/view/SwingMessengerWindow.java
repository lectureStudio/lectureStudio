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
import java.util.Optional;

import javax.inject.Inject;
import javax.swing.JFrame;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.service.UserPrivilegeService;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.presenter.swing.utils.ViewUtil;
import org.lecturestudio.swing.components.MessageAsReplyView;
import org.lecturestudio.swing.components.MessageView;
import org.lecturestudio.swing.components.SpeechRequestView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.message.MessengerDirectMessageAsReply;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.lecturestudio.web.api.message.util.MessageUtil;
import org.lecturestudio.web.api.model.UserInfo;

@SwingView(name = "messenger-window")
public class SwingMessengerWindow extends JFrame implements MessengerWindow {

	private final Dictionary dict;

	private final UserPrivilegeService userPrivilegeService;

	private Container messageViewContainer;


	@Inject
	SwingMessengerWindow(Dictionary dictionary,
			UserPrivilegeService userPrivilegeService) {
		super();

		this.dict = dictionary;
		this.userPrivilegeService = userPrivilegeService;
	}

	@Override
	public void setMessengerMessage(MessengerMessage message) {
		SwingUtils.invoke(() -> {
			UserInfo userInfo = userPrivilegeService.getUserInfo();

			MessageView messageView = ViewUtil.createMessageView(MessageView.class, userInfo, message, dict);
			messageView.setMessage(message.getMessage().getText(), message.getMessageId());
			messageView.setOnDiscard(() -> {
				removeMessageView(messageView);
			});

			if (message instanceof MessengerDirectMessage) {
				MessengerDirectMessage directMessage = (MessengerDirectMessage) message;
				String recipient = directMessage.getRecipientId();

				if (recipient.equals("organisers")) {
					messageView.setPrivateText(dict.get("text.message.to.organisators"));
				}
				else {
					messageView.setPrivateText(dict.get("text.message.privately"));
				}
			}

			messageView.pack();

			messageViewContainer.add(messageView);
			messageViewContainer.revalidate();
		});
	}

	@Override
	public void setMessengerMessageAsReply(MessengerMessage message, MessengerMessage messageToReplyTo) {
		SwingUtils.invoke(() -> {
			UserInfo userInfo = userPrivilegeService.getUserInfo();

			MessageAsReplyView messageView = ViewUtil.createMessageView(MessageAsReplyView.class, userInfo, message, dict);

			messageView.setMessage(message.getMessage().getText(), message.getMessageId());

			final String userToReplyTo = MessageUtil.evaluateSenderOfMessageToReplyTo(messageToReplyTo, userInfo, dict);
			messageView.setUserToReplyTo(userToReplyTo);

			messageView.setOnDiscard(() -> {
				removeMessageView(messageView);
			});

			if (message instanceof MessengerDirectMessageAsReply directMessageAsReply) {
				String recipient = directMessageAsReply.getRecipientId();

				if (recipient.equals("organisers")) {
					messageView.setPrivateText(dict.get("text.message.to.organisators"));
				}
				else {
					messageView.setPrivateText(dict.get("text.message.privately"));
				}
			}

			messageView.pack();

			messageViewContainer.add(messageView);
			messageViewContainer.revalidate();
		});
	}

	@Override
	public void setModifiedMessengerMessage(MessengerMessage modifiedMessage) {
		SwingUtils.invoke(() -> {
			final Optional<MessageView> toModify = findCorrespondingMessageView(modifiedMessage.getMessageId());

			if(toModify.isEmpty()) return;

			toModify.get().setMessage(modifiedMessage.getMessage().getText(), modifiedMessage.getMessageId());
			toModify.get().setIsEdited();
		});
	}

	@Override
	public void removeMessengerMessage(String messageId) {
		removeMessageView(messageId);
	}

	@Override
	public void setSpeechRequestMessage(SpeechRequestMessage message) {
		SwingUtils.invoke(() -> {
			UserInfo userInfo = userPrivilegeService.getUserInfo();

			SpeechRequestView requestView = ViewUtil.createMessageView(SpeechRequestView.class, userInfo, message, dict);
			requestView.setRequestId(message.getRequestId());
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

	private void removeMessageView(final String messageId) {
		for(Component component : messageViewContainer.getComponents()) {
			if((component instanceof MessageView messageView) &&
					messageView.getMessageId().equals(messageId)) {
				
				messageViewContainer.remove(component);
				messageViewContainer.validate();
				messageViewContainer.repaint();
				
				return;
			}
		}
	}

	private Optional<MessageView> findCorrespondingMessageView(final String messageId) {
		for (Component component : messageViewContainer.getComponents()) {
			if ((component instanceof MessageView messageView) &&
					messageView.getMessageId().equals(messageId)) {
				return Optional.of(messageView);
			}
		}
		return Optional.empty();
	}
}
