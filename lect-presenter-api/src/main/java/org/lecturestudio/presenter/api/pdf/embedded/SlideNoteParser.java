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

package org.lecturestudio.presenter.api.pdf.embedded;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.model.SlideNote;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlideNoteParser {

	private static final Logger LOG = LogManager.getLogger(SlideNoteParser.class);

	private static final char NEWLINE = '\n';

	private static final char SPACE = ' ';


	private enum Symbol {

		LNOTE("{##"),
		RNOTE("##}");


		private final String symbol;


		Symbol(String symbol) {
			this.symbol = symbol;
		}

		public String toString() {
			return symbol;
		}
	}


	private enum Operation {
		Text,
		SlideNoteBegin,
		SlideNoteEnd
	}


	private final List<SlideNote> elements;

	private String input;

	private int pos;

	private StringBuilder textBuilder;


	public SlideNoteParser() {
		this.elements = new ArrayList<>();
	}
	
	public List<SlideNote> getSlideNotes() {
		return elements;
	}
	
	public void parse(String input) {
		if (input == null) {
			throw new NullPointerException("No input to parse.");
		}
		
		elements.clear();
		
		this.input = input;
		
		for (pos = 0; pos < input.length(); ++pos) {
			Symbol symbol = getSymbol(pos);
			Operation operation = getOperation(symbol);
			
			switch (operation) {
				case Text:
					text();
					break;
					
				case SlideNoteBegin:
					beginSlideNote();
					break;
					
				case SlideNoteEnd:
					endSlideNote();
					break;
			}
		}
	}
	
	private void beginSlideNote() {
		SlideNote note = new SlideNote();
		textBuilder = new StringBuilder();
		
		elements.add(note);
	}
	
	private void endSlideNote() {
		if (elements.isEmpty()) {
			LOG.error("End slide note failed. No slide note created.");
			return;
		}
		
		// Set text of the recently created slide note.
		SlideNote note = elements.get(elements.size() - 1);
		
		if (note != null && textBuilder != null) {
			note.setText(textBuilder.toString().trim());
		}
	}
	
	private void text() {
		if (textBuilder != null) {
			char ch = input.charAt(pos);

			if (ch == NEWLINE) {
				ch = SPACE;
			}

			textBuilder.append(ch);
		}
	}
	
	private Symbol getSymbol(int pos) {
		if (eof(pos)) {
			return null;
		}
		
		for (Symbol sym : Symbol.values()) {
			String symStr = sym.toString();
			
			// Skip if symbol reaches the end of input.
			if (eof(pos + symStr.length())) {
				continue;
			}
			
			String subStr = input.substring(pos, pos + symStr.length());
			
			if (symStr.equals(subStr)) {
				setPosition(pos + symStr.length() - 1);
				return sym;
			}
		}
		return null;
	}
	
	private Operation getOperation(Symbol symbol) {
		if (symbol == null) {
			return Operation.Text;
		}

		switch (symbol) {
			case LNOTE:
				return Operation.SlideNoteBegin;
			case RNOTE:
				return Operation.SlideNoteEnd;
			default:
				return Operation.Text;
		}
	}
	
	private boolean eof(int pos) {
		return pos > input.length();
	}
	
	private void setPosition(int pos) {
		this.pos = pos;
	}
    
}
