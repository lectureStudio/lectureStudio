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

package org.lecturestudio.presenter.api.handler;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.RemindDisplayActivationPresenter;
import org.lecturestudio.presenter.api.presenter.command.CloseablePresenterCommand;

/**
 * Handles notifications related to display activation in the presenter application.
 * <p>
 * This handler monitors document and page events, tracking changes to pages and shapes.
 * When content is modified but presentation views are not visible, it triggers a
 * notification to remind the user to activate the displays.
 *
 * @author Alex Andres
 */
public class DisplayNotificationHandler extends PresenterHandler {

	/** The controller managing the presentation views and state. */
	private final PresentationController presentationController;

	/** The display configuration containing settings for notification preferences. */
	private final DisplayConfiguration displayConfig;

	/** Listener for page edit events that delegates to the pageEdited method. */
	private final PageEditedListener pageEditedListener = this::pageEdited;

	/** The currently active page being displayed or edited. */
	private Page page;

	/** Flag indicating whether the page has been changed since last check. */
	private boolean pageChanged;

	/** Flag indicating whether a shape has been added to the current page. */
	private boolean shapeAdded;

	/** Flag indicating whether the user has declined activation notifications. */
	private boolean userDeclined;

	/** Flag indicating whether presentation views are currently visible. */
	private boolean viewsVisible;


	/**
	 * Constructs a new DisplayNotificationHandler with the specified context and presentation controller.
	 *
	 * @param context        The presenter context providing access to the configuration and event bus.
	 * @param presController The controller responsible for managing presentation views.
	 */
	public DisplayNotificationHandler(PresenterContext context, PresentationController presController) {
		super(context);

		presentationController = presController;
		displayConfig = context.getConfiguration().getDisplayConfig();
	}

	@Override
	public void initialize() {
		context.getEventBus().register(this);

		presentationController.presentationViewsVisibleProperty().addListener((o, oldValue, newValue) -> {
			setPresentationViewsVisible(newValue);
		});
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (event.closed()) {
			return;
		}

		Document doc = event.getDocument();
		Page page = nonNull(doc) ? doc.getCurrentPage() : null;

		if (event.selected() && nonNull(page)) {
			page.addPageEditedListener(pageEditedListener);
		}

		pageChanged(page);
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		final Page page = event.getPage();

		if (event.isRemoved()) {
			page.removePageEditedListener(pageEditedListener);
		}
		else if (event.isSelected()) {
			Page oldPage = event.getOldPage();

			if (nonNull(oldPage)) {
				oldPage.removePageEditedListener(pageEditedListener);
			}

			page.addPageEditedListener(pageEditedListener);

			pageChanged(page);
		}
	}

	/**
	 * Processes a page change event by updating the current page.
	 *
	 * @param page The new page to be processed.
	 */
	private void pageChanged(Page page) {
		setPage(page);
	}

	/**
	 * Handles page edit events, processing changes to page content.
	 * Ignores events where only shape properties changed (not addition/removal).
	 *
	 * @param event The page edit event containing information about the changes.
	 */
	private void pageEdited(final PageEditEvent event) {
		if (event.shapeChanged()) {
			return;
		}

		pageChanged(event.getPage());
		setShape();
	}

	/**
	 * Marks that a shape has been added to the current page and shows an activation notification if necessary.
	 */
	private void setShape() {
		shapeAdded = true;

		if (notifyState()) {
			showActivateNotification();
		}
	}

	/**
	 * Updates the current page reference and tracks whether a page change occurred.
	 * Shows an activation notification if necessary.
	 *
	 * @param page The new page to set as current.
	 */
	private void setPage(Page page) {
		if (nonNull(this.page) && this.page != page) {
			pageChanged = true;
		}

		this.page = page;

		if (notifyState()) {
			showActivateNotification();
		}
	}

	/**
	 * Updates the visibility state of presentation views.
	 * Resets tracking state when views become hidden.
	 *
	 * @param viewsVisible True if presentation views are visible, false otherwise.
	 */
	private void setPresentationViewsVisible(boolean viewsVisible) {
		this.viewsVisible = viewsVisible;

		if (!viewsVisible) {
			resetState();
		}
	}

	/**
	 * Determines whether to show a notification based on the current state.
	 * Returns true if content has changed, displays are not visible, external screens are connected,
	 * and notifications are not disabled by user preference.
	 *
	 * @return True if a notification should be shown, false otherwise.
	 */
	private boolean notifyState() {
		if (!displayConfig.getNotifyToActivate() || userDeclined) {
			return false;
		}

		// Only notify if external screens are actually connected
		if (Screens.getScreenDevices().length <= 1) {
			return false;
		}

		return (shapeAdded || pageChanged) && !viewsVisible;
	}

	/**
	 * Displays a notification reminding the user to activate the display.
	 */
	private void showActivateNotification() {
		context.getEventBus().post(new CloseablePresenterCommand<>(RemindDisplayActivationPresenter.class, () -> {
			// User declined, so do not ask again.
			userDeclined = true;
		}));

		// TODO: When do fire this event?
		//context.getEventBus().post(new DisplayNotificationEvent(notifyState()));
	}

	/**
	 * Resets all state tracking flags to their default values.
	 * Called when the display state changes or when tracking should be restarted.
	 */
	private void resetState() {
		pageChanged = false;
		shapeAdded = false;
		userDeclined = false;
	}
}
