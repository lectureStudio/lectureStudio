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

package org.lecturestudio.presenter.api.presenter.command;

import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.presenter.StartStreamPresenter;
import org.lecturestudio.web.api.stream.StreamContext;
import org.lecturestudio.web.api.stream.model.Course;

/**
 * Command to show the stream starting presenter with initialized course data.
 * This command extends ShowPresenterCommand to handle the initialization of
 * a StartStreamPresenter instance with the specified course and start action.
 *
 * @author Alex Andres
 */
public class StartStreamCommand extends ShowPresenterCommand<StartStreamPresenter> {

	/** The course to be streamed. */
	private final Course course;

	/** The action to execute when the stream starts. */
	private final ConsumerAction<StreamContext> startAction;


	/**
	 * Creates a new StartStreamCommand.
	 *
	 * @param course      The course to be streamed.
	 * @param startAction The action to execute when the stream starts.
	 */
	public StartStreamCommand(Course course, ConsumerAction<StreamContext> startAction) {
		super(StartStreamPresenter.class);

		this.course = course;
		this.startAction = startAction;
	}

	/**
	 * Executes this command by setting the course and start action on the provided
	 * StartStreamPresenter instance.
	 *
	 * @param presenter The presenter instance to initialize.
	 */
	@Override
	public void execute(StartStreamPresenter presenter) {
		presenter.setCourse(course);
		presenter.setOnStart(startAction);
	}
}
