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

package org.lecturestudio.core.pdf.mupdf;

import static java.util.Objects.nonNull;

import com.artifex.mupdf.fitz.*;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.geometry.Rectangle2D;

public class WordWalker implements StructuredTextWalker {

	private final Rect pageBounds;

	private final List<Rectangle2D> boundsList;

	private final float scale;

	private Rect wordBounds;

	private boolean inBounds = false;


	/**
	 * Create a new {@link WordWalker} with the specified page bounds.
	 *
	 * @param pageBounds The page bounds.
	 */
	public WordWalker(Rect pageBounds) {
		this.pageBounds = pageBounds;
		this.scale = 1.F / (pageBounds.x1 - pageBounds.x0);
		this.boundsList = new ArrayList<>();
	}

	@Override
	public void onImageBlock(Rect bbox, Matrix transform, Image image) {
	}

	@Override
	public void beginTextBlock(Rect bbox) {
		inBounds = pageBounds.contains(bbox);
	}

	@Override
	public void endTextBlock() {
	}

	@Override
	public void beginLine(Rect bbox, int wMode) {
		if (inBounds) {
			wordBounds = new Rect();
		}
	}

	@Override
	public void endLine() {
		if (inBounds && nonNull(wordBounds)) {
			saveWord(wordBounds);
		}
	}

	@Override
	public void onChar(int c, Point origin, Font font, float size, Quad quad) {
		if (inBounds) {
			if (Character.isWhitespace(c)) {
				saveWord(wordBounds);
				saveWord(quad.toRect());

				wordBounds = new Rect();
			}
			else {
				wordBounds.union(quad.toRect());
			}
		}
	}

	/**
	 * Get a list of all word bounds.
	 *
	 * @return A list of all word bounds.
	 */
	public java.util.List<Rectangle2D> getWordBounds() {
		return boundsList;
	}

	private void saveWord(Rect wordBounds) {
		double x = scale * wordBounds.x0;
		double y = scale * wordBounds.y0;
		double w = scale * (wordBounds.x1 - wordBounds.x0);
		double h = scale * (wordBounds.y1 - wordBounds.y0);

		boundsList.add(new Rectangle2D(x, y, w, h));
	}

}
