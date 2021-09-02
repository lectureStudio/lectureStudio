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

import static java.util.Objects.nonNull;

import java.util.Iterator;
import java.util.List;

public class DocumentOutline extends DocumentOutlineItem {

	/**
	 * Create a new {@link DocumentOutline}.
	 * (Calls {@link DocumentOutlineItem#DocumentOutlineItem()})
	 */
	public DocumentOutline() {
		super();
	}

	public DocumentOutlineItem getOutlineItem(Integer pageNumber) {
		return getOutlineItem(getChildren(), pageNumber);
	}

	private static DocumentOutlineItem getOutlineItem(List<DocumentOutlineItem> itemList, Integer pageNumber) {
		Iterator<DocumentOutlineItem> iterator = itemList.iterator();

		if (iterator.hasNext()) {
			DocumentOutlineItem item1 = iterator.next();
			Integer pageNum1 = item1.getPageNumber();

			if (pageNum1.equals(pageNumber)) {
				return item1;
			}
			if (pageNum1 > pageNumber) {
				return null;
			}

			while (iterator.hasNext()) {
				DocumentOutlineItem item2 = iterator.next();
				Integer pageNum2 = item2.getPageNumber();

				if (pageNum2.equals(pageNumber)) {
					return item2;
				}

				boolean inRange = pageNumber < pageNum2;
				boolean isLast = !iterator.hasNext();

				if (inRange || isLast) {
					DocumentOutlineItem item = inRange ? item1 : item2;

					if (!item.getChildren().isEmpty()) {
						DocumentOutlineItem nested = getOutlineItem(item.getChildren(), pageNumber);

						if (nonNull(nested)) {
							item = nested;
						}
					}

					return item;
				}

				item1 = item2;
			}
		}

		return null;
	}
}
