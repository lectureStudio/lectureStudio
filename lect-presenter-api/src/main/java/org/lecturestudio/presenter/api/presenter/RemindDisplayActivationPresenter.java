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

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.view.RemindDisplayActivationView;

public class RemindDisplayActivationPresenter extends Presenter<RemindDisplayActivationView> {

	private final PresentationController presentationController;


	@Inject
	protected RemindDisplayActivationPresenter(ApplicationContext context,
											   RemindDisplayActivationView view,
											   PresentationController presentationController) {
		super(context, view);

		this.presentationController = presentationController;
	}

	@Override
	public void initialize() {
		Dictionary dict = context.getDictionary();

		view.setType(NotificationType.QUESTION);
		view.setTitle(dict.get("displays.notification.title"));
		view.setMessage(dict.get("displays.notification.message"));
		view.setOnActivate(this::activate);
		view.setOnClose(this::close);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void activate() {
		close();

		if (!presentationController.getPresentationViewsVisible()) {
			presentationController.showPresentationViews(true);
		}
	}
}
