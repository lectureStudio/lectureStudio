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

package org.lecturestudio.core.recording;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class RecordedEvents extends RecordedObjectBase {

	private List<RecordedPage> recordedPages;


	public RecordedEvents(List<RecordedPage> recordedPages) {
		this.recordedPages = recordedPages;
	}

	public RecordedEvents(byte[] input) throws IOException {
		parseFrom(input);
	}

	public List<RecordedPage> getRecordedPages() {
		return recordedPages;
	}

	public void removePage(int number) {
		for (RecordedPage page : recordedPages) {
			if (page.getNumber() == number) {
				recordedPages.remove(page);
				return;
			}
		}
	}

	public RecordedPage getRecordedPage(int number) {
		for (RecordedPage page : recordedPages) {
			if (page.getNumber() == number) {
				return page;
			}
		}
		return null;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream actionStream = new ByteArrayOutputStream();

		for (RecordedPage recPage : recordedPages) {
			byte[] pageData = recPage.toByteArray();
			actionStream.write(pageData);
		}

		actionStream.close();

		return actionStream.toByteArray();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		recordedPages = new LinkedList<>();

		while (buffer.hasRemaining()) {
			// Read the page data size.
			int entryLength = buffer.getInt();

			// Read page data.
			byte[] pageData = new byte[entryLength];
			buffer.get(pageData);

			recordedPages.add(new RecordedPage(pageData));
		}
	}

}
