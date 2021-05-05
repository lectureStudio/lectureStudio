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

package org.lecturestudio.presenter.javafx.view.model;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.lecturestudio.web.api.filter.IpRangeRule;

public class IpRuleTableItem {

	private final IpRangeRule rule;

	private final StringProperty fromIP;

	private final StringProperty toIP;


	public IpRuleTableItem() {
		this(new IpRangeRule());
	}

	public IpRuleTableItem(IpRangeRule rule) {
		this.fromIP = new SimpleStringProperty(rule.getFromIp());
		this.fromIP.addListener(observable -> {
			rule.setFromIp(getFromIP());
		});
		this.toIP = new SimpleStringProperty(rule.getToIp());
		this.toIP.addListener(observable -> {
			rule.setToIp(getToIP());
		});
		this.rule = rule;
	}

	public IpRangeRule getIpRangeRule() {
		return rule;
	}

	public String getFromIP() {
		return fromIPProperty().get();
	}

	public void setFromIP(String IP) {
		fromIPProperty().set(IP);
	}

	public StringProperty fromIPProperty() {
		return fromIP;
	}

	public String getToIP() {
		return toIPProperty().get();
	}

	public void setToIP(String IP) {
		toIPProperty().set(IP);
	}

	public StringProperty toIPProperty() {
		return toIP;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fromIP, toIP);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		IpRuleTableItem other = (IpRuleTableItem) obj;

		return Objects.equals(fromIP, other.fromIP) && Objects.equals(toIP, other.toIP);
	}

}
