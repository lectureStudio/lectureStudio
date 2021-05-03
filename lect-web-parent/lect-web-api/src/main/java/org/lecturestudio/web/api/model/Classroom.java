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

package org.lecturestudio.web.api.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import org.lecturestudio.web.api.filter.IpRangeRule;

@Entity
public class Classroom implements Serializable {

	/** The unique classroom identifier. */
	@Id
	@SequenceGenerator(name = "ClassroomGen", sequenceName = "classroom_id_seq", allocationSize = 1)
	@GeneratedValue(generator = "ClassroomGen")
	private long id;

	/** The name of the classroom. */
	private String name;
	
	/** The short name of the classroom which is used mainly for URLs. */
	private String shortName;

	/** The timestamp of when the classroom was created. */
	private long createdTime;

	private UUID uuid;

	@OneToMany(cascade = { CascadeType.ALL })
	private List<IpRangeRule> ipFilterRules;

	@OneToMany(cascade = { CascadeType.ALL })
	private Set<ClassroomService> services;

	/** The initial list of documents used in this classroom. */
	@OneToMany(cascade = { CascadeType.ALL })
	private Set<ClassroomDocument> documents;
	

	public Classroom() {
		this(null, null);
	}
	
	public Classroom(String name, String shortName) {
		setName(name);
		setShortName(shortName);
		setDocuments(new HashSet<>());
		setServices(new HashSet<>());
	}

	public String getName() {
		return name;
	}

	public void setName(String className) {
		this.name = className;
	}
	
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the timestamp
	 */
	public long getCreatedTimestamp() {
		return createdTime;
	}

	/**
	 * @param timestamp the timestamp
	 */
	public void setCreatedTimestamp(long timestamp) {
		this.createdTime = timestamp;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public List<IpRangeRule> getIpFilterRules() {
		return ipFilterRules;
	}

	public void setIpFilterRules(List<IpRangeRule> rules) {
		this.ipFilterRules = rules;
	}

	public Set<ClassroomService> getServices() {
		return services;
	}

	public void setServices(Set<ClassroomService> services) {
		this.services = services;
	}

	/**
	 * @return the documents
	 */
	public Set<ClassroomDocument> getDocuments() {
		return documents;
	}

	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(Set<ClassroomDocument> documents) {
		this.documents = documents;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName());
		buffer.append(": ");
		buffer.append(getName());
		buffer.append(", ");
		buffer.append(getShortName());
		buffer.append(", ");
		buffer.append("Created: ");
		buffer.append(getCreatedTimestamp());
		buffer.append(", ");
		buffer.append("IP Rules: ");
		buffer.append(getIpFilterRules());
		buffer.append(", ");
		buffer.append("Services: ");
		buffer.append(getServices());
		buffer.append(", ");
		buffer.append("Documents: ");
		buffer.append(getDocuments());

		return buffer.toString();
	}
}
