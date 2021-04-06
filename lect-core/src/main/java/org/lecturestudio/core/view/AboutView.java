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

package org.lecturestudio.core.view;

import java.util.List;
import java.util.Properties;

import org.lecturestudio.core.model.Contributor;
import org.lecturestudio.core.model.Sponsor;

public interface AboutView extends View {

	void setAppName(String name);

	void setAppVersion(String version);

	void setAppBuildDate(String date);

	void setWebsite(String website);

	void setIssueWebsite(String website);

	void setContributors(List<Contributor> contributors);

	void setSponsors(List<Sponsor> sponsors);

	void setProperties(Properties properties);

	void setOnClose(Action action);

}
