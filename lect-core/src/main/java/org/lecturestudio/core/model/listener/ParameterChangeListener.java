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

package org.lecturestudio.core.model.listener;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.PresentationParameter;

/**
 * Listens for ParameterChanged events on a specific page.
 * 
 * @author Tobias
 * 
 */
public interface ParameterChangeListener {

	/**
	 * Returns the Page for which the listener applies at the moment.
	 * 
	 * @return
	 */
	Page forPage();

	/**
	 * Fired when the PresentationParameter for Page p has changed.
	 * 
	 * @param p
	 * @param parameter
	 */
	void parameterChanged(Page p, PresentationParameter parameter);

}
