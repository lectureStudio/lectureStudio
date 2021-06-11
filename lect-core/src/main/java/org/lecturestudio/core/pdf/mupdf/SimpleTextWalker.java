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

public class SimpleTextWalker implements StructuredTextWalker {

	private final Rect pageBounds;

	private final StringBuffer buffer;

	private Rect lastLineBBox;

	private boolean inBounds = false;


	/**
	 * Create a new {@link SimpleTextWalker} with the specified page bounds.
	 *
	 * @param pageBounds The page bounds.
	 */
	public SimpleTextWalker(Rect pageBounds) {
		this.pageBounds = pageBounds;
		this.buffer = new StringBuffer();
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
		if (inBounds && nonNull(lastLineBBox)) {
			if (lastLineBBox.y0 == bbox.y0) {
				buffer.append(" ");
			}
			else {
				buffer.append("\n");
			}
		}

		lastLineBBox = bbox;
	}

	@Override
	public void endLine() {
	}

	@Override
	public void onChar(int c, Point origin, Font font, float size, Quad quad) {
		if (inBounds) {
			buffer.appendCodePoint(c);
		}
	}

	/**
	 * Get the text.
	 *
	 * @return The content of {@link #buffer} as a String.
	 */
	public String getText() {
		return buffer.toString();
	}

}
