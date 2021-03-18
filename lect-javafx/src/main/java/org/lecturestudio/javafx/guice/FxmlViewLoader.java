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

package org.lecturestudio.javafx.guice;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.inject.spi.InjectionListener;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Provider;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.BuilderFactory;

import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.javafx.view.FxmlView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FxmlViewLoader implements InjectionListener {

	private final static Logger LOG = LogManager.getLogger(FxmlViewLoader.class);

	private static final String VIEW_RESOURCES = "/resources/views/";

	private final Provider<AggregateBundle> resourceProvider;

	private final Provider<BuilderFactory> builderProvider;

	private static FxmlViewLoader instance;


	private FxmlViewLoader(Provider<AggregateBundle> resourceProvider, Provider<BuilderFactory> builderProvider) {
		this.resourceProvider = resourceProvider;
		this.builderProvider = builderProvider;
	}

	public static FxmlViewLoader getInstance(Provider<AggregateBundle> resourceProvider, Provider<BuilderFactory> builderProvider) {
		if (FxmlViewLoader.instance == null) {
			FxmlViewLoader.instance = new FxmlViewLoader(resourceProvider, builderProvider);
		}
		return FxmlViewLoader.instance;
	}

	@Override
	public void afterInjection(Object view) {
		AggregateBundle resourceBundle = resourceProvider.get();
		BuilderFactory builderFactory = builderProvider.get();

		Class<?> superClass = view.getClass().getSuperclass();

		while (nonNull(superClass) && superClass.isAnnotationPresent(FxmlView.class)) {
			FxmlView superAnnotation = superClass.getAnnotation(FxmlView.class);

			load(view, superAnnotation, resourceBundle, builderFactory);

			superClass = superClass.getSuperclass();
		}

		FxmlView viewAnnotation = view.getClass().getAnnotation(FxmlView.class);

		load(view, viewAnnotation, resourceBundle, builderFactory);
	}

	private void load(Object view, FxmlView viewAnnotation, AggregateBundle resourceBundle, BuilderFactory builderFactory) {
		String viewName = viewAnnotation.name();
		String resourcePrefix = VIEW_RESOURCES + viewName + "/" + viewName;
		String fxmlFile = resourcePrefix + ".fxml";
		String dictPath = resourcePrefix.substring(1).replaceAll("[\\\\/]", ".");

		loadDictionary(resourceBundle, dictPath);

		if (isNull(getClass().getResource(fxmlFile))) {
			return;
		}

		Parent viewNode = loadView(view, fxmlFile, resourceBundle, builderFactory);

		if (nonNull(viewNode)) {
			URL cssURL = getClass().getResource(resourcePrefix + ".css");

			if (nonNull(cssURL)) {
				viewNode.getStylesheets().add(cssURL.toExternalForm());
			}
		}
	}

	private void loadDictionary(AggregateBundle resourceBundle, String dictPath) {
		try {
			resourceBundle.load(dictPath);
		}
		catch (IOException e) {
			LOG.error("Load FXML view dictionary failed", e);
		}
	}

	private Parent loadView(Object view, String fxmlFile, ResourceBundle resourceBundle, BuilderFactory builderFactory) {
		try {
			URL fxmlURL = FxmlViewLoader.class.getResource(fxmlFile);

			FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, resourceBundle, builderFactory);
			fxmlLoader.setController(view);
			fxmlLoader.setRoot(view);

			return fxmlLoader.load();
		}
		catch (IOException e) {
			LOG.error("Load FXML view failed", e);
		}

		return null;
	}

}
