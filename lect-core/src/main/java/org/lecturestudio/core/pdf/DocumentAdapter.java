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

package org.lecturestudio.core.pdf;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.shape.Shape;

public interface DocumentAdapter {

	void close() throws IOException;

	DocumentOutline getDocumentOutline();

	DocumentRenderer getDocumentRenderer();

	Graphics2D createGraphics(int pageIndex, String name, boolean appendContent);

	void setTitle(String title);

	String getTitle();

	void setAuthor(String author);

	String getAuthor();

	Rectangle2D getPageBounds(int pageNumber);

	int getPageCount();

	String getPageText(int pageNumber) throws IOException;

	List<Rectangle2D> getPageWordsNormalized(int pageNumber) throws IOException;

	Rectangle2D getPageTextBounds(int pageNumber) throws IOException;

	Set<URI> getLinks(int pageNumber);

	Set<File> getLaunchActions(int pageNumber);

	void addPage(int width, int height);

	void deletePage(int pageNumber);

	int importPage(DocumentAdapter srcDocument, int pageNumber) throws IOException;

	void toOutputStream(OutputStream stream) throws IOException;

	void setEditableAnnotations(int pageNumber, List<Shape> shapes) throws IOException;

	List<Shape> getEditableAnnotations(int pageNumber) throws IOException;

	Map<Integer, List<Shape>> removeEditableAnnotations() throws IOException;

}
