/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import com.google.common.eventbus.Subscribe;

import java.util.concurrent.CompletableFuture;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.presenter.command.PreviewStreamCommand;

import me.friwi.jcefmaven.UnsupportedPlatformException;

/**
 * Handle stream viewing state by incorporating and installing the Java Chromium
 * Embedded Framework (JCEF) if necessary.
 *
 * @author Alex Andres
 */
public class PreviewStreamHandler extends CefStreamHandler {

	private final EventBus eventBus;

	private ExecutableState streamState;


	public PreviewStreamHandler(PresenterContext context) {
		super(context);

		eventBus = context.getEventBus();
	}

	@Override
	protected void installFinished() {
		showStreamView();
	}

	@Override
	public void initialize() {
		eventBus.register(this);
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		streamState = event.getState();

		if (streamState == ExecutableState.Stopped) {
			eventBus.unregister(this);
		}
	}

	@Subscribe
	public void onEvent(final MessengerStateEvent event) {
		ExecutableState state = event.getState();

		// Show the stream view when both, the stream itself and the chat have
		// successfully started.
		if (state == ExecutableState.Started
				&& streamState == ExecutableState.Started) {
			try {
				if (!isJcefInstalled()) {
					installJcef();
				}
				else {
					showStreamView();
				}
			}
			catch (UnsupportedPlatformException e) {
				throw new RuntimeException(e);
			}
			finally {
				// This handler has nothing to do anymore.
				eventBus.unregister(this);
			}
		}
	}

	private void showStreamView() {
		CompletableFuture.runAsync(() -> {
			// Add a slight delay to let the stream setup settle.
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				// Ignore
			}

			eventBus.post(new PreviewStreamCommand(() -> {
				// On close, unsubscribe from receiving state events.
				eventBus.unregister(this);
			}));
		});
	}
}
