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

public class QuizAnswer extends ServiceModel implements Comparable<QuizAnswer>, Cloneable, Serializable {

	private String[] options;


	public QuizAnswer() {
		this(null);
	}

	public QuizAnswer(String[] options) {
		this.options = options;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

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
			a.append(opt);
		}
		for (String opt : answer.getOptions()) {
			b.append(opt);
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

		return getServiceId().equals(other.getServiceId()) && Arrays
				.equals(options, other.options);
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
