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

package org.lecturestudio.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an outline item in a document.
 *
 * @author Alex Andres
 */
public class DocumentOutlineItem {

	private final String title;

	private final Integer pageNumber;

	private final List<DocumentOutlineItem> children;


	public DocumentOutlineItem() {
		this(null, null);
	}

	public DocumentOutlineItem(String title, Integer pageNumber) {
		this.title = title;
		this.pageNumber = pageNumber;
		this.children = new ArrayList<>();
	}

	/**
	 * Get the title of this outline item.
	 *
	 * @return The title of this item.
	 */
	public String getTitle() {
		return title;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public List<DocumentOutlineItem> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "DocumentOutlineItem{" + "title='" + title + '\''
				+ ", pageNumber=" + pageNumber + '}';
	}
}
