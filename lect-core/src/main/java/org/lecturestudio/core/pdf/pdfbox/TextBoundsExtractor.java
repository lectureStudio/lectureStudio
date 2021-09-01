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

import org.lecturestudio.core.geometry.Rectangle2D;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class TextBoundsExtractor extends PDFTextStripper {

	/** The {@link PDDocument} on which the {@link TextBoundsExtractor} is working. */
	private final PDDocument document;

	/** The text bounds of the {@link TextBoundsExtractor}. */
	private final Rectangle2D textBounds;


	/**
	 * Create a new {@link TextBoundsExtractor} with the specified {@link PDDocument}.
	 *
	 * @param document The {@link PDDocument}.
	 *
	 * @throws IOException If there is an error loading the properties.
	 */
	public TextBoundsExtractor(PDDocument document) throws IOException {
		this.document = document;
		this.textBounds = new Rectangle2D();
	}

	/**
	 * Get the text bounds of the page in {@link #document} that has the specified page number.
	 *
	 * @param pageNumber The page number from which to get the text bounds.
	 *
	 * @return The text bounds of the page in {@link #document} that has the specified page number.
	 */
	public Rectangle2D getTextBounds(int pageNumber) throws IOException {
		textBounds.setRect(0, 0, 0, 0);

		setStartPage(pageNumber);
		setEndPage(pageNumber);
		getText(document);
		
		return textBounds;
	}

	@Override
	protected void processTextPosition(TextPosition text) {
		textBounds.union(new Rectangle2D(text.getX(), text.getY(), text.getWidth(), text.getHeight()));
	}
}
