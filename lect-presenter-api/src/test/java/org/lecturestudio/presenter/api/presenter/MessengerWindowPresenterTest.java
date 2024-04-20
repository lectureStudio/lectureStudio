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

package org.lecturestudio.presenter.api.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.MessengerWindow;

import org.junit.jupiter.api.Test;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.lecturestudio.web.api.model.Message;

class MessengerWindowPresenterTest extends PresenterTest {

	@Test
	void testInit() {
		MessengerMockWindow window = new MessengerMockWindow() {
			@Override
			public void setTextSize(double size) {
				assertEquals(10.0, size);
			}

			@Override
			public void setTitle(String title) {
				assertEquals("messenger.window.title", title);
			}
		};

		MessengerWindowPresenter presenter = new MessengerWindowPresenter(context, window);
		presenter.initialize();
	}

	@Test
	void testCloseWindow() {
		AtomicBoolean closed = new AtomicBoolean(false);

		MessengerMockWindow window = new MessengerMockWindow();

		MessengerWindowPresenter presenter = new MessengerWindowPresenter(context, window);
		presenter.initialize();
		presenter.setOnClose(() -> closed.set(true));

		window.close();

		assertTrue(closed.get());
	}

	@Test
	void testOnMessage() {
		AtomicReference<MessengerMessage> messageRef = new AtomicReference<>();
		MessengerMessage message = new MessengerMessage();
		message.setMessage(new Message("What?"));
		message.setDate(ZonedDateTime.now());
		message.setUserId(UUID.randomUUID().toString());

		MessengerMockWindow window = new MessengerMockWindow() {
			@Override
			public void setMessengerMessage(MessengerMessage message) {
				messageRef.set(message);
			}
		};

		MessengerWindowPresenter presenter = new MessengerWindowPresenter(context, window);
		presenter.initialize();
		presenter.onEvent(message);

		assertEquals(message, messageRef.get());
	}



	private static class MessengerMockWindow implements MessengerWindow {

		private Action closeAction;

		private boolean closed = false;


		@Override
		public void setMessengerMessage(MessengerMessage message) {

		}

		@Override
		public void setMessengerMessageAsReply(MessengerMessage message, MessengerMessage messageToReplyTo) {

		}

		@Override
		public void setModifiedMessengerMessage(MessengerMessage modifiedMessage) {

		}

		@Override
		public void removeMessengerMessage(String messageId) {

		}

		@Override
		public void setSpeechRequestMessage(SpeechRequestMessage message) {

		}

		@Override
		public void setTextSize(double size) {

		}

		@Override
		public void setTitle(String title) {

		}

		@Override
		public void close() {
			if (!closed) {
				// Close window only once.
				closed = true;

				closeAction.execute();
			}
		}

		@Override
		public void open() {

		}

		@Override
		public void setOnClose(Action action) {
			assertNotNull(action);

			closeAction = action;
		}
	}

}
