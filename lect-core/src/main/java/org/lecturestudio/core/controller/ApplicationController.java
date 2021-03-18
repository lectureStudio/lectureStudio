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

package org.lecturestudio.core.controller;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.AsyncCommandController;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.ApplicationControllerEvent;
import org.lecturestudio.core.bus.event.ApplicationShutdownEvent;
import org.lecturestudio.core.bus.event.CloseDocumentEvent;
import org.lecturestudio.core.bus.event.CloseDocumentsEvent;
import org.lecturestudio.core.bus.event.CloseSelectedDocumentEvent;
import org.lecturestudio.core.bus.event.CreateWhiteboardEvent;
import org.lecturestudio.core.bus.event.CreateWhiteboardPageEvent;
import org.lecturestudio.core.bus.event.DeleteWhiteboardPageEvent;
import org.lecturestudio.core.bus.event.OpenWhiteboardEvent;
import org.lecturestudio.core.bus.event.SelectDocumentEvent;
import org.lecturestudio.core.bus.event.SelectNextPageEvent;
import org.lecturestudio.core.bus.event.SelectPageEvent;
import org.lecturestudio.core.bus.event.SelectPreviousPageEvent;
import org.lecturestudio.core.bus.event.ShutdownEvent;
import org.lecturestudio.core.bus.event.ToggleWhiteboardEvent;
import org.lecturestudio.core.bus.event.UICommand;
import org.lecturestudio.core.bus.event.UIEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.service.DocumentService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An ApplicationController asynchronously executes application-related events
 * in receiving order. Application events mainly concern the application
 * life-cycle and centralized document handling.
 *
 * @author Alex Andres
 */
public abstract class ApplicationController extends AsyncCommandController<ApplicationControllerEvent> {

	private static final Logger LOG = LogManager.getLogger(ApplicationController.class);

	/** The list of sub-controllers managed by this controller. */
	private final List<Controller> controllers = new ArrayList<>();

	/** The application event bus. */
	private final EventBus eventBus;

	/** The document service to manage all opened documents. */
	private final DocumentService documentService;


	/**
	 * Create an ApplicationController with the specified application context.
	 *
	 * @param context The application context.
	 */
	public ApplicationController(ApplicationContext context) {
		super(context);

		eventBus = context.getEventBus();
		documentService = context.getDocumentService();
	}
	
	@Subscribe
	public final void onEvent(ApplicationControllerEvent event) {
		processEvent(event);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		super.initInternal();
		
		registerCommand(OpenWhiteboardEvent.class,			this::openWhiteboard);
		registerCommand(SelectDocumentEvent.class,			this::selectDocument, Document.class);
		registerCommand(CloseDocumentEvent.class,			this::closeDocument, Document.class);
		registerCommand(CloseDocumentsEvent.class,			this::closeDocuments, List.class);
		registerCommand(CloseSelectedDocumentEvent.class,	this::closeSelectedDocument);
		registerCommand(CreateWhiteboardEvent.class,		this::addNewWhiteboard);
		registerCommand(CreateWhiteboardPageEvent.class,	this::createWhiteboardPage);
		registerCommand(DeleteWhiteboardPageEvent.class,	this::deleteWhiteboardPage);
		registerCommand(ToggleWhiteboardEvent.class,		this::toggleWhiteboard);
		registerCommand(SelectPageEvent.class,				this::selectPage, Page.class);
		registerCommand(SelectNextPageEvent.class,			this::selectNextPage);
		registerCommand(SelectPreviousPageEvent.class,		this::selectPreviousPage);
		registerCommand(ApplicationShutdownEvent.class,		this::shutdown);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		super.startInternal();

		for (Controller controller : controllers) {
			controller.start();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		super.stopInternal();

		for (Controller controller : controllers) {
			controller.stop();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		super.stopInternal();

		// Shutdown gracefully. Let other controllers destroy in case of an error.
		for (Controller controller : controllers) {
			try {
				controller.destroy();
			}
			catch (Exception e) {
				LOG.error("Destroying controller failed.", e);
			}
		}
	}

	protected void registerController(Controller controller) {
		controllers.add(controller);
	}

	protected void dispatchErrorToUI(String message) {
		Dictionary dict = getContext().getDictionary();
		String error = dict.get("error") + ": " + message;

		eventBus.post(new UIEvent(UICommand.UI_ERROR, error));
	}
	
	protected void saveConfiguration() {
		try {
			getContext().saveConfiguration();
		}
		catch (Exception e) {
			LOG.error("Save configuration failed.", e);
		}
	}
	
	protected void addDocument(Document doc) {
		documentService.addDocument(doc);
	}
	
	protected void removeDocument(Document doc) {
		documentService.removeDocument(doc);
	}
	
	protected void selectDocument(Document doc) {
		documentService.selectDocument(doc);
	}
	
	protected void selectLastDocument() {
		documentService.selectLastDocument();
	}
	
	protected void closeSelectedDocument() {
		documentService.closeSelectedDocument();
	}
	
	private void closeDocument(Document document) {
		documentService.closeDocument(document);
	}
	
	private void closeDocuments(List<Document> documents) {
		documentService.closeDocuments(documents);
	}
	
	protected void closeAllDocuments() {
		documentService.closeAllDocuments();
	}
	
	private void openWhiteboard() {
		documentService.openWhiteboard();
	}

	private void toggleWhiteboard() {
		documentService.toggleWhiteboard();
	}

	private void addNewWhiteboard() {
		documentService.addWhiteboard();
	}

	protected Document createWhiteboard() throws Exception {
		return documentService.addWhiteboard().get();
	}

	private void createWhiteboardPage() {
		documentService.createWhiteboardPage();
	}

	private void deleteWhiteboardPage() {
		documentService.deleteWhiteboardPage();
	}

	protected void selectPage(Page page) {
		documentService.selectPage(page);
	}
	
	protected void selectNextPage() {
		documentService.selectNextPage();
	}
	
	private void selectPreviousPage() {
		documentService.selectPreviousPage();
	}
	
	/**
	 * Shuts down the application.
	 * 
	 * @throws ExecutableException If the shutdown failed.
	 */
	protected void shutdown() throws ExecutableException {
		eventBus.post(new ShutdownEvent());
		
		destroy();

		System.exit(0);
	}

}
