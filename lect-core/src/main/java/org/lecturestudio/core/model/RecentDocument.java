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

package org.lecturestudio.core.model;

import java.util.Date;

public class RecentDocument {

	private String name;

	private String path;

	private Date lastModified;


	public RecentDocument() {
		
	}
	
	public RecentDocument(String name, String path, Date opened) {
		setDocumentName(name);
		setDocumentPath(path);
		setLastModified(opened);
	}
	
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setDocumentName(String name) {
		this.name = name;
	}

	public void setDocumentPath(String path) {
		this.path = path;
	}

	public String getDocumentName() {
		return name;
	}

	public String getDocumentPath() {
		return path;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    
	    if (obj == null)
	        return false;
	    
	    if (getClass() != obj.getClass())
	        return false;
	    
	    final RecentDocument other = (RecentDocument) obj;
	    boolean name = getDocumentName().equals(other.getDocumentName());
	    boolean path = getDocumentPath().equals(other.getDocumentPath());
	    
	    return name && path;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() & path.hashCode();
	}

}
