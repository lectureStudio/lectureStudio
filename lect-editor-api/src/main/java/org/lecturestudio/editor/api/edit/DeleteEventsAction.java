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

import java.util.*;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class DeleteEventsAction extends RecordedObjectAction<RecordedEvents> {

	private final List<RecordedPage> backupPages = new ArrayList<>();

	private final List<Integer> removedPages = new ArrayList<>();

	private final Map<Integer, Interval<Integer>> pageChanges = new HashMap<>();

	private final Interval<Integer> editInterval;

	private Integer shiftPage;

	private Recording recording;

	private HashMap<String, Recording.ToolDemoRecording> backupToolDemoRecordingsHashMap = new HashMap<>();

	public DeleteEventsAction(RecordedEvents lectureObject, Interval<Integer> interval, Recording recording) {
		super(lectureObject);
		this.recording = recording;
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
		for (
				Integer number : removedPages) {
			lecturePages.removePage(number);
			lastRemoved = number;
		}

		// Cut page content.
		for (
				Map.Entry<Integer, Interval<Integer>> entry : pageChanges.entrySet()) {
			Integer number = entry.getKey();
			Interval<Integer> interval = entry.getValue();

			RecordedPage recPage = lecturePages.getRecordedPage(number);
			recPage.cut(interval);
		}

		if (shiftPage != null) {
			RecordedPage recPage = lecturePages.getRecordedPage(shiftPage);
			recPage.setTimestamp(editInterval.getStart());
		}

		//TODO TBD
		//for (Recording.ToolDemoRecording toolDemoRecording : toolDemoRecordings) {
		//	if (editInterval.getEnd() < toolDemoRecording.interval().getStart()) {
		//		toolDemoRecording.interval().set(toolDemoRecording.interval().getStart() - (editInterval.getEnd() - editInterval.getStart()), toolDemoRecording.interval().getEnd() - (editInterval.getEnd() - editInterval.getStart()));
		//	}
		//}

		//Adjusts the interval of the toolDemoRecordings and the adjusted entities to a backup hashmap.
		backupToolDemoRecordingsHashMap.clear();
		Iterator<Recording.ToolDemoRecording> i = recording.getToolDemoRecordingsData().iterator();
		while (i.hasNext()) {
			Recording.ToolDemoRecording toolDemoRecording = i.next();
			if (editInterval.getEnd() < toolDemoRecording.interval().getStart()) {
				backupToolDemoRecordingsHashMap.put(toolDemoRecording.fileName(), toolDemoRecording.clone());
				toolDemoRecording.interval().set(toolDemoRecording.interval().getStart() - (editInterval.getEnd() - editInterval.getStart()), toolDemoRecording.interval().getEnd() - (editInterval.getEnd() - editInterval.getStart()));
			} else if (editInterval.getEnd() >= toolDemoRecording.interval().getStart() && editInterval.getStart() <= toolDemoRecording.interval().getEnd()) {
				backupToolDemoRecordingsHashMap.put(toolDemoRecording.fileName(), toolDemoRecording.clone());
				toolDemoRecording.interval().set(-1L, -1L);
			}
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

		//TODO TBD
		//for (Recording.ToolDemoRecording toolDemoRecording : recording.getToolDemoRecordingsData()) {
		//	if (editInterval.getEnd() < toolDemoRecording.interval().getStart() + (editInterval.getEnd() - editInterval.getStart())) {
		//		toolDemoRecording.interval().set(backupToolDemoRecordings.get(0).interval().getStart(), backupToolDemoRecordings.get(0).interval().getEnd());
		//	}
		//}
		//TODO TBD
		//Non working but optimal option just revert the whole lists, that really doesnt matter even with 10 tool demos the performance wont tank
		//recording.setToolDemoRecordingsData(new ArrayList<>());
		//for (Recording.ToolDemoRecording toolDemoRecording : backupToolDemoRecordings) {
		//	recording.addToolDemoRecordingsData(toolDemoRecording);
		//}

		//Restores the original interval of the edited toolDemos.
		for (Recording.ToolDemoRecording toolDemoRecording : recording.getToolDemoRecordingsData()) {
			Recording.ToolDemoRecording backupRecoding = backupToolDemoRecordingsHashMap.get(toolDemoRecording.fileName());
			if (backupRecoding != null) {
				toolDemoRecording.interval().set(backupRecoding.interval().getStart(), backupRecoding.interval().getEnd());
			}
		}

		recPages.clear();
		recPages.addAll(backupPages);
	}

	@Override
	public void redo() {
		execute();
	}

}
