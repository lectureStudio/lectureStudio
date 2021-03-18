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

package org.lecturestudio.core.service;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.model.RecentDocument;

@Singleton
public class RecentDocumentService {

	private final RecentDocumentSource source;


	@Inject
	public RecentDocumentService(RecentDocumentSource source) {
		this.source = source;
	}

	public void add(RecentDocument document) {
		source.delete(document);

		int count = getRecentDocuments().size();
		if (count > 4) {
			// Delete the least recent.
			source.deleteAtIndex(count - 1);
		}

		source.addAtIndex(0, document);
	}

	public void delete(RecentDocument document) {
		source.delete(document);
	}

	public List<RecentDocument> getRecentDocuments() {
		List<RecentDocument> list = source.getAll();
		Iterator<RecentDocument> iter = list.iterator();

		while (iter.hasNext()) {
			RecentDocument doc = iter.next();

			File file = new File(doc.getDocumentPath());
			if (!file.exists()) {
				iter.remove();
				source.delete(doc);
			}
		}

		return list;
	}

}
