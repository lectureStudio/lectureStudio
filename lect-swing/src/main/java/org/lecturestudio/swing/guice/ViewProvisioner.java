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

package org.lecturestudio.swing.guice;

import com.google.inject.spi.ProvisionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.inject.Injector;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.swing.swixml.ViewFactory;
import org.lecturestudio.swing.swixml.ViewLoader;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ViewProvisioner implements ProvisionListener {

	private final static Logger LOG = LogManager.getLogger(ViewProvisioner.class);

	private static final String VIEW_RESOURCES = "/resources/views/";

	private final Provider<Injector> injectorProvider;

	private final Provider<AggregateBundle> resourceProvider;

	private final Map<Class<?>, Class<?>> viewMap;


	public ViewProvisioner(Provider<Injector> injectorProvider,
			Provider<AggregateBundle> resourceProvider,
			Map<Class<?>, Class<?>> viewMap) {
		this.injectorProvider = injectorProvider;
		this.resourceProvider = resourceProvider;
		this.viewMap = viewMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void onProvision(ProvisionInvocation<T> invocation) {
		Class<? super T> cls = invocation.getBinding().getKey().getTypeLiteral().getRawType();
		T view = invocation.provision();

		AggregateBundle resourceBundle = resourceProvider.get();
		Class<?> superClass = cls.getSuperclass();

		while (nonNull(superClass) && superClass.isAnnotationPresent(SwingView.class)) {
			SwingView superAnnotation = superClass.getAnnotation(SwingView.class);

			loadDictionary(superAnnotation, resourceBundle);
			initView((JComponent) view, superAnnotation.name(), resourceBundle);

			superClass = superClass.getSuperclass();
		}

		SwingView viewAnnotation = cls.getAnnotation(SwingView.class);

		loadDictionary(viewAnnotation, resourceBundle);

		Class<? extends Presenter<?>> presenterClass = (Class<? extends Presenter<?>>) viewAnnotation.presenter();

		initView((Container) view, viewAnnotation.name(), resourceBundle);
		initPresenter(view, presenterClass);
	}

	private void loadDictionary(SwingView viewAnnotation, AggregateBundle resourceBundle) {
		String viewName = viewAnnotation.name();
		String resourcePrefix = VIEW_RESOURCES + viewName + "/" + viewName;
		String dictPath = resourcePrefix.substring(1).replaceAll("[\\\\/]", ".");

		loadDictionary(resourceBundle, dictPath);
	}

	private void loadDictionary(AggregateBundle resourceBundle, String dictPath) {
		try {
			resourceBundle.load(dictPath);
		}
		catch (IOException e) {
			LOG.error("Load view dictionary failed", e);
		}
	}

	private <T extends Container> void initView(T view, String viewName, AggregateBundle resourceBundle) {
		if (isNull(viewName) || viewName.isEmpty()) {
			return;
		}

		String resourcePrefix = VIEW_RESOURCES + viewName + "/" + viewName;
		String xmlFile = resourcePrefix + ".xml";

		URL xmlUrl = getClass().getResource(xmlFile);

		if (isNull(xmlUrl)) {
			return;
		}

		Injector injector = injectorProvider.get();
		ViewLoader<T> viewLoader = new ViewLoader<>(view, resourceBundle, injector);

		for (var entry : viewMap.entrySet()) {
			viewLoader.getTaglib().registerTag(entry.getKey().getSimpleName(),
					new ViewFactory(injector, entry.getValue()));
		}

		try {
			viewLoader.render(xmlUrl);

			for (Method method : view.getClass().getDeclaredMethods()) {
				if (method.isAnnotationPresent(ViewPostConstruct.class) &&
					method.getParameterCount() == 0) {
					method.setAccessible(true);
					method.invoke(view);
				}
			}
		}
		catch (Exception e) {
			LOG.error("Load XML view failed", e);
		}
	}

	private <T> void initPresenter(T view, Class<? extends Presenter<?>> presenterClass) {
		if (isNull(presenterClass)) {
			return;
		}

		Injector injector = injectorProvider.get();
		List<?> interfaces = List.of(view.getClass().getInterfaces());

		for (Constructor<?> ctor : presenterClass.getDeclaredConstructors()) {
			if (ctor.isAnnotationPresent(Inject.class)) {
				List<Object> params = new ArrayList<>();

				for (Class<?> paramCls : ctor.getParameterTypes()) {
					if (interfaces.contains(paramCls)) {
						params.add(view);
					}
					else {
						params.add(injector.getInstance(paramCls));
					}
				}

				try {
					ctor.setAccessible(true);

					Presenter<?> presenter = (Presenter<?>) ctor.newInstance(params.toArray());

					for (Field field : presenterClass.getDeclaredFields()) {
						if (field.isAnnotationPresent(Inject.class)) {
							field.setAccessible(true);
							field.set(presenter, injector.getInstance(field.getType()));
						}
					}

					presenter.initialize();
				}
				catch (Exception e) {
					LOG.error("Initialize view-presenter failed", e);
				}
			}
		}
	}
}
