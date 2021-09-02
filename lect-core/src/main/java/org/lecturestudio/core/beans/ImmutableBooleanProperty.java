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

package org.lecturestudio.core.beans;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Immutable boolean property implementation.
 *
 * @author Alex Andres
 */
public class ImmutableBooleanProperty extends ImmutableObjectProperty<Boolean> {

	/** The immutable property. */
	private ImmutableProperty immutableProperty;


	/**
	 * Create an {@link ImmutableBooleanProperty} with the initial value set to {@code false}.
	 */
	public ImmutableBooleanProperty() {
		this(false);
	}

	/**
	 * Create an {@link ImmutableBooleanProperty} with the specified initial value.
	 *
	 * @param defaultValue The initial value.
	 */
	public ImmutableBooleanProperty(boolean defaultValue) {
		super(defaultValue);
	}

	/**
	 * Get the immutable {@link BooleanProperty}.
	 *
	 * @return the immutable {@link BooleanProperty}.
	 */
	public BooleanProperty getImmutableProperty() {
		if (isNull(immutableProperty)) {
			immutableProperty = new ImmutableProperty();
		}
		return immutableProperty;
	}

	@Override
	protected void fireChange(Observable<Boolean> observable, Boolean oldValue, Boolean newValue) {
		super.fireChange(observable, oldValue, newValue);

		if (nonNull(immutableProperty)) {
			immutableProperty.fireChange(immutableProperty, oldValue, newValue);
		}
	}



	private class ImmutableProperty extends BooleanProperty {

		@Override
		public Boolean get() {
			return ImmutableBooleanProperty.this.get();
		}

		@Override
		public void set(Boolean value) {
			// No-op. Immutable.
		}
	}

}
