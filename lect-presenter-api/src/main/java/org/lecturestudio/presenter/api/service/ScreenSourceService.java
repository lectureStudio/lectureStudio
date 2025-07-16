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
import org.lecturestudio.presenter.api.model.ScreenShareContext;

/**
 * Service that manages screen sharing contexts for documents. This service maintains
 * a mapping between Document objects and their corresponding ScreenShareContext objects.
 *
 * @author Alex Andres
 */
@Singleton
public class ScreenSourceService {

	/** Maps Document objects to their corresponding ScreenShareContext objects. */
	private final Map<Document, ScreenShareContext> docScreenSourceMap;


	/**
	 * Constructs a new ScreenSourceService with an empty document-to-screen-share-context mapping.
	 */
	public ScreenSourceService() {
		docScreenSourceMap = new HashMap<>();
	}

	/**
	 * Associates a screen share context with a document.
	 *
	 * @param doc     The document to associate the context with.
	 * @param context The screen-share context to associate with the document.
	 */
	public void addScreenShareContext(Document doc, ScreenShareContext context) {
		docScreenSourceMap.put(doc, context);
	}

	/**
	 * Removes the screen-share context associated with the specified document.
	 *
	 * @param doc The document whose screen-share context should be removed.
	 */
	public void removeScreenSource(Document doc) {
		docScreenSourceMap.remove(doc);
	}

	/**
	 * Retrieves the screen-share context associated with the specified document.
	 *
	 * @param doc The document whose screen-share context should be retrieved.
	 *
	 * @return The screen-share context associated with the document, or null if none exists.
	 */
	public ScreenShareContext getScreenShareContext(Document doc) {
		return docScreenSourceMap.get(doc);
	}

	/**
	 * Removes all document-to-screen-share-context mappings.
	 */
	public void clear() {
		docScreenSourceMap.clear();
	}
}
