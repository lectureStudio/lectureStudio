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

package org.lecturestudio.javafx.layout;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;

/**
 * Defines optional width constraints for a column in a {@link
 * javafx.scene.control.TableView}.
 *
 * @author Alex Andres
 */
public class ColumnSizeConstraints {

	/**
	 * The preferred width for the column. This property is ignored if percentWidth
	 * is set.
	 * <p>
	 * The default value is USE_COMPUTED_SIZE, which means the preferred width will
	 * be computed to be the largest preferred width of the column's content.
	 */
	private DoubleProperty prefWidth;

	/**
	 * The width percentage of the column. If set to a value greater than 0, the
	 * column will be resized to this percentage of the TableView's available width
	 * and the other size constraints will be ignored.
	 * <p>
	 * The default value is -1, which means the percentage will be ignored.
	 */
	private DoubleProperty percentWidth;


	public final void setPrefWidth(double value) {
		prefWidthProperty().set(value);
	}

	public final double getPrefWidth() {
		return prefWidth == null ? USE_COMPUTED_SIZE : prefWidth.get();
	}

	public final DoubleProperty prefWidthProperty() {
		if (prefWidth == null) {
			prefWidth = new DoublePropertyBase(USE_COMPUTED_SIZE) {

				@Override
				public Object getBean() {
					return ColumnSizeConstraints.this;
				}

				@Override
				public String getName() {
					return "prefWidth";
				}
			};
		}
		return prefWidth;
	}

	public final void setPercentWidth(double value) {
		percentWidthProperty().set(value);
	}

	public final double getPercentWidth() {
		return percentWidth == null ? -1 : percentWidth.get();
	}

	public final DoubleProperty percentWidthProperty() {
		if (percentWidth == null) {
			percentWidth = new DoublePropertyBase(-1) {

				@Override
				public Object getBean() {
					return ColumnSizeConstraints.this;
				}

				@Override
				public String getName() {
					return "percentWidth";
				}
			};
		}
		return percentWidth;
	}

}
