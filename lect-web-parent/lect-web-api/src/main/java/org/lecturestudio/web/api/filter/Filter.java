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

package org.lecturestudio.web.api.filter;

import static java.util.Objects.isNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;

public abstract class Filter<T, R extends FilterRule<T>> implements Serializable {

	private final List<R> rules;


	public Filter() {
		rules = new ArrayList<>();
	}

	public Filter(Filter<T, R> filter) {
		rules = new ArrayList<>();
		rules.addAll(filter.getRules());
	}

	public void addListener(ListChangeListener<ObservableList<R>> listener) {
//		rules.addListener(listener);
	}

	public void removeListener(ListChangeListener<ObservableList<R>> listener) {
//		rules.removeListener(listener);
	}

	@SuppressWarnings("unchecked")
	public void registerRule(R rule) {
		rules.add((R) rule.clone());
	}

	public void registerRules(List<R> rules) {
		if (isNull(rules)) {
			return;
		}

		for (R rule : rules) {
			registerRule(rule);
		}
	}

	public void unregisterRule(R rule) {
		rules.remove(rule);
	}

	public void clear() {
		rules.clear();
	}

	public int size() {
		return rules.size();
	}

	public List<R> getRules() {
		return Collections.unmodifiableList(rules);
	}

	public void setRules(List<R> rules) {
		clear();
		registerRules(rules);
	}

	public boolean isAllowed(T input) {
		for (R rule : rules) {
			if (rule.isAllowed(input)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAllowedByAll(T input) {
		for (R rule : rules) {
			if (!rule.isAllowed(input)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(rules);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		Filter<?, ?> other = (Filter<?, ?>) obj;

		return Objects.equals(rules, other.rules);
	}

}
