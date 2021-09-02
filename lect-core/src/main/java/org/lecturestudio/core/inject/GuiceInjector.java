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

package org.lecturestudio.core.inject;

import com.google.inject.Guice;
import com.google.inject.Module;

import java.util.ArrayList;
import java.util.List;

public class GuiceInjector implements Injector {

	private final com.google.inject.Injector injector;


	/**
	 * Creates a new instance of {@link GuiceInjector} with the specified modules.
	 *
	 * @param modules The modules.
	 */
	public GuiceInjector(Module... modules) {
		List<Module> list = new ArrayList<>(List.of(modules));
		list.add(binder -> binder.bind(Injector.class).toInstance(this));

		injector = Guice.createInjector(list);
	}

	@Override
	public <T> T getInstance(Class<T> cls) {
		return injector.getInstance(cls);
	}
}
