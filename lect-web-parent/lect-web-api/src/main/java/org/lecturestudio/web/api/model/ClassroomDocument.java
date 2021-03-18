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
import java.util.Objects;

public class ClassroomDocument implements Serializable {

	private long id;

	private String fileName;
	
	private String checksum;

	
	public ClassroomDocument() {
		this(null);
	}
	
	public ClassroomDocument(String fileName) {
		setFileName(fileName);
	}
	
	/**
	 * @param fileName the documentFile to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * @param checksum the checksum to set
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ClassroomDocument that = (ClassroomDocument) o;

		return Objects.equals(fileName, that.fileName) && Objects.equals(checksum, that.checksum);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileName, checksum);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName());
		buffer.append(": ");
		buffer.append(getFileName());
		buffer.append(", ");
		buffer.append(getChecksum());
		
		return buffer.toString();
	}
}
