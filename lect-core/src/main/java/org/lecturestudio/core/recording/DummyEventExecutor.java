/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.recording;

import static java.util.Objects.isNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;

/**
 * An {@code EventExecutor} that executes all events at once resulting in a
 * recording having the final playback state.
 *
 * @author Alex Andres
 *
 * @see EventExecutor
 */
public class DummyEventExecutor extends EventExecutor {

	private final Document document;

	private final ToolController toolController;

	private final List<RecordedPage> recordedPages;


	/**
	 * Creates a new {@code DummyEventExecutor} with the provided parameters.
	 *
	 * @param document       The document to pre-load.
	 * @param toolController The tool controller to execute tool events.
	 * @param recordedPages  The recorded pages containing the events to
	 *                       execute.
	 */
	public DummyEventExecutor(Document document, ToolController toolController,
			List<RecordedPage> recordedPages) {
		this.document = document;
		this.toolController = toolController;
		this.recordedPages = recordedPages;
	}

	@Override
	public long getElapsedTime() {
		return 0;
	}

	@Override
	public int getPageNumber(int seekTime) {
		return 0;
	}

	@Override
	public synchronized int seekByTime(int seekTime) {
		return 0;
	}

	@Override
	public synchronized Integer seekByPage(int pageNumber) {
		return 0;
	}

	@Override
	protected void initInternal() {
		// No-op
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			executeEvents();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() {
		// No-op
	}

	@Override
	protected void destroyInternal() {
		// No-op
	}

	@Override
	protected void executeEvents() throws Exception {
		for (RecordedPage recPage : recordedPages) {
			loadStaticShapes(recPage);

			Stack<PlaybackAction> actions = getPlaybackActions(recPage);

			while (!actions.isEmpty()) {
				PlaybackAction action = actions.pop();
				action.execute(toolController);
			}
		}
	}

	private Stack<PlaybackAction> getPlaybackActions(RecordedPage recPage) {
		// Add page change event.
		PlaybackAction action = new PageAction(DocumentType.PDF, 0, recPage.getNumber());
		action.setTimestamp(recPage.getTimestamp());

		Stack<PlaybackAction> playbacks = new Stack<>();
		playbacks.push(action);
		playbacks.addAll(recPage.getPlaybackActions());

		Collections.reverse(playbacks);

		return playbacks;
	}

	private void loadStaticShapes(RecordedPage recPage)
			throws Exception {
		Page page = document.getPage(recPage.getNumber());

		if (isNull(page)) {
			return;
		}

		Iterator<StaticShapeAction> iter = recPage.getStaticActions().iterator();

		if (iter.hasNext()) {
			// Remember currently selected page.
			int lastPageNumber = document.getCurrentPageNumber();

			// Select the page to which to add static actions.
			document.selectPage(recPage.getNumber());

			while (iter.hasNext()) {
				StaticShapeAction staticAction = iter.next();
				PlaybackAction action = staticAction.getAction();

				// Execute static action on selected page.
				action.execute(toolController);
			}

			// Go back to the page which was selected prior preloading.
			document.selectPage(lastPageNumber);

			page.sendChangeEvent();
		}
	}
}
