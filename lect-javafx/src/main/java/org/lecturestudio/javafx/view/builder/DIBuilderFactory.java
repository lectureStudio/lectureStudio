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

package org.lecturestudio.javafx.view.builder;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;

import org.lecturestudio.core.inject.Injector;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.javafx.control.ExtSplitMenuButton;
import org.lecturestudio.javafx.inject.ViewPresenterBuilder;
import org.lecturestudio.javafx.view.FxmlView;

public class DIBuilderFactory implements BuilderFactory {

	private static final String JAVAFX_PACKAGE = "javafx";

	private final BuilderFactory baseFactory;

	private final Injector injector;


	@Inject
	public DIBuilderFactory(Injector injector) {
		this.baseFactory = new JavaFXBuilderFactory();
		this.injector = injector;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Builder<?> getBuilder(Class<?> type) {
		if (type.getPackageName().startsWith(JAVAFX_PACKAGE)) {
			// No injection for standard classes.
			return baseFactory.getBuilder(type);
		}

		if (type.isAnnotationPresent(FxmlView.class)) {
			FxmlView viewAnnotation = type.getAnnotation(FxmlView.class);
			Class<? extends Presenter<?>> presenterClass = (Class<? extends Presenter<?>>) viewAnnotation.presenter();

			return new ViewPresenterBuilder(injector, presenterClass);
		}
		else if (ExtSplitMenuButton.class.isAssignableFrom(type)) {
			return createButtonBuilder((Class<? extends ExtSplitMenuButton>) type);
		}

		Constructor<?>[] constructors = type.getDeclaredConstructors();

		for (Constructor<?> constructor : constructors) {
			if (constructor.isAnnotationPresent(Inject.class)) {
				return (Builder<Object>) () -> injector.getInstance(type);
			}
		}

		return baseFactory.getBuilder(type);
	}

	private <T extends ExtSplitMenuButton> Builder<T> createButtonBuilder(Class<T> cls) {
		return new ExtSplitMenuButtonBuilder<>(injector, cls);
	}
}
