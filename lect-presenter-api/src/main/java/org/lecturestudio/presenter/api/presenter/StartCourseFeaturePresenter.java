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
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.command.ShowSettingsCommand;
import org.lecturestudio.presenter.api.service.WebServiceInfo;
import org.lecturestudio.presenter.api.view.StartCourseFeatureView;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.service.StreamProviderService;

public class StartCourseFeaturePresenter extends Presenter<StartCourseFeatureView> {

	private final WebServiceInfo webServiceInfo;

	private Course course;

	/** The action that is executed when the user clicked the start button. */
	private Action startAction;

	/** The action that is executed when the process has been aborted. */
	private Action abortAction;


	@Inject
	StartCourseFeaturePresenter(ApplicationContext context,
			StartCourseFeatureView view, WebServiceInfo webServiceInfo) {
		super(context, view);

		this.webServiceInfo = webServiceInfo;
	}

	@Override
	public void initialize() {
		PresenterContext pContext = (PresenterContext) context;
		List<Course> courses = null;

		try {
			courses = loadCourses();
		}
		catch (Exception e) {
			view.setCourses(List.of());
			view.setError(context.getDictionary().get("start.course.feature.service.error"));

			pContext.setCourse(null);
		}

		if (nonNull(courses)) {
			if (nonNull(course)) {
				// Restrict to only one course that has a feature started by
				// the running app-instance.
				courses = List.of(course);
			}
			else {
				Course selectedCourse = pContext.getCourse();

				if (isNull(selectedCourse) && !courses.isEmpty()) {
					// Set first available lecture by default.
					pContext.setCourse(courses.get(0));
				}
				else if (!courses.contains(selectedCourse)) {
					pContext.setCourse(courses.get(0));
				}
			}

			view.setCourses(courses);
			view.setCourse(pContext.courseProperty());
		}

		view.setOnStart(this::onStart);
		view.setOnSettings(this::onSettings);
		view.setOnClose(this::close);
	}

	@Override
	public void close() {
		dispose();

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.setStreamStarted(false);

		if (nonNull(abortAction)) {
			abortAction.execute();
		}
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public void setOnStart(Action action) {
		startAction = action;
	}

	public void setOnAbort(Action action) {
		abortAction = action;
	}

	private void onStart() {
		dispose();

		if (nonNull(startAction)) {
			startAction.execute();
		}
	}

	private void onSettings() {
		close();

		context.getEventBus().post(new ShowSettingsCommand("stream"));
	}

	private void dispose() {
		super.close();
	}

	private List<Course> loadCourses() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl(webServiceInfo.getStreamPublisherApiUrl());

		StreamProviderService streamProviderService = new StreamProviderService(
				parameters, streamConfig::getAccessToken);

		return streamProviderService.getCourses();
	}
}