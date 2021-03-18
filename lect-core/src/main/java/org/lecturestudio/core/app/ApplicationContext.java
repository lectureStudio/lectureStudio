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

import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * Base application context implementation that holds data object required by
 * the application. Such objects are, for instance, the {@link Configuration},
 * the {@link Dictionary} or the {@link DocumentService}.
 *
 * @author Alex Andres
 */
public abstract class ApplicationContext {

	/** The application resource data locator. */
	private final AppDataLocator dataLocator;

	/** The application configuration. */
	private final Configuration configuration;

	/** The application dictionary. */
	private final Dictionary dictionary;

	/** The document service to manage all slide documents. */
	private final DocumentService documentService;

	/** The presentation provider to manage the presentation of each page. */
	private final Map<ViewType, PresentationParameterProvider> ppProvider;

	/** The application event data bus. */
	private final EventBus eventBus;

	/** The audio event bus. */
	private final EventBus audioBus;


	/**
	 * This method is meant to be implemented by concrete application context
	 * class that implement their own configuration handling, like specific
	 * configuration paths and names.
	 *
	 * @throws Exception If an fatal error occurs while saving the configuration.
	 */
	abstract public void saveConfiguration() throws Exception;


	/**
	 * Create a new ApplicationContext instance with the given parameters.
	 *
	 * @param dataLocator The application resource data locator.
	 * @param config      The application configuration.
	 * @param dict        The application dictionary.
	 * @param eventBus    The application event data bus.
	 * @param audioBus    The audio event bus.
	 */
	public ApplicationContext(AppDataLocator dataLocator, Configuration config,
			Dictionary dict, EventBus eventBus, EventBus audioBus) {
		this.dataLocator = dataLocator;
		this.configuration = config;
		this.dictionary = dict;
		this.eventBus = eventBus;
		this.audioBus = audioBus;
		this.ppProvider = new HashMap<>();
		this.documentService = new DocumentService(this);

		ppProvider.put(ViewType.User, new PresentationParameterProvider(config));
		ppProvider.put(ViewType.Preview, new PresentationParameterProvider(config));
		ppProvider.put(ViewType.Presentation, new PresentationParameterProvider(config));
	}

	/**
	 * Obtain the application configuration.
	 *
	 * @return the application configuration.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Obtain the application dictionary.
	 *
	 * @return the application dictionary.
	 */
	public Dictionary getDictionary() {
		return dictionary;
	}

	/**
	 * Obtain the document service.
	 *
	 * @return the document service.
	 */
	public DocumentService getDocumentService() {
		return documentService;
	}

	/**
	 * Obtain the application event data bus.
	 *
	 * @return the application event data bus.
	 */
	public EventBus getEventBus() {
		return eventBus;
	}

	/**
	 * Obtain the audio event bus.
	 *
	 * @return the audio event bus.
	 */
	public EventBus getAudioBus() {
		return audioBus;
	}

	/**
	 * Obtain the list of all opened documents.
	 *
	 * @return the list of all opened documents.
	 */
	public DocumentList getDocuments() {
		return documentService.getDocuments();
	}

	/**
	 * Obtain the {@code AppDataLocator} to access application specific data.
	 *
	 * @return the {@code AppDataLocator}.
	 */
	public AppDataLocator getDataLocator() {
		return dataLocator;
	}

	/**
	 * Obtain the PresentationParameterProvider for the given ViewType.
	 *
	 * @param type The ViewType of the presentation provider.
	 *
	 * @return the PresentationParameterProvider bound to the ViewType.
	 */
	public PresentationParameterProvider getPagePropertyPropvider(ViewType type) {
		return ppProvider.get(type);
	}

}
