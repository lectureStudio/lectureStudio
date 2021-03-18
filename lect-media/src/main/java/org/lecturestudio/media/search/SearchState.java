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

public class SearchState {

	private final SearchResult searchResult;

	private int selectedIndex;


	public SearchState(SearchResult searchResult) {
		this.searchResult = searchResult;
		this.selectedIndex = 0;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	public int selectPreviousIndex() {
		selectedIndex = Math.max(selectedIndex - 1, 1);
		return searchResult.getPageIndices().get(selectedIndex - 1);
	}

	public int selectNextIndex() {
		selectedIndex = Math.min(selectedIndex + 1, getTotalHits());
		return searchResult.getPageIndices().get(selectedIndex - 1);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public int getTotalHits() {
		return searchResult.getPageIndices().size();
	}
}
