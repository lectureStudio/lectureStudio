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

package org.lecturestudio.swing.components;

import static java.util.Objects.nonNull;

import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;

public abstract class PageObject<T extends Shape> extends JComponent implements PageObjectView<T> {

	private T shape;

	private AffineTransform pageTransform;

	private boolean copying;

	private boolean focus;

	/**
	 * The action that is called when the page object is requested to be
	 * closed.
	 */
	private Action closeAction;

	/** The action that is called when the page object is about to be copied. */
	private Action copyAction;

	/** The action that is called when the page object has gained focus. */
	private ConsumerAction<Boolean> focusAction;


	abstract protected void updateToShapeContent(T shape);

	abstract protected void updateToTransform(AffineTransform transform);


	public PageObject() {
		super();
	}

	public AffineTransform getPageTransform() {
		return pageTransform;
	}

	public void setPageTransform(AffineTransform transform) {
		if (pageTransform != transform) {
			pageTransform = transform;

			updateToTransform(transform);
		}
	}

	@Override
	public T getPageShape() {
		return shape;
	}

	@Override
	public void setPageShape(T shape) {
		if (this.shape != shape) {
			this.shape = shape;

			updateToShapeContent(shape);
		}
	}

	@Override
	public boolean isCopying() {
		return copying;
	}

	@Override
	public boolean getFocus() {
		return focus;
	}

	@Override
	public void setFocus(boolean focus) {
		this.focus = focus;

		if (nonNull(focusAction)) {
			focusAction.execute(focus);
		}
	}

	@Override
	public void setOnClose(Action action) {
		closeAction = action;
	}

	@Override
	public void setOnCopy(Action action) {
		copyAction = action;
	}

	@Override
	public void setOnFocus(ConsumerAction<Boolean> action) {
		focusAction = action;
	}

	protected Action getOnClose() {
		return closeAction;
	}

	protected Action getOnCopy() {
		return copyAction;
	}
}
