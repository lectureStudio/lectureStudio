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

import com.google.common.eventbus.Subscribe;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.web.api.message.MessengerMessage;

public class MessengerWindowPresenter extends Presenter<MessengerWindow> {

	@Inject
	MessengerWindowPresenter(ApplicationContext context, MessengerWindow view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		setOnClose(this::closeWindow);

		context.getEventBus().register(this);

		Configuration config = context.getConfiguration();

		view.setOnClose(this::close);
		view.setTextSize(config.getUIControlSize());
		view.setTitle(context.getDictionary().get("messenger.window.title"));
		//view.setIcons(window.getIcons());

		config.uiControlSizeProperty().addListener((observable, oldSize, newSize) -> {
			view.setTextSize(newSize);
		});

		view.open();
	}

	@Override
	public void destroy() {
		closeWindow();
	}

	@Override
	public void close() {
		context.getEventBus().unregister(this);

		super.close();
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Window;
	}

	@Subscribe
	public void onEvent(MessengerMessage message) {
		view.addMessage(message);
	}

	public void closeWindow() {
		view.close();
	}

}