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

package org.lecturestudio.web.api.model.quiz;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import org.lecturestudio.web.api.model.ServiceModel;

/**
 * Represents an answer to a quiz question, consisting of one or more options.
 *
 * @author Alex Andres
 */
public class QuizAnswer extends ServiceModel implements Comparable<QuizAnswer>, Cloneable, Serializable {

	/** Array of option strings representing the answer choices. */
	private String[] options;


	/**
	 * Constructs an empty QuizAnswer with no options.
	 */
	public QuizAnswer() {
		this(null);
	}

	/**
	 * Constructs a QuizAnswer with the specified options.
	 *
	 * @param options Array of strings representing the answer options.
	 */
	public QuizAnswer(String[] options) {
		this.options = options;
	}

	/**
	 * Returns the array of option strings.
	 *
	 * @return The options array.
	 */
	public String[] getOptions() {
		return options;
	}

	/**
	 * Sets the options array.
	 *
	 * @param options Array of strings representing the answer options.
	 */
	public void setOptions(String[] options) {
		this.options = options;
	}

	/**
	 * Sets the value of a specific option at the given index.
	 *
	 * @param index The index of the option to set.
	 * @param value The new value for the option.
	 *
	 * @throws IndexOutOfBoundsException If the index is out of range.
	 */
	public void setValue(int index, String value) {
		if (index < 0 || index >= options.length) {
			throw new IndexOutOfBoundsException("Cannot set value for index " + index);
		}

		options[index] = value;
	}

	@Override
	public int compareTo(QuizAnswer answer) {
		StringBuilder a = new StringBuilder();
		StringBuilder b = new StringBuilder();

		for (String opt : getOptions()) {
			a.append(opt).append("-");
		}
		for (String opt : answer.getOptions()) {
			b.append(opt).append("-");
		}

		return a.toString().compareToIgnoreCase(b.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		QuizAnswer other = (QuizAnswer) o;

		if (!Objects.equals(getServiceId(), other.getServiceId())) {
			return false;
		}

		return Arrays.equals(options, other.options);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getServiceId(), Arrays.hashCode(options));
	}

	@Override
	public QuizAnswer clone() {
		return new QuizAnswer(options);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + Arrays.toString(getOptions());
	}

}
