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

package org.lecturestudio.presenter.javafx.view;

import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.SettingsBaseView;
import org.lecturestudio.presenter.api.view.SettingsView;

@FxmlView(name = "main-settings")
public class FxSettingsView extends TabPane implements SettingsView {

	private final SimpleBooleanProperty extendedSettings;

	private final Set<Node> extendedNodes;


	public FxSettingsView() {
		super();

		extendedSettings = new SimpleBooleanProperty();
		extendedNodes = new HashSet<>();
	}

	@Override
	public void setAdvancedSettings(boolean selected) {
		FxUtils.invoke(() -> {
			extendedSettings.set(selected);
		});
	}

	@Override
	public void setSettingsPath(String path) {
		for (Tab tab : getTabs()) {
			if (tab.getId().equals(path)) {
				getSelectionModel().select(tab);
				break;
			}
		}
	}

	@Override
	public void setOnClose(Action action) {
		for (Tab tab : getTabs()) {
			Node content = tab.getContent();

			if (SettingsBaseView.class.isAssignableFrom(content.getClass())) {
				SettingsBaseView settingsView = (SettingsBaseView) content;
				settingsView.setOnClose(action);
			}
		}
	}

	@FXML
	private void initialize() {
		FxUtils.getAllNodesInParent(this, "#extendedUI", extendedNodes);

		showAdvancedSettings(extendedSettings.get());

		extendedSettings.addListener((observable, oldValue, newValue) -> {
			showAdvancedSettings(newValue);
		});
	}

	private void showAdvancedSettings(boolean show) {
		for (Node node : extendedNodes) {
			node.setVisible(show);
			node.setManaged(show);
		}
	}

}
