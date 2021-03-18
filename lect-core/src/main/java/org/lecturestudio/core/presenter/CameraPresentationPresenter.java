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

package org.lecturestudio.core.presenter;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.camera.bus.event.CameraImageEvent;
import org.lecturestudio.core.view.CameraPresentationView;

public class CameraPresentationPresenter extends PresentationPresenter<CameraPresentationView> {

	private final EventBus eventBus;


	public CameraPresentationPresenter(ApplicationContext context, CameraPresentationView view) {
		super(context, view);

		this.eventBus = context.getEventBus();

		initialize();
	}

	@Subscribe
	public void onEvent(final CameraImageEvent event) {
		if (!view.isVisible()) {
			return;
		}

		view.setImage(event.getImage());
	}

	@Override
	public void close() {
		eventBus.unregister(this);

		super.close();
	}

	@Override
	public void initialize() {
		eventBus.register(this);
	}
}
