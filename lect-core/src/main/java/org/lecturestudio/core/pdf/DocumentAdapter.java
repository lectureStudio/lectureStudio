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
import org.lecturestudio.core.model.NotesPosition;
import org.lecturestudio.core.model.shape.Shape;

public interface DocumentAdapter {

	/**
	 * Closes the document.
	 */
	void close() throws IOException;

	/**
	 * Get the document outline.
	 *
	 * @return The document outline.
	 */
	DocumentOutline getDocumentOutline();

	/**
	 * Get the document renderer.
	 *
	 * @return The document renderer.
	 */
	DocumentRenderer getDocumentRenderer();

	/**
	 * Create a {@link Graphics2D} object with the specified parameters.
	 *
	 * @param pageIndex The index of the PDF page to which to draw.
	 * @param name The PDF graphics stream name.
	 * @param appendContent {@code true} if content should be appended to the existing one, {@code false} to overwrite.
	 *
	 * @return The newly created {@link Graphics2D} object.
	 */
	Graphics2D createGraphics(int pageIndex, String name, boolean appendContent);

	/**
	 * Set the title of the document.
	 *
	 * @param title The title.
	 */
	void setTitle(String title);

	/**
	 * Get the title of the document.
	 *
	 * @return The title.
	 */
	String getTitle();

	/**
	 * Set the author of the document.
	 *
	 * @param author The author.
	 */
	void setAuthor(String author);

	/**
	 * Get the author of the document.
	 *
	 * @return The author.
	 */
	String getAuthor();

	/**
	 * Get the bounds of the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 * @param position The position of notes on a page.
	 *
	 * @return The bounds of the page that has the specified page number.
	 */
	Rectangle2D getPageBounds(int pageNumber, NotesPosition position);

	/**
	 * Get the number of pages in the document.
	 *
	 * @return The number of pages in the document.
	 */
	int getPageCount();

	/**
	 * Get the text of the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The text of the page that has the specified page number.
	 */
	String getPageText(int pageNumber) throws IOException;

	/**
	 * Get the word bounds of the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The word bounds of the page that has the specified page number.
	 */
	List<Rectangle2D> getPageWordsNormalized(int pageNumber) throws IOException;

	/**
	 * Get the text bounds of the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The text bounds of the page that has the specified page number.
	 */
	Rectangle2D getPageTextBounds(int pageNumber) throws IOException;

	Set<URI> getLinks(int pageNumber);

	Set<File> getLaunchActions(int pageNumber);

	/**
	 * Add a page with the specified width and height to the end of document.
	 *
	 * @param width The width of the page.
	 * @param height The height of the page.
	 */
	void addPage(int width, int height);

	/**
	 * Delete the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 */
	void deletePage(int pageNumber);

	int importPage(DocumentAdapter srcDocument, int pageNumber) throws IOException;

	/**
	 * Save the document to the specified {@link OutputStream}.
	 *
	 * @param stream The {@link OutputStream} to write to
	 *
	 * @throws IOException if the output could not be written
	 */
	void toOutputStream(OutputStream stream) throws IOException;

	void setEditableAnnotations(int pageNumber, List<Shape> shapes) throws IOException;

	List<Shape> getEditableAnnotations(int pageNumber) throws IOException;

	/**
	 * Parse page contents and remove editable annotations for page rendering.
	 *
	 * @return The removed annotations from all pages.
	 *
	 * @throws IOException If the cleaned document cannot be created.
	 */
	Map<Integer, List<Shape>> removeEditableAnnotations() throws IOException;

}
