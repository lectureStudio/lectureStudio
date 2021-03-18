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

package org.lecturestudio.media.search;

import java.util.List;

public class SearchResult {

	private final String queryString;

	private final List<Integer> pageIndices;

	private final List<String> suggestions;


	public SearchResult(String queryString, List<Integer> pageIndices, List<String> suggestions) {
		this.queryString = queryString;
		this.pageIndices = pageIndices;
		this.suggestions = suggestions;
	}

	public String getQueryString() {
		return queryString;
	}

	public List<Integer> getPageIndices() {
		return pageIndices;
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	@Override
	public String toString() {
		return String.format(
				"%s [queryString=%s, pageIndices=%s, suggestions=%s]",
				SearchResult.class.getSimpleName(), queryString, pageIndices,
				suggestions);
	}
}
