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

import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.web.api.event.HeartbeatEvent;

public class HeartbeatErrorHandler extends PresenterHandler {

	private final AtomicBoolean heartbeatError = new AtomicBoolean(false);


	/**
	 * Create a new {@code HeartbeatErrorHandler} with the given context.
	 *
	 * @param context The presenter application context.
	 */
	public HeartbeatErrorHandler(PresenterContext context) {
		super(context);
	}

	@Override
	public void initialize() {
		context.getEventBus().register(this);
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		// Reset error state for any state here.
		heartbeatError.set(false);
	}

	@Subscribe
	public void onEvent(final HeartbeatEvent event) {
		if (event.type() == HeartbeatEvent.Type.FAILURE) {
			if (heartbeatError.compareAndSet(false, true)) {
				context.showNotification(NotificationType.WARNING, "heartbeat.error", "heartbeat.error.message");
			}
		}
		else {
			heartbeatError.set(false);
		}
	}
}
