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

package org.lecturestudio.editor.api.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class DeleteEventsAction extends RecordedObjectAction<RecordedEvents> {

	private final List<RecordedPage> backupPages = new ArrayList<>();

	private final List<Integer> removedPages = new ArrayList<>();

	private final Map<Integer, Interval<Integer>> pageChanges = new HashMap<>();

	private final Interval<Integer> editInterval;

	private Integer shiftPage;


	public DeleteEventsAction(RecordedEvents lectureObject, Interval<Integer> interval) {
		super(lectureObject);

		this.editInterval = interval;
	}

	public void removeRecordedPage(int number) {
		removedPages.add(number);
	}

	public void changeRecordedPage(Integer number, Interval<Integer> interval) {
		pageChanges.put(number, interval);
	}

	public void setShiftPage(Integer number) {
		this.shiftPage = number;
	}

	@Override
	public void execute() {
		RecordedEvents lecturePages = getRecordedObject();
		List<RecordedPage> recPages = lecturePages.getRecordedPages();
		int lastRemoved = 0;

		// Create backup.
		backupPages.clear();
		for (RecordedPage page : recPages) {
			backupPages.add(page.clone());
		}

		// Remove pages.
		for (Integer number : removedPages) {
			lecturePages.removePage(number);
			lastRemoved = number;
		}

		// Cut page content.
		for (Map.Entry<Integer, Interval<Integer>> entry : pageChanges.entrySet()) {
			Integer number = entry.getKey();
			Interval<Integer> interval = entry.getValue();

			RecordedPage recPage = lecturePages.getRecordedPage(number);
			recPage.cut(interval);
		}

		if (shiftPage != null) {
			RecordedPage recPage = lecturePages.getRecordedPage(shiftPage);
			recPage.setTimestamp(editInterval.getStart());
		}

		// Shift page numbers and time stamps.
		for (RecordedPage page : recPages) {
			page.shift(editInterval);

			if (page.getNumber() > lastRemoved) {
				page.setNumber(page.getNumber() - removedPages.size());
			}
		}
	}

	@Override
	public void undo() {
		RecordedEvents lecturePages = getRecordedObject();
		List<RecordedPage> recPages = lecturePages.getRecordedPages();

		recPages.clear();
		recPages.addAll(backupPages);
	}

	@Override
	public void redo() {
		execute();
	}

}
