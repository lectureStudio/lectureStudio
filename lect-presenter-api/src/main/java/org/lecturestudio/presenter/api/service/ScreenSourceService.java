/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.web.api.model.ScreenSource;

@Singleton
public class ScreenSourceService {

	private final Map<Document, ScreenSource> docScreenSourceMap;


	public ScreenSourceService() {
		docScreenSourceMap = new HashMap<>();
	}

	public void addScreenSource(Document doc, ScreenSource source) {
		docScreenSourceMap.put(doc, source);
	}

	public void removeScreenSource(Document doc) {
		docScreenSourceMap.remove(doc);
	}

	public ScreenSource getScreenSource(Document doc) {
		return docScreenSourceMap.get(doc);
	}

	public void clear() {
		docScreenSourceMap.clear();
	}
}
