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

/**
 * Interface for PDF document operations providing access to document content,
 * rendering, metadata, and annotation capabilities.
 *
 * @author Alex Andres
 */
public interface DocumentAdapter {

	/**
	 * Closes the document and releases any system resources associated with it.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	void close() throws IOException;

	/**
	 * Gets the outline (bookmarks) of the document.
	 *
	 * @return the document outline structure.
	 */
	DocumentOutline getDocumentOutline();

	/**
	 * Gets the document renderer for this document.
	 *
	 * @return the document renderer.
	 */
	DocumentRenderer getDocumentRenderer();

	/**
	 * Creates a graphics context for drawing on a specific page.
	 *
	 * @param pageIndex     the zero-based index of the page.
	 * @param name          the name of the graphics context.
	 * @param appendContent whether to append to existing content.
	 *
	 * @return a Graphics2D object for drawing.
	 */
	Graphics2D createGraphics(int pageIndex, String name, boolean appendContent);

	/**
	 * Creates a graphics context for drawing on a specific page with the position of the notes.
	 *
	 * @param pageIndex     the zero-based index of the page.
	 * @param name          the name of the graphics context.
	 * @param appendContent whether to append to existing content.
	 * @param notesPosition the position of notes.
	 *
	 * @return a Graphics2D object for drawing.
	 */
	Graphics2D createGraphics(int pageIndex, String name, boolean appendContent, NotesPosition notesPosition);

	/**
	 * Sets the title of the document.
	 *
	 * @param title the title to set.
	 */
	void setTitle(String title);

	/**
	 * Gets the title of the document.
	 *
	 * @return the document title.
	 */
	String getTitle();

	/**
	 * Sets the author of the document.
	 *
	 * @param author the author to set.
	 */
	void setAuthor(String author);

	/**
	 * Gets the author of the document.
	 *
	 * @return the document author.
	 */
	String getAuthor();

	/**
	 * Gets the bounds of a page with specified notes position.
	 *
	 * @param pageNumber the page number (1-based).
	 * @param position   the position of notes.
	 *
	 * @return the rectangular bounds of the page.
	 */
	Rectangle2D getPageBounds(int pageNumber, NotesPosition position);

	/**
	 * Gets the total number of pages in the document.
	 *
	 * @return the page count.
	 */
	int getPageCount();

	/**
	 * Gets the text content of a page.
	 *
	 * @param pageNumber the page number (1-based).
	 *
	 * @return the text content of the page.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	String getPageText(int pageNumber) throws IOException;

	/**
	 * Gets a limited number of text lines from a page.
	 *
	 * @param pageNumber the page number (1-based).
	 * @param maxLines   the maximum number of lines to return.
	 *
	 * @return list of text lines.
	 */
	List<String> getPageTextLines(int pageNumber, int maxLines);

	/**
	 * Gets normalized word bounds for a page with specified notes position.
	 *
	 * @param pageNumber         the page number (1-based).
	 * @param splitNotesPosition the position of split notes.
	 *
	 * @return list of normalized word rectangles.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	List<Rectangle2D> getPageWordsNormalized(int pageNumber, NotesPosition splitNotesPosition) throws IOException;

	/**
	 * Gets the bounding rectangle of all text on a page.
	 *
	 * @param pageNumber the page number (1-based).
	 *
	 * @return the bounding rectangle of text.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	Rectangle2D getPageTextBounds(int pageNumber) throws IOException;

	/**
	 * Gets all hyperlinks on a page.
	 *
	 * @param pageNumber the page number (1-based).
	 *
	 * @return set of URIs representing links.
	 */
	Set<URI> getLinks(int pageNumber);

	/**
	 * Gets file launch actions on a page.
	 *
	 * @param pageNumber the page number (1-based).
	 *
	 * @return set of files to be launched.
	 */
	Set<File> getLaunchActions(int pageNumber);

	/**
	 * Adds a new blank page with specified dimensions.
	 *
	 * @param width  the width of the page.
	 * @param height the height of the page.
	 */
	void addPage(int width, int height);

	/**
	 * Deletes a page from the document.
	 *
	 * @param pageNumber the page number (1-based) to delete.
	 */
	void deletePage(int pageNumber);

	/**
	 * Imports a page from another document.
	 *
	 * @param srcDocument  the source document.
	 * @param srcPageIndex the page index in the source document.
	 * @param dstPageIndex the destination page index.
	 *
	 * @return the index of the imported page.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	int importPage(DocumentAdapter srcDocument, int srcPageIndex, int dstPageIndex) throws IOException;

	/**
	 * Writes the document to an output stream.
	 *
	 * @param stream the output stream.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	void toOutputStream(OutputStream stream) throws IOException;

	/**
	 * Sets editable annotations for a page.
	 *
	 * @param pageNumber the page number (1-based).
	 * @param shapes     the list of shapes to set as annotations.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	void setEditableAnnotations(int pageNumber, List<Shape> shapes) throws IOException;

	/**
	 * Gets editable annotations for a page.
	 *
	 * @param pageNumber the page number (1-based).
	 *
	 * @return list of shape annotations.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	List<Shape> getEditableAnnotations(int pageNumber) throws IOException;

	/**
	 * Removes all editable annotations from the document.
	 *
	 * @return map of page numbers to removed annotations.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	Map<Integer, List<Shape>> removeEditableAnnotations() throws IOException;

}
