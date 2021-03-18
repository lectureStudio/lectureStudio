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

package org.lecturestudio.core.net;

import org.lecturestudio.core.Executable;

public interface ApplicationServer extends Executable {

	/**
	 * Deploy a web application to the application server 'webapps' directory.
	 *
	 * @param contextPath The context mapping to use, "" for root context.
	 * @param appName The application name.
	 *
	 * @throws Exception if a deployment error occurs.
	 */
	void startWebApp(String contextPath, String appName) throws Exception;

	/**
	 * Remove a deployed web application with the specified context mapping
	 * from the application server.
	 *
	 * @param contextPath The context mapping of the deployed web application.
	 *
	 * @throws Exception if a removal error occurs.
	 */
	void stopWebApp(String contextPath) throws Exception;

}
