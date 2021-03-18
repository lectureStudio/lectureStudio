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

package org.lecturestudio.editor.javafx;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.ApplicationFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.inject.Injector;
import org.lecturestudio.editor.api.presenter.MainPresenter;
import org.lecturestudio.editor.javafx.inject.guice.ApplicationModule;
import org.lecturestudio.editor.javafx.inject.guice.ViewModule;

public class EditorFxFactory implements ApplicationFactory {

	private final Injector injector;


	public EditorFxFactory() {
		injector = new GuiceInjector(new ApplicationModule(), new ViewModule());
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return injector.getInstance(ApplicationContext.class);
	}

	@Override
	public org.lecturestudio.core.presenter.MainPresenter<?> getStartPresenter() {
		return injector.getInstance(MainPresenter.class);
	}
}
