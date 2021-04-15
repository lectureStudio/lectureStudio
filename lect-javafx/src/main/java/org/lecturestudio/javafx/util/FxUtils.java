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

package org.lecturestudio.javafx.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;

public final class FxUtils {

	public static Parent load(String fxmlPath, ResourceBundle resources, Object controller) {
		return load(fxmlPath, resources, controller, null);
	}

	public static Parent load(String fxmlPath, ResourceBundle resources, Object controller, Object root) {
		URL fxmlURL = FxUtils.class.getResource(fxmlPath);

		FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, resources);
		fxmlLoader.setController(controller);
		fxmlLoader.setRoot(root);

		Parent parent;

		try {
			parent = fxmlLoader.load();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return parent;
	}

	public static void callback(Runnable callback) {
		if (nonNull(callback)) {
			callback.run();
		}
	}

	/**
	 * Run this Runnable in the JavaFX Application Thread. This method can be
	 * called whether or not the current thread is the JavaFX Application
	 * Thread.
	 * 
	 * @param runnable The code to be executed in the JavaFX Application Thread.
	 */
	public static void invoke(Runnable runnable) {
		if (isNull(runnable)) {
			return;
		}

		try {
			if (Platform.isFxApplicationThread()) {
				runnable.run();
			}
			else {
				Platform.runLater(runnable);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void getAllNodesInParent(Parent parent, String selector, Set<Node> results) {
		Set<Node> result = parent.lookupAll(selector);

		if (!result.isEmpty()) {
			results.addAll(result);
		}

		for (Node child : parent.getChildrenUnmodifiable()) {
			if (child instanceof Parent) {
				getAllNodesInParent((Parent) child, selector, results);
			}
		}
	}

	public static void bindAction(ButtonBase button, Action action) {
		requireNonNull(button);
		requireNonNull(action);

		button.addEventHandler(ActionEvent.ACTION, event -> action.execute());
	}

	public static void bindAction(ToggleButton toggle, ConsumerAction<Boolean> action) {
		requireNonNull(toggle);
		requireNonNull(action);

		toggle.addEventHandler(ActionEvent.ACTION, event -> action.execute(toggle.isSelected()));
	}

	public static void bindAction(CheckBox checkBox, ConsumerAction<Boolean> action) {
		requireNonNull(checkBox);
		requireNonNull(action);

		checkBox.addEventHandler(ActionEvent.ACTION, event -> action.execute(checkBox.isSelected()));
	}

	public static void bindAction(MenuItem menuItem, Action action) {
		requireNonNull(menuItem);
		requireNonNull(action);

		menuItem.addEventHandler(ActionEvent.ACTION, event -> action.execute());
	}

	public static void bindAction(CheckMenuItem menuItem, ConsumerAction<Boolean> action) {
		requireNonNull(menuItem);
		requireNonNull(action);

		menuItem.addEventHandler(ActionEvent.ACTION, event -> action.execute(menuItem.isSelected()));
	}
}
