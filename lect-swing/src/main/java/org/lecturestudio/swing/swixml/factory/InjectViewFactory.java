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

package org.lecturestudio.swing.swixml.factory;

import java.awt.LayoutManager;
import java.lang.reflect.InvocationTargetException;

import org.lecturestudio.core.inject.Injector;

import org.swixml.Parser;
import org.swixml.factory.BeanFactory;
import org.w3c.dom.Element;

public class InjectViewFactory extends BeanFactory {

	protected final Injector injector;


	public InjectViewFactory(Class<?> viewClass, Injector injector) {
		super(viewClass);

		this.injector = injector;
	}

	@Override
	public Object create(Object owner, Element element) {
		return injector.getInstance(getTemplate());
	}

	@Override
	public Object newInstance(Object... parameter)
			throws InvocationTargetException {
		try {
			return injector.getInstance(getTemplate());
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	@Override
	public boolean process(Parser p, Object parent, Element child,
			LayoutManager layoutMgr) {
		return false;
	}

}
