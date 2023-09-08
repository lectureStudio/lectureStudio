/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.service;

import java.util.function.Consumer;

import org.lecturestudio.web.api.message.MessageTransport;
import org.lecturestudio.web.api.message.WebMessage;

public abstract class ReactiveProviderService extends ProviderService {

	private final MessageTransport messageTransport;


	public ReactiveProviderService(ServiceParameters parameters,
			MessageTransport messageTransport) {
		this.parameters = parameters;
		this.messageTransport = messageTransport;
	}

	public <T extends WebMessage> void addMessageListener(Class<T> cls,
			Consumer<T> onEvent) {
		messageTransport.addListener(cls, onEvent);
	}

	public <T extends WebMessage> void removeMessageListener(Class<T> cls,
			Consumer<T> onEvent) {
		messageTransport.removeListener(cls, onEvent);
	}

	public void close() {

	}
}
