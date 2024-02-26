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

package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.edit.DeletePageActions;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.view.PageEventsView;
import org.lecturestudio.editor.api.view.model.PageEvent;
import org.lecturestudio.media.recording.RecordingEvent;

public class PageEventsPresenter extends Presenter<PageEventsView> {

	private final RecordingFileService recordingService;

	private final RecordingPlaybackService playbackService;

	private final DocumentService documentService;

	private ObjectProperty<PageEvent> pageEventProperty;


	@Inject
	PageEventsPresenter(ApplicationContext context, PageEventsView view,
			DocumentService documentService,
			RecordingPlaybackService playbackService,
			RecordingFileService recordingService) {
		super(context, view);

		this.documentService = documentService;
		this.playbackService = playbackService;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		pageEventProperty = new ObjectProperty<>();

		view.bindSelectedPageEvent(pageEventProperty);
		view.setOnDeleteEvent(this::deletePageEvent);
		view.setOnSelectEvent(this::selectPageEvent);

		context.getEventBus().register(this);
	}

	@Override
	public void destroy() {
		context.getEventBus().unregister(this);
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.selected()) {
			loadSelectedPageEvents();
		}
	}

	@Subscribe
	public void onEvent(RecordingChangeEvent event) {
		switch (event.getContentType()) {
			case ALL, EVENTS_ADDED, EVENTS_CHANGED, EVENTS_REMOVED ->
					loadSelectedPageEvents();
		}
	}

	@Subscribe
	public void onEvent(org.lecturestudio.core.bus.event.PageEvent event) {
		if (event.isSelected()) {
			loadPageEvents(event.getPage());
		}
	}

	private void deletePageEvent(PageEvent event) {
		CompletableFuture.runAsync(() -> {
			try {
				List<PlaybackAction> compositeActions = event.getCompositeActions();
				int pageNumber = event.getPageNumber();

				deletePageActions(compositeActions, pageNumber);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		}).exceptionally(throwable -> {
			handleException(throwable, "Remove action failed",
					"page.events.delete.error");
			return null;
		}).join();
	}

	/**
	 * Moves the current timestamp to right before the PlaybackAction
	 *
	 * @param event the PlaybackAction, which timestamp should be selected
	 */
	private void selectPageEvent(PageEvent event) {
		if (Objects.isNull(event)) {
			return;
		}

		try {
			playbackService.seek((int) event.getTime().getMillis() - 20);
		}
		catch (ExecutableException e) {
			handleException(e, "Seek failed", "page.events.seek.error");
		}
	}

	private void deletePageActions(List<PlaybackAction> actions,
			int pageNumber) throws ExecutableException, RecordingEditException {
		if (playbackService.started()) {
			playbackService.suspend();
		}

		Recording recording = recordingService.getSelectedRecording();

		addEditAction(recording, new DeletePageActions(recording, actions,
				pageNumber));
	}

	private void loadSelectedPageEvents() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		loadPageEvents(doc.getCurrentPage());
	}

	private void loadPageEvents(Page page) {
		Recording recording = recordingService.getSelectedRecording();
		RecordedPage recordedPage = recording.getRecordedEvents()
				.getRecordedPage(page.getPageNumber());

		List<PageEvent> eventList = new ArrayList<>();
		PlaybackAction previousAction = null;
		Integer previousEndTs = null;

		for (var action : recordedPage.getPlaybackActions()) {
			ActionType actionType = action.getType();

			// Exclude the following Types from being shown in the EventList
			switch (actionType) {
				case TOOL_BEGIN:
				case TOOL_EXECUTE:
				case TEXT_CHANGE:
				case TEXT_FONT_CHANGE:
				case TEXT_LOCATION_CHANGE:
					continue;
				case TOOL_END:
					previousEndTs = action.getTimestamp();
					continue;
			}

			// Do not show the action in the list, if it has the same handle
			// as the previous action.
			if (isNull(previousAction)
					|| !action.getClass().equals(previousAction.getClass())
					|| !action.hasHandle()
					|| !previousAction.hasHandle()
					|| action.getHandle() != previousAction.getHandle()) {
				if (nonNull(previousAction) && nonNull(previousEndTs)
						&& Math.abs(action.getTimestamp() - previousEndTs) < -1000
						&& action.getClass().equals(previousAction.getClass())) {
					// This is a composite action.
					PageEvent initEvent = eventList.get(eventList.size() - 1);
					initEvent.getCompositeActions().add(action);
				}
				else {
					// This is an individual action.
					eventList.add(new PageEvent(action, page.getPageNumber()));
				}
			}

			previousAction = action;
		}

		view.setPageEvents(eventList);
	}

	private void addEditAction(Recording recording, EditAction action)
			throws RecordingEditException {
		recording.getEditManager().addEditAction(action);

		updateEditState(recording);
	}

	private void updateEditState(Recording recording) {
		EditorContext editorContext = (EditorContext) context;

		editorContext.setCanRedo(recording.getEditManager().hasRedoActions());
		editorContext.setCanUndo(recording.getEditManager().hasUndoActions());
	}
}
