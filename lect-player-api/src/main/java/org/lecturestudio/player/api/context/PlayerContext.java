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

package org.lecturestudio.player.api.context;

import java.io.File;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.ConfigurationService;
import org.lecturestudio.core.app.configuration.JsonConfigurationService;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.media.playback.PlaybackContext;

public class PlayerContext extends PlaybackContext {

	private final File configFile;


	public PlayerContext(AppDataLocator dataLocator, File configFile,
			Configuration config, Dictionary dict, EventBus eventBus,
			EventBus audioBus) {
		super(dataLocator, config, dict, eventBus, audioBus);

		this.configFile = configFile;
	}

	@Override
	public void saveConfiguration() throws Exception {
		ConfigurationService<Configuration> configService = new JsonConfigurationService<>();
		configService.save(configFile, getConfiguration());
	}
}
