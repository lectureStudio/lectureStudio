/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.swing.inject.guice;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.LocaleProvider;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.ConfigurationService;
import org.lecturestudio.core.app.configuration.JsonConfigurationService;
import org.lecturestudio.core.util.DirUtils;

/**
 * Guice module that provides configuration-related dependencies for the application.
 * <p>
 * This module is responsible for setting up the configuration service and loading the
 * application configuration from the file system. It defines the application data location
 * and configuration file path.
 *
 * @author Alex Andres
 */
public class ConfigurationModule implements Module {

    private final static Logger LOG = LogManager.getLogger(ConfigurationModule.class);

    /**
     * Application data locator that defines the base path for application-specific data.
     * This locator uses "lecturePresenter" as the application identifier to create the
     * appropriate directory structure for storing application data.
     */
    public static final AppDataLocator LOCATOR = new AppDataLocator("lecturePresenter");

    /**
     * The configuration file for the application.
     * This file is located in the application data directory and named "config.json".
     * It stores all application settings in JSON format.
     */
    public static final File CONFIG_FILE = new File(LOCATOR.toAppDataPath("config.json"));


    @Override
    public void configure(Binder binder) {

    }

    @Provides
    @Singleton
    ConfigurationService<Configuration> provideConfigurationService() {
        ConfigurationService<Configuration> configService = null;

        try {
            configService = new JsonConfigurationService<>();
        }
        catch (Exception e) {
            LOG.error("Create configuration service failed", e);
        }

        return configService;
    }

    @Provides
    @Singleton
    Configuration provideConfiguration(ConfigurationService<Configuration> configService) {
        Configuration configuration = null;

        try {
            DirUtils.createIfNotExists(Paths.get(LOCATOR.getAppDataPath()));

            if (!CONFIG_FILE.exists()) {
                // Create a configuration with default values.
                configuration = new Configuration();
                configuration.setLocale(Locale.getDefault());
            }
            else {
                configuration = configService.load(CONFIG_FILE, Configuration.class);
            }

            // Set system default locale.
            LocaleProvider localeProvider = new LocaleProvider();
            configuration.setLocale(localeProvider.getBestSupported(configuration.getLocale()));
        }
        catch (Exception e) {
            LOG.error("Create configuration failed", e);
        }

        return configuration;
    }
}
