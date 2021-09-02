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

package org.lecturestudio.core.app;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lecturestudio.core.exception.UncaughtExceptionHandler;
import org.lecturestudio.core.util.OsInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base application bootstrapper implementation meant to be extended by specific
 * bootstrappers to configure the Virtual Machine for individual applications.
 * The bootstrapper will create and start a new Virtual Machine process which
 * runs the application.
 *
 * @param <T> The type of the applications main class.
 *
 * @author Alex Andres
 */
public abstract class ApplicationBootstrapper<T> {

	/** Logger for {@link ApplicationBootstrapper} */
	private final static Logger LOG = LogManager.getLogger(ApplicationBootstrapper.class);

	/** The list of Virtual Machine arguments. */
	private final List<String> vmArguments = new ArrayList<>();

	/** The list of application arguments. */
	private final List<String> appArguments = new ArrayList<>();


	/**
	 * Create a new ApplicationBootstrapper instance. This constructor sets the
	 * default "java.library.path" to "lib/native/{platform}".
	 */
	public ApplicationBootstrapper() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(LOG));

		addVMArgument("-Djava.library.path=lib/native/" + OsInfo.getPlatformName());
	}

	/**
	 * Add application arguments.
	 *
	 * @param args The application arguments to add.
	 */
	public void addAppArguments(String... args) {
		appArguments.addAll(Arrays.asList(args));
	}

	/**
	 * Add a new Virtual Machine argument.
	 *
	 * @param arg The Virtual Machine argument to add.
	 */
	public void addVMArgument(String arg) {
		vmArguments.add(arg);
	}

	/**
	 * Initiates the bootstrapping of the application by creating and starting a
	 * new Virtual Machine process. The previously set arguments via the {@link
	 * #addVMArgument(String)} method will be passed to the new Virtual Machine
	 * process.
	 */
	public final void bootstrap() {
		String separator = File.separator;
		String javaBin = System.getProperty("java.home") + separator + "bin" + separator + "java";
		String classpath = System.getProperty("java.class.path");

		// Get main class by reflection.
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		Class<?> mainClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

		List<String> parameter = new ArrayList<>();
		parameter.add(javaBin);
		parameter.add("-cp");
		parameter.add(classpath);
		parameter.addAll(vmArguments);
		parameter.add(mainClass.getCanonicalName());
		parameter.addAll(appArguments);

		ProcessBuilder processBuilder = new ProcessBuilder(parameter);

		try {
			processBuilder.start();
		}
		catch (Exception e) {
			LOG.error("Bootstrapping failed.", e);
		}
	}

}
