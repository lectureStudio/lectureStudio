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

import static java.util.Objects.nonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class RegexRule implements FilterRule<String> {

	@Id
	@SequenceGenerator(name = "RegexRuleGen", sequenceName = "regex_rule_seq", allocationSize = 1)
	@GeneratedValue(generator = "RegexRuleGen")
	private long id;

	private transient Pattern pattern;

	private String regex;


	public RegexRule() {
		this("");
	}

	public RegexRule(String regex) {
		setRegex(regex);
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(nonNull(regex) ? regex : "");
	}

	@Override
	public boolean isAllowed(String value) {
		Matcher matcher = pattern.matcher(value);
		return !matcher.matches();
	}

	@Override
	public FilterRule<String> clone() {
		return new RegexRule(regex);
	}

	@Override
	public int hashCode() {
		return 37 * regex.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		RegexRule other = (RegexRule) obj;

		return regex != null && regex.equals(other.regex);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + regex;
	}

}
