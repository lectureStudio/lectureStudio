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

package org.lecturestudio.web.service.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.lecturestudio.core.app.configuration.ConfigurationService;
import org.lecturestudio.core.app.configuration.YamlConfigurationService;
import org.lecturestudio.web.api.config.WebServiceConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class ConfigurationProvider {

	private static final Logger LOG = LogManager.getLogger(ConfigurationProvider.class);


	@Produces
	@Default
	public WebServiceConfiguration getConfiguration() {
		ConfigurationService<WebServiceConfiguration> configService = new YamlConfigurationService<>();

		WebServiceConfiguration config = null;

		try {
			URL url = getClass().getClassLoader().getResource("../web-service.cfg");
			File configFile = Paths.get(Objects.requireNonNull(url).toURI()).toFile();

			config = configService.load(configFile, WebServiceConfiguration.class);
		}
		catch (Exception e) {
			LOG.error("Load configuration failed.", e);
		}

		return config;
	}

}
