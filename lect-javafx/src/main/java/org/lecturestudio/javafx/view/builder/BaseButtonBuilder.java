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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import javafx.util.Builder;

import org.lecturestudio.core.inject.Injector;

public class BaseButtonBuilder<T extends ButtonBase> implements Builder<T> {

	private final Injector injector;

	private final Class<T> buttonClass;

	private KeyCombination accelerator;

	private Node graphic;

	private ToggleGroup toggleGroup;

	private Tooltip tooltip;

	private String userData;

	private String styleClass;


	BaseButtonBuilder(Injector injector, Class<T> buttonClass) {
		this.injector = injector;
		this.buttonClass = buttonClass;
	}

	@Override
	public T build() {
		T button = injector.getInstance(buttonClass);
		button.getStyleClass().addAll(getStyleClasses());
		button.setGraphic(getGraphic());
		button.setTooltip(getTooltip());
		button.setUserData(getUserData());

		return button;
	}

	public KeyCombination getAccelerator() {
		return accelerator;
	}

	public void setAccelerator(KeyCombination accelerator) {
		this.accelerator = accelerator;
	}

	public ToggleGroup getToggleGroup() {
		return toggleGroup;
	}

	public void setToggleGroup(ToggleGroup toggleGroup) {
		this.toggleGroup = toggleGroup;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	public Node getGraphic() {
		return graphic;
	}

	public void setGraphic(Node graphic) {
		this.graphic = graphic;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	List<String> getStyleClasses() {
		List<String> classes = new ArrayList<>();

		if (styleClass != null) {
			classes.addAll(Arrays.asList(styleClass.split(",")));
		}

		return classes;
	}

}
