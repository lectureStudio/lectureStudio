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
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.geometry.Rectangle2D;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class WordBoundsExtractor extends PDFTextStripper {

	/** The {@link PDDocument} on which the {@link WordBoundsExtractor} is working. */
	private final PDDocument document;

	private List<Rectangle2D> boundsList;

	private double scale;


	/**
	 * Create a new {@link WordBoundsExtractor} with the specified {@link PDDocument}.
	 *
	 * @param document The {@link PDDocument}..
	 *
	 * @throws IOException If there is an error loading the properties.
	 */
	public WordBoundsExtractor(PDDocument document) throws IOException {
		this.document = document;
	}

	/**
	 * Get the word bounds of the page in {@link #document} that has the specified page number.
	 *
	 * @param pageNumber The page number from which to get the word bounds.
	 *
	 * @return The word bounds of the page in {@link #document} that has the specified page number.
	 */
	public java.util.List<Rectangle2D> getWordBounds(int pageNumber) throws IOException {
		PDPage page = document.getPage(pageNumber);
		PDRectangle rect = page.getMediaBox();

		boundsList = new ArrayList<>();
		scale = 1.D / rect.getWidth();

		setStartPage(pageNumber);
		setEndPage(pageNumber);
		getText(document);

		return boundsList;
	}

	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		String wordSeparator = getWordSeparator();
		List<TextPosition> word = new ArrayList<>();

		for (TextPosition text : textPositions) {
			String code = text.getUnicode();

			if (code != null && code.length() >= 1) {
				if (!code.equals(wordSeparator)) {
					word.add(text);
				}
				else if (!word.isEmpty()) {
					saveWord(word);
					word.clear();
				}
			}
		}

		if (!word.isEmpty()) {
			saveWord(word);
			word.clear();
		}
	}

	private void saveWord(List<TextPosition> word) {
		Rectangle2D boundingBox = null;

		for (TextPosition text : word) {
			Rectangle2D box = new Rectangle2D(text.getXDirAdj(), text.getYDirAdj() - text.getHeight(), text.getWidthDirAdj(), text.getHeight());

			if (boundingBox == null) {
				boundingBox = box;
			}
			else {
				boundingBox.union(box);
			}
		}

		if (boundingBox != null) {
			double x = scale * boundingBox.getX();
			double y = scale * boundingBox.getY();
			double w = scale * boundingBox.getWidth();
			double h = scale * boundingBox.getHeight();

			boundsList.add(new Rectangle2D(x, y, w, h));
		}
	}
}
