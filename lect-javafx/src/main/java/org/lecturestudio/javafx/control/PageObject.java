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

package org.lecturestudio.javafx.control;

import static java.util.Objects.nonNull;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.transform.Transform;

import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;

public abstract class PageObject<T extends Shape> extends Control implements PageObjectView<T> {

	private static final String DEFAULT_STYLE_CLASS = "page-object";

	private final ObjectProperty<T> shape = new SimpleObjectProperty<>();

	private final ObjectProperty<Transform> pageTransform = new SimpleObjectProperty<>();

	private final BooleanProperty copying = new SimpleBooleanProperty();

	private final BooleanProperty focus = new SimpleBooleanProperty();

	/** The action that is called when the page object is requested to be closed. */
	private Action closeAction;

	/** The action that is called when the page object is about to be copied. */
	private Action copyAction;

	/** The action that is called when the page object has gained focus. */
	private ConsumerAction<Boolean> focusAction;


	public PageObject() {
		initialize();
	}

	public ObjectProperty<Transform> pageTransformProperty() {
		return pageTransform;
	}

	public Transform getPageTransform() {
		return pageTransform.get();
	}

	public void setPageTransform(Transform transform) {
		this.pageTransform.set(transform);
	}

	public ObjectProperty<T> pageShapeProperty() {
		return shape;
	}

	public BooleanProperty copyingProperty() {
		return copying;
	}

	public BooleanProperty focusProperty() {
		return focus;
	}

	@Override
	public T getPageShape() {
		return shape.get();
	}

	@Override
	public void setPageShape(T shape) {
		this.shape.set(shape);
	}

	@Override
	public boolean isCopying() {
		return copyingProperty().get();
	}

	@Override
	public boolean getFocus() {
		return focusProperty().get();
	}

	@Override
	public void setFocus(boolean focus) {
		focusProperty().set(focus);

		if (nonNull(focusAction)) {
			focusAction.execute(focus);
		}
	}

	@Override
	public void setOnClose(Action action) {
		this.closeAction = action;
	}

	@Override
	public void setOnCopy(Action action) {
		this.copyAction = action;
	}

	@Override
	public void setOnFocus(ConsumerAction<Boolean> action) {
		this.focusAction = action;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/page-object.css").toExternalForm();
	}

	Action getOnClose() {
		return closeAction;
	}

	Action getOnCopy() {
		return copyAction;
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}

}
