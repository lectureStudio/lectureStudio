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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class IpRangeRule implements FilterRule<String> {

	@Id
	@SequenceGenerator(name = "ipRangeRuleSeq", sequenceName = "iprangerule_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "ipRangeRuleSeq")
	private Long id;

	private String fromIp;

	private String toIp;


	public IpRangeRule() {
		this("", "");
	}

	public IpRangeRule(String fromIp, String toIp) {
		setFromIp(fromIp);
		setToIp(toIp);
	}

	public Long getId() {
		return id;
	}

	public String getFromIp() {
		return fromIp;
	}

	public void setFromIp(String fromIp) {
		this.fromIp = fromIp;
	}

	public String getToIp() {
		return toIp;
	}

	public void setToIp(String toIp) {
		this.toIp = toIp;
	}

	@Override
	public boolean isAllowed(String ip) {
		long toCheck = convertStringToInt(ip);
		long from = convertStringToInt(this.fromIp);
		long to = convertStringToInt(this.toIp);

		return (toCheck >= from && toCheck <= to);
	}

	@Override
	public FilterRule<String> clone() {
		return new IpRangeRule(fromIp, toIp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fromIp, toIp);
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

		boolean equal = fromIp == other.fromIp || (fromIp != null && fromIp
				.equals(other.fromIp));

		if (!equal) {
			return false;
		}

		equal = toIp == other.toIp || (toIp != null && toIp.equals(other.toIp));

		return equal;
	}

	@Override
	public String toString() {
		return fromIp + " - " + toIp;
	}

	private static long convertStringToInt(String ip) {
		String[] ipStr = ip.split("\\.");

		int octet1 = Integer.parseInt(ipStr[0]);
		int octet2 = Integer.parseInt(ipStr[1]);
		int octet3 = Integer.parseInt(ipStr[2]);
		int octet4 = Integer.parseInt(ipStr[3]);

		return ((octet1 << 24) | (octet2 << 16) | (octet3 << 8) | octet4) & 0xFFFFFFFFL;
	}

}
