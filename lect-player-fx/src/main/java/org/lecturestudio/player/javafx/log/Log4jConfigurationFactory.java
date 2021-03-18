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

package org.lecturestudio.player.javafx.log;

import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.log.Log4jXMLConfigurationFactory;

@Plugin(name = "Log4jConfigurationFactory", category = "ConfigurationFactory")
@Order(10)
public class Log4jConfigurationFactory extends Log4jXMLConfigurationFactory {

	public Log4jConfigurationFactory() {
		AppDataLocator dataLocator = new AppDataLocator("lecturePlayerFX");

		System.setProperty("logFilePath", dataLocator.getAppDataPath());
	}

}
