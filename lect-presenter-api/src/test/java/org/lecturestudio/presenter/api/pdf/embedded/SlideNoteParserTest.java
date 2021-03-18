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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.lecturestudio.core.model.SlideNote;

import org.junit.jupiter.api.Test;

class SlideNoteParserTest {

	@Test
	final void testSimpleNote() {
		String input = "{## Note text line 1 ##}";
		
		SlideNoteParser parser = new SlideNoteParser();
		parser.parse(input);
		
		List<SlideNote> notes = parser.getSlideNotes();
		
		assertNotNull(notes);
		assertEquals(1, notes.size());
		assertEquals("Note text line 1", notes.get(0).getText());
	}
	
	@Test
	final void testMultilineNote() {
		String input = "{##\n Note text line 1\nNote text line 2 \n##}";
		
		SlideNoteParser parser = new SlideNoteParser();
		parser.parse(input);
		
		List<SlideNote> notes = parser.getSlideNotes();
		
		assertNotNull(notes);
		assertEquals(1, notes.size());
		assertEquals("Note text line 1 Note text line 2", notes.get(0).getText());
	}

	@Test
	final void testMultipleNotes() {
		String input = "{## Note text line 1 ##} {## Note text line 2 ##}";
		
		SlideNoteParser parser = new SlideNoteParser();
		parser.parse(input);
		
		List<SlideNote> notes = parser.getSlideNotes();
		
		assertNotNull(notes);
		assertEquals(2, notes.size());
		assertEquals("Note text line 1", notes.get(0).getText());
		assertEquals("Note text line 2", notes.get(1).getText());
	}
	
	@Test
	final void testTextEmbeddedNote() {
		String input = "Text around {## Note text line 1 ##} embedded slide notes.";
		
		SlideNoteParser parser = new SlideNoteParser();
		parser.parse(input);
		
		List<SlideNote> notes = parser.getSlideNotes();
		
		assertNotNull(notes);
		assertEquals(1, notes.size());
		assertEquals("Note text line 1", notes.get(0).getText());
	}
	
	@Test
	final void testNoWhitespaces() {
		String input = "{##Note text line 1##}{##Note text line 2##}";
		
		SlideNoteParser parser = new SlideNoteParser();
		parser.parse(input);
		
		List<SlideNote> notes = parser.getSlideNotes();
		
		assertNotNull(notes);
		assertEquals(2, notes.size());
		assertEquals("Note text line 1", notes.get(0).getText());
		assertEquals("Note text line 2", notes.get(1).getText());
	}

}
