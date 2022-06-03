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

package org.lecturestudio.presenter.api.service;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.presenter.api.context.PresenterContext;

/**
 * Web service base class with convenience methods and common fields. Web
 * services are meant to extend this class to enable common handling of all
 * services through a service manager.
 *
 * @author Alex Andres
 */
public abstract class FeatureServiceBase extends ExecutableBase {

	protected final PresenterContext context;

	/**
	 * The course ID to which this service belongs to.
	 */
	protected long courseId;

	/**
	 * The ID of this service.
	 */
	protected String serviceId;


	/**
	 * Creates a new {@code WebRtcServiceBase}.
	 *
	 * @param context       The application context.
	 */
	public FeatureServiceBase(PresenterContext context) {
		this.context = context;
	}

	/**
	 * Set the course ID to which this service belongs to.
	 *
	 * @param id The ID of the course.
	 */
	public void setCourseId(long id) {
		courseId = id;
	}
}
