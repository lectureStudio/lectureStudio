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

package org.lecturestudio.javafx.internal.util;

import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCombination;

import org.lecturestudio.javafx.control.ExtTab;
import org.lecturestudio.javafx.control.ExtTabPane;

public class ControlAcceleratorSupport {

	public static void addAcceleratorsIntoScene(ObservableList<MenuItem> items, ExtTab anchor) {
		// with Tab, we first need to wait until the Tab has a TabPane associated with it
		addAcceleratorsIntoScene(items, (Object) anchor);
	}

	public static void removeAcceleratorsFromScene(List<? extends MenuItem> items, ExtTab anchor) {
		ExtTabPane tabPane = anchor.getTabPane();
		if (tabPane == null)
			return;

		Scene scene = tabPane.getScene();

		removeAcceleratorsFromScene(items, scene);
	}

	private static void addAcceleratorsIntoScene(ObservableList<MenuItem> items, Object anchor) {
		// with TableColumnBase, we first need to wait until it has a TableView/TreeTableView associated with it
		if (anchor == null) {
			throw new IllegalArgumentException("Anchor cannot be null");
		}

		final ReadOnlyObjectProperty<? extends Control> controlProperty = getControlProperty(anchor);
		final Control control = controlProperty.get();

		if (control == null) {
			controlProperty.addListener(new InvalidationListener() {

				@Override
				public void invalidated(Observable observable) {
					final Control control = controlProperty.get();

					if (control != null) {
						controlProperty.removeListener(this);
						addAcceleratorsIntoScene(items, control);
					}
				}
			});
		}
		else {
			addAcceleratorsIntoScene(items, control);
		}
	}

	private static void removeAcceleratorsFromScene(List<? extends MenuItem> items, Scene scene) {
		if (scene == null) {
			return;
		}

		for (final MenuItem menuitem : items) {
			if (menuitem instanceof Menu) {
				// TODO remove the menu listener from the menu.items list

				// remove the accelerators of items contained within the menu
				removeAcceleratorsFromScene(((Menu) menuitem).getItems(), scene);
			}
			else {
				// remove the removed MenuItem accelerator KeyCombination from
				// the scene accelerators map
				final Map<KeyCombination, Runnable> accelerators = scene.getAccelerators();
				accelerators.remove(menuitem.getAccelerator());
			}
		}
	}

	private static ReadOnlyObjectProperty<? extends Control> getControlProperty(Object obj) {
		if (obj instanceof TableColumn) {
			return ((TableColumn) obj).tableViewProperty();
		}
		else if (obj instanceof TreeTableColumn) {
			return ((TreeTableColumn) obj).treeTableViewProperty();
		}
		else if (obj instanceof ExtTab) {
			return ((ExtTab) obj).tabPaneProperty();
		}

		return null;
	}
}
