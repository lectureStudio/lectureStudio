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
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.PreviewStreamView;

public class StreamPreviewPresenter extends CefStreamPresenter<PreviewStreamView> {

	@Inject
	StreamPreviewPresenter(ApplicationContext context, PreviewStreamView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		super.initialize();

		view.setBrowserComponent(browser.getUIComponent());
		view.setOnClose(this::close);
	}

	@Override
	public void close() {
		PresenterContext ctx = (PresenterContext) context;
		ctx.getEventBus().unregister(this);
		ctx.setViewStream(false);

		super.close();
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}
}
