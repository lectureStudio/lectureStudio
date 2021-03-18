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

package org.lecturestudio.core.model.action;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;

/**
 * Abstract class for an action executed on a {@link Page} object. Redo- and
 * undoable actions should be implemented as subclasses of AbstractAction.
 *
 * @author Tobias
 */
public abstract class ShapeAction {

	private final List<Shape> shapes = new ArrayList<>();

	private final Page page;


	public ShapeAction(Page page) {
		this.page = page;
	}

	/**
	 * Executes the action
	 */
	public abstract void execute();

	/**
	 * Undoes the action. Should have exactly the reverse effect as
	 * execute/redo.
	 */
	public abstract void undo();

	/**
	 * Redoes the action. Should have exactly the reverse effect as undo.
	 */
	public abstract void redo();


	public Page getPage() {
		return page;
	}

	public List<Shape> getShapes() {
		return shapes;
	}

}
