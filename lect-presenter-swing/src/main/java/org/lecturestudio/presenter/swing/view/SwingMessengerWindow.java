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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.swing.components.MessageView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.web.api.message.MessengerMessage;

@SwingView(name = "messenger-window")
public class SwingMessengerWindow extends JFrame implements MessengerWindow {

	private Container container;


	SwingMessengerWindow() {
		super();
	}

	@Override
	public void addMessage(MessengerMessage message) {
		SwingUtils.invoke(() -> {
			MessageView messageView = new MessageView();
			messageView.setDate(message.getDate());
			messageView.setHost(message.getRemoteAddress());
			messageView.setMessage(message.getMessage().getText());
			messageView.setImage(message.getImage());
			messageView.setPreferredSize(new Dimension(messageView.getPreferredSize().width, messageView.getPreferredSize().height));
			messageView.setMaximumSize(new Dimension(messageView.getMaximumSize().width, messageView.getPreferredSize().height));
			messageView.setMinimumSize(new Dimension(200, messageView.getPreferredSize().height));

			container.add(messageView);
			container.revalidate();
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
}
