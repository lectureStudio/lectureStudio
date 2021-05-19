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

package org.lecturestudio.presenter.api.config;

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.web.api.filter.IpFilter;

public class NetworkConfiguration {

	private final StringProperty adapter = new StringProperty();

	private final ObjectProperty<BroadcastProfile> bcastProfile = new ObjectProperty<>();

	/** The list of available broadcast profiles. */
	private final ObservableList<BroadcastProfile> bcastProfiles = new ObservableArrayList<>();

	private final ObjectProperty<IpFilter> ipFilter = new ObjectProperty<>();


	/**
	 * @return the adapter
	 */
	public String getAdapter() {
		return adapter.get();
	}

	/**
	 * @param adapter the adapter to set
	 */
	public void setAdapter(String adapter) {
		this.adapter.set(adapter);
	}
	
	public StringProperty adapterProperty() {
		return adapter;
	}

	/**
	 * @return the adapter
	 */
	public BroadcastProfile getBroadcastProfile() {
		return bcastProfile.get();
	}

	/**
	 * @param adapter the adapter to set
	 */
	public void setBroadcastProfile(BroadcastProfile adapter) {
		this.bcastProfile.set(adapter);
	}

	public ObjectProperty<BroadcastProfile> broadcastProfileProperty() {
		return bcastProfile;
	}

	/**
	 * Get the list of available broadcast profiles.
	 *
	 * @return All broadcast profiles.
	 *
	 * @see BroadcastProfile
	 */
	public ObservableList<BroadcastProfile> getBroadcastProfiles() {
		return bcastProfiles;
	}

	/**
	 * @return the ipFilter
	 */
	public IpFilter getIpFilter() {
		return ipFilter.get();
	}

	/**
	 * @param ipFilter the ipFilter to set
	 */
	public void setIpFilter(IpFilter ipFilter) {
		this.ipFilter.set(ipFilter);
	}
	
	public ObjectProperty<IpFilter> ipFilterProperty() {
		return ipFilter;
	}
	
}
