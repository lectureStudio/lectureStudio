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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.view.StartStreamView;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.service.StreamService;

public class StartStreamPresenter extends Presenter<StartStreamView> {

	/** The action that is executed when the saving process has been aborted. */
	private Action startAction;


	@Inject
	StartStreamPresenter(ApplicationContext context, StartStreamView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		loadCourses();

		view.setOnStart(this::onStart);
		view.setOnClose(this::close);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnStart(Action startAction) {
		this.startAction = startAction;
	}

	private void onStart() {
		close();

		if (nonNull(startAction)) {
			startAction.execute();
		}
	}

	public void loadCourses() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl("https://lecturestudio.dek.e-technik.tu-darmstadt.de");

		StreamService streamService = new StreamService(parameters, streamConfig::getAccessToken);

		try {
			List<Course> courses = streamService.getCourses();
			Course selectedCourse = streamConfig.getCourse();

			if (isNull(selectedCourse) && !courses.isEmpty()) {
				// Set first available lecture by default.
				streamConfig.setCourse(courses.get(0));
			}
			else if (!courses.contains(selectedCourse)) {
				streamConfig.setCourse(courses.get(0));
			}

			view.setCourses(courses);
			view.setCourse(streamConfig.courseProperty());
		}
		catch (Exception e) {
			view.setCourses(List.of());
			view.setError(context.getDictionary().get("start.stream.service.error"));

			streamConfig.setCourse(null);
		}
	}
}