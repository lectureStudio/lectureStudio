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

package org.lecturestudio.core.presenter;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.SlidePresentationView;
import org.lecturestudio.core.view.ViewType;

/**
 * A presenter class responsible for managing slide presentations.
 * <p>
 * This presenter handles the interaction between the application and the slide presentation view,
 * managing document and page selection, and responding to events from the event bus.
 * It updates the view according to changes in the current document and presentation parameters.
 * </p>
 *
 * @author Alex Andres
 */
public class SlidePresentationPresenter extends PresentationPresenter<SlidePresentationView> {

	/** The document service used to access and manipulate documents. */
	private final DocumentService documentService;

	/** The event bus used for publishing and subscribing to events. */
	private final EventBus eventBus;

	/** Listener that responds to changes in presentation parameters. */
	private ParameterChangeListener parameterChangeListener;

	/** The document currently being presented. */
	private Document currentDocument;


	/**
	 * Creates a new slide presentation presenter.
	 *
	 * @param context         The application context.
	 * @param view            The slide presentation view to be managed by this presenter.
	 * @param documentService The service that provides access to documents.
	 */
	public SlidePresentationPresenter(ApplicationContext context, SlidePresentationView view, DocumentService documentService) {
		super(context, view);

		this.documentService = documentService;
		this.eventBus = context.getEventBus();

		initialize();
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (event.selected() || event.replaced()) {
			setDocument(event.getDocument());
		}
		else if (event.closed()) {
			setDocument(null);
		}

		if (event.selected() || event.replaced()) {
			Document document = event.getDocument();

			setPage(document.getCurrentPage());
		}
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		if (event.isSelected()) {
			setPage(event.getPage());
		}
	}

	@Override
	public void close() {
		eventBus.unregister(this);

		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.Presentation);
		ppProvider.removeParameterChangeListener(parameterChangeListener);

		super.close();
	}

	@Override
	public void initialize() {
		eventBus.register(this);

		setDocument(documentService.getDocuments().getSelectedDocument());

		parameterChangeListener = new ParameterChangeListener() {

			@Override
			public Page forPage() {
				return nonNull(currentDocument) ? currentDocument.getCurrentPage() : null;
			}

			@Override
			public void parameterChanged(Page page, PresentationParameter parameter) {
				if (!view.isVisible()) {
					return;
				}

				view.setPage(page, parameter);
			}
		};

		// Register for page parameter change updates.
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.Presentation);
		ppProvider.addParameterChangeListener(parameterChangeListener);

		view.setOnVisible(this::viewVisible);
	}

	private void viewVisible() {
		if (nonNull(currentDocument)) {
			setPage(currentDocument.getCurrentPage());
		}
	}

	private void setPage(Page page) {
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.Presentation);
		PresentationParameter parameter = ppProvider.getParameter(page);

		view.setPage(page, parameter);
	}

	private void setDocument(Document doc) {
		this.currentDocument = doc;
	}
}
