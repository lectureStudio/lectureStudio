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

package org.lecturestudio.presenter.javafx.view;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.presenter.PresentationPresenter;
import org.lecturestudio.core.presenter.SlidePresentationPresenter;
import org.lecturestudio.core.view.PresentationView;
import org.lecturestudio.core.view.PresentationViewFactory;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.SlidePresentationView;
import org.lecturestudio.javafx.window.SlideViewWindow;
import org.lecturestudio.swing.DefaultRenderContext;

public class FxPresentationViewFactory implements PresentationViewFactory {

	@Override
	public PresentationPresenter<? extends PresentationView> createPresentationView(ApplicationContext context, Screen screen) {
		final RenderController renderController = new RenderController(context, new DefaultRenderContext());
		final SlidePresentationView view;

		if (Platform.isFxApplicationThread()) {
			view = new SlideViewWindow(screen, renderController);
		}
		else {
			final CountDownLatch createLatch = new CountDownLatch(1);

			FutureTask<SlidePresentationView> createTask = new FutureTask<>(() -> {
				SlidePresentationView viewWindow = new SlideViewWindow(screen, renderController);

				createLatch.countDown();

				return viewWindow;
			});

			Platform.runLater(createTask);

			try {
				createLatch.await();

				view = createTask.get();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return new SlidePresentationPresenter(context, view, context.getDocumentService());
	}
}
