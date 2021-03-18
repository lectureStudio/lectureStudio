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

import java.util.Objects;

public class IpRangeRule implements FilterRule<String> {

	private String from;

	private String to;


	public IpRangeRule() {
		this("", "");
	}

	public IpRangeRule(String from, String to) {
		setFrom(from);
		setTo(to);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String fromIP) {
		this.from = fromIP;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String toIP) {
		this.to = toIP;
	}

	@Override
	public boolean isAllowed(String ip) {
		long toCheck = convertStringToInt(ip);
		long from = convertStringToInt(this.from);
		long to = convertStringToInt(this.to);

		return (toCheck >= from && toCheck <= to);
	}

	@Override
	public FilterRule<String> clone() {
		return new IpRangeRule(from, to);
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		IpRangeRule other = (IpRangeRule) obj;

		boolean equal = from == other.from || (from != null && from.equals(other.from));

		if (!equal) {
			return false;
		}

		equal = to == other.to || (to != null && to.equals(other.to));

		return equal;
	}

	@Override
	public String toString() {
		return from + " - " + to;
	}

	private long convertStringToInt(String ip) {
		String[] ipStr = ip.split("\\.");

		int octet1 = Integer.parseInt(ipStr[0]);
		int octet2 = Integer.parseInt(ipStr[1]);
		int octet3 = Integer.parseInt(ipStr[2]);
		int octet4 = Integer.parseInt(ipStr[3]);

		return ((octet1 << 24) | (octet2 << 16) | (octet3 << 8) | octet4) & 0xFFFFFFFFL;
	}

}
