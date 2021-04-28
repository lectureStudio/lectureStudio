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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;

/**
 * An {@code EventExecutor} that executes all events at once resulting in a
 * recording having the final playback state.
 *
 * @author Alex Andres
 *
 * @see EventExecutor
 */
public class DummyEventExecutor extends EventExecutor {

	private final ToolController toolController;

	private final List<RecordedPage> recordedPages;

	private Stack<PlaybackAction> playbacks;

	private Map<Integer, Integer> pageChangeEvents;


	/**
	 * Creates a new {@code DummyEventExecutor} with the provided parameters.
	 *
	 * @param toolController The tool controller to execute tool events.
	 * @param recordedPages  The recorded pages containing the events to
	 *                       execute.
	 */
	public DummyEventExecutor(ToolController toolController,
			List<RecordedPage> recordedPages) {
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
		playbacks = new Stack<>();
		pageChangeEvents = new LinkedHashMap<>();

		for (RecordedPage recPage : recordedPages) {
			pageChangeEvents.put(recPage.getNumber(), recPage.getTimestamp());
		}

		getPlaybackActions(0);
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
		getPlaybackActions(0);
	}

	@Override
	protected void destroyInternal() {
		playbacks.clear();
		pageChangeEvents.clear();
	}

	@Override
	protected void executeEvents() throws Exception {
		for (Integer pageNumber : pageChangeEvents.keySet()) {
			getPlaybackActions(pageNumber);

			while (!playbacks.isEmpty()) {
				PlaybackAction action = playbacks.peek();

				action.execute(toolController);

				playbacks.pop();
			}
		}
	}

	private synchronized void getPlaybackActions(int pageNumber) {
		RecordedPage recPage = recordedPages.get(pageNumber);

		// Add page change event.
		PlaybackAction action = new PageAction(DocumentType.PDF, 0, pageNumber);
		action.setTimestamp(recPage.getTimestamp());

		playbacks.clear();
		playbacks.push(action);
		playbacks.addAll(recPage.getPlaybackActions());

		if (!playbacks.empty()) {
			Collections.reverse(playbacks);
		}
	}
}
