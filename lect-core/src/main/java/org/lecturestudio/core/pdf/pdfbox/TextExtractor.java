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

package org.lecturestudio.core.pdf.pdfbox;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class TextExtractor extends PDFTextStripper {

	/** The {@link PDDocument} on which the {@link TextExtractor} is working. */
	private final PDDocument document;


	/**
	 * Create a new {@link TextExtractor} with the specified {@link PDDocument}.
	 *
	 * @param document The {@link PDDocument}.
	 *
	 * @throws IOException If there is an error loading the properties.
	 */
	public TextExtractor(PDDocument document) throws IOException {
		this.document = document;

		setSortByPosition(true);
		setShouldSeparateByBeads(true);
	}

	/**
	 * Get the text of the page in {@link #document} that has the specified page number.
	 *
	 * @param pageNumber The page number from which to get the text.
	 *
	 * @return The text of the page in {@link #document} that has the specified page number.
	 */
	public String getText(int pageNumber) throws IOException {
		setStartPage(pageNumber);
		setEndPage(pageNumber);

		return getText(document);
	}

}
