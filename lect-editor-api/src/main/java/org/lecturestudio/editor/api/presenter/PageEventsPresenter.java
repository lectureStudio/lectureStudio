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

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.edit.DeleteEventAction;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.media.recording.RecordingEvent;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.PageEventsView;
import org.lecturestudio.editor.api.view.model.PageEvent;

public class PageEventsPresenter extends Presenter<PageEventsView> {

	private final RecordingFileService recordingService;

	private final DocumentService documentService;

	private ObjectProperty<PageEvent> pageEventProperty;


	@Inject
	PageEventsPresenter(ApplicationContext context, PageEventsView view,
						DocumentService documentService,
						RecordingFileService recordingService) {
		super(context, view);

		this.documentService = documentService;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		pageEventProperty = new ObjectProperty<>();

		view.bindSelectedPageEvent(pageEventProperty);
		view.setOnDeleteEvent(this::deletePageEvent);

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
		if (event.getContentType() == Recording.Content.EVENTS) {
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
		Recording recording = recordingService.getSelectedRecording();
		RecordedEvents lectureEvents = recording.getRecordedEvents();
		PlaybackAction action = event.getPlaybackAction();
		int pageNumber = event.getPageNumber();

		try {
			DeleteEventAction deleteAction = new DeleteEventAction(lectureEvents, action, pageNumber);
			deleteAction.execute();

			lectureEvents.addEditAction(deleteAction);

			recording.fireChangeEvent(Recording.Content.EVENTS);
		}
		catch (Exception e) {
			handleException(e, "Remove event failed", "page.events.delete.error");
		}
	}

	private void loadSelectedPageEvents() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		loadPageEvents(doc.getCurrentPage());
	}

	private void loadPageEvents(Page page) {
		Recording recording = recordingService.getSelectedRecording();
		RecordedPage recordedPage = recording.getRecordedEvents().getRecordedPage(page.getPageNumber());

		List<PageEvent> eventList = new ArrayList<>();

		for (var action : recordedPage.getPlaybackActions()) {
			ActionType actionType = action.getType();

			switch (actionType) {
				case TOOL_BEGIN:
				case TOOL_EXECUTE:
				case TOOL_END:
					continue;
			}

			eventList.add(new PageEvent(action, page.getPageNumber()));
		}

		view.setPageEvents(eventList);
	}
}
