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

package org.lecturestudio.core.service;

import java.io.IOException;

import org.lecturestudio.core.model.Contributor;

public class ContributorsYamlSource extends YamlSource<Contributor> {

	/**
	 * Create a new {@link ContributorsYamlSource} instance.
	 *
	 * @param resourcePath The path to the contributors resource file.
	 */
	public ContributorsYamlSource(String resourcePath) throws IOException {
		super(resourcePath);
	}
}
