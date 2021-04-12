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
package org.lecturestudio.core.bus.event;

import org.lecturestudio.core.model.Page;

public class PageEvent extends BusEvent {
	
	public enum Type { CREATED, REMOVED, SELECTED };

	private final Page page;
	private final Page oldPage;
	
	private final Type type;
	
	
	public PageEvent(Page page, Type type) {
		this(page, null, type);
	}
	
	public PageEvent(Page page, Page oldPage, Type type) {
		this.page = page;
		this.oldPage = page;
		this.type = type;
	}
	
	public Page getPage() {
		return page;
	}
	
	public Page getOldPage() {
		return oldPage;
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean isCreated() {
		return type == Type.CREATED;
	}
	
	public boolean isRemoved() {
		return type == Type.REMOVED;
	}
	
	public boolean isSelected() {
		return type == Type.SELECTED;
	}
	
}
