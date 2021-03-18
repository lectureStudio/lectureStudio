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

import javax.inject.Inject;

import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;

public class DIViewContextFactory implements ViewContextFactory {

	private final Injector injector;


	@Inject
	public DIViewContextFactory(Injector injector) {
		this.injector = injector;
	}

	@Override
	public <T> T getInstance(Class<T> cls) {
		return injector.getInstance(cls);
	}

	@Override
	public FileChooserView createFileChooserView() {
		return injector.getInstance(FileChooserView.class);
	}

	@Override
	public DirectoryChooserView createDirectoryChooserView() {
		return injector.getInstance(DirectoryChooserView.class);
	}

}
