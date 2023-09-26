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

import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.StreamPreviewPresenter;

import me.friwi.jcefmaven.UnsupportedPlatformException;

/**
 * Handle stream viewing state by incorporating and installing the Java Chromium
 * Embedded Framework (JCEF) if necessary.
 *
 * @author Alex Andres
 */
public class PreviewStreamHandler extends CefStreamHandler {

	private final EventBus eventBus;


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
	}

	private void showStreamView() {
		eventBus.post(new ShowPresenterCommand<>(StreamPreviewPresenter.class));
	}
}
