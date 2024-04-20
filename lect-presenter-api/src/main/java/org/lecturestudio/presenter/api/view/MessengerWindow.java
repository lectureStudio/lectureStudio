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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;

public interface MessengerWindow extends View {

	void setMessengerMessage(MessengerMessage message);

	void setMessengerMessageAsReply(MessengerMessage message, MessengerMessage messageToReplyTo);

	void setModifiedMessengerMessage(MessengerMessage modifiedMessage);

	void removeMessengerMessage(String messageId);

	void setSpeechRequestMessage(SpeechRequestMessage message);

	void setTextSize(double size);

	void setTitle(String title);

	void close();

	void open();

	void setOnClose(Action action);

}
