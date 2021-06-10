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

package org.lecturestudio.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.CoreTest;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest extends CoreTest {

	private DocumentService documentService;


	@BeforeEach
	void setUp() {
		documentService = new DocumentService(context);
	}

	@Test
	void testOpenDocument() throws Exception {
		String path = getClass().getClassLoader().getResource("empty.pdf").getFile();

		Document doc = documentService.openDocument(new File(path)).get();

		assertNotNull(doc);
		assertEquals(DocumentType.PDF, doc.getType());
		assertEquals(1, doc.getPageCount());
		assertEquals(1, documentService.getDocuments().size());
		assertEquals(doc, documentService.getDocuments().getSelectedDocument());

		assertThrows(ExecutionException.class, () -> {
			String filePath = getClass().getClassLoader().getResource("error.pdf").getFile();

			documentService.openDocument(new File(filePath)).get();
		});
	}

	@Test
	void testAddDocument() throws Exception {
		Document doc = new Document();

		assertEquals(DocumentType.PDF, doc.getType());

		documentService.addDocument(doc);

		assertEquals(1, documentService.getDocuments().size());
		assertEquals(1, documentService.getDocuments().getPdfDocuments().size());
		assertEquals(0, documentService.getDocuments().getWhiteboardCount());

		documentService.addDocument(new Document());

		assertEquals(2, documentService.getDocuments().size());
	}

	@Test
	void testRemoveDocument() throws Exception {
		Document doc1 = new Document();
		Document doc2 = new Document();

		documentService.addDocument(doc1);
		documentService.addDocument(doc2);
		documentService.removeDocument(doc1);

		assertEquals(1, documentService.getDocuments().size());

		documentService.removeDocument(doc2);

		assertEquals(0, documentService.getDocuments().size());
	}

	@Test
	void testSelectDocument() throws Exception {
		Document doc1 = new Document();
		Document doc2 = new Document();

		documentService.selectDocument(doc1);

		assertNull(documentService.getDocuments().getSelectedDocument());

		documentService.selectDocument(null);

		documentService.addDocument(doc1);
		documentService.addDocument(doc2);

		documentService.selectDocument(doc1);

		assertEquals(doc1, documentService.getDocuments().getSelectedDocument());

		documentService.selectDocument(doc2);

		assertEquals(doc2, documentService.getDocuments().getSelectedDocument());
	}

	@Test
	void testCloseDocument() throws Exception {
		Document doc1 = new Document();
		Document doc2 = new Document();

		documentService.addDocument(doc1);
		documentService.addDocument(doc2);
		documentService.selectDocument(doc2);
		documentService.closeDocument(doc2);

		assertEquals(1, documentService.getDocuments().size());
		assertEquals(doc1, documentService.getDocuments().getSelectedDocument());

		documentService.closeDocument(doc1);

		assertEquals(0, documentService.getDocuments().size());
		assertNull(documentService.getDocuments().getSelectedDocument());
	}

	@Test
	void testCloseAllDocuments() throws Exception {
		Document doc1 = new Document();
		Document doc2 = new Document();

		documentService.addDocument(doc1);
		documentService.addDocument(doc2);
		documentService.selectDocument(doc2);
		documentService.closeAllDocuments();

		assertEquals(0, documentService.getDocuments().size());
		assertNull(documentService.getDocuments().getSelectedDocument());
	}

	@Test
	void testAddWhiteboard() throws Exception {
		CompletableFuture<Document> future = documentService.addWhiteboard();

		Document doc = future.get();

		assertNotNull(doc);
		assertEquals(DocumentType.WHITEBOARD, doc.getType());
		assertEquals(1, doc.getPageCount());
	}

	@Test
	void testOpenWhiteboard() throws Exception {
		documentService.openWhiteboard().get();

		DocumentList documentList = documentService.getDocuments();
		Document doc = documentList.getSelectedDocument();

		assertEquals(1, documentList.getWhiteboardCount());
		assertEquals(DocumentType.WHITEBOARD, doc.getType());
		assertEquals(1, doc.getPageCount());
	}

	@Test
	void testAddOpenWhiteboard() throws Exception {
		documentService.addWhiteboard().get();
		documentService.openWhiteboard().get();

		DocumentList documentList = documentService.getDocuments();
		Document doc = documentList.getSelectedDocument();

		assertEquals(1, documentList.getWhiteboardCount());
		assertEquals(DocumentType.WHITEBOARD, doc.getType());
		assertEquals(1, doc.getPageCount());
	}

	@Test
	void testToggleWhiteboard() throws Exception {
		Document doc = new Document();
		doc.setDocumentType(DocumentType.WHITEBOARD);

		documentService.addDocument(doc);
		documentService.selectDocument(doc);

		assertEquals(doc, documentService.getDocuments().getSelectedDocument());

		documentService.toggleWhiteboard();

		Document selected = documentService.getDocuments().getSelectedDocument();

		assertEquals(DocumentType.WHITEBOARD, selected.getType());

		documentService.toggleWhiteboard();

		assertEquals(doc, documentService.getDocuments().getSelectedDocument());
	}

	@Test
	void testCreateWhiteboardPage() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			documentService.createWhiteboardPage();
		});

		Document doc = documentService.openWhiteboard().get();
		documentService.createWhiteboardPage();
		Page page = documentService.createWhiteboardPage();

		assertEquals(3, doc.getPageCount());
		assertEquals(page, doc.getCurrentPage());
	}

	@Test
	void testDeleteWhiteboardPage() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			documentService.deleteWhiteboardPage();
		});

		Document doc = documentService.openWhiteboard().get();
		Page page1 = doc.getCurrentPage();
		Page page2 = documentService.createWhiteboardPage();
		documentService.createWhiteboardPage();

		documentService.deleteWhiteboardPage();

		assertEquals(2, doc.getPageCount());
		assertEquals(page2, doc.getCurrentPage());

		documentService.deleteWhiteboardPage();

		assertEquals(1, doc.getPageCount());
		assertEquals(page1, doc.getCurrentPage());

		documentService.deleteWhiteboardPage();

		assertEquals(1, doc.getPageCount());
	}

	@Test
	void testSelectPage() throws Exception {
		Document doc = new Document();
		Page page1 = doc.createPage();
		Page page2 = doc.createPage();
		Page page3 = doc.createPage();

		documentService.addDocument(doc);
		documentService.selectDocument(doc);
		documentService.selectPage(page1);

		assertEquals(page1, doc.getCurrentPage());

		documentService.selectPage(page3);

		assertEquals(page3, doc.getCurrentPage());

		documentService.selectPage(page2);

		assertEquals(page2, doc.getCurrentPage());
	}

	@Test
	void testSelectNextPage() throws Exception {
		Document doc = new Document();
		Page page1 = doc.createPage();
		Page page2 = doc.createPage();
		Page page3 = doc.createPage();

		documentService.addDocument(doc);
		documentService.selectDocument(doc);

		assertEquals(page1, doc.getCurrentPage());

		documentService.selectNextPage();

		assertEquals(page2, doc.getCurrentPage());

		documentService.selectNextPage();

		assertEquals(page3, doc.getCurrentPage());

		documentService.selectNextPage();

		assertEquals(page3, doc.getCurrentPage());
	}

	@Test
	void testSelectPreviousPage() throws Exception {
		Document doc = new Document();
		Page page1 = doc.createPage();
		Page page2 = doc.createPage();
		Page page3 = doc.createPage();

		documentService.addDocument(doc);
		documentService.selectDocument(doc);
		documentService.selectPage(page3);
		documentService.selectPreviousPage();

		assertEquals(page2, doc.getCurrentPage());

		documentService.selectPreviousPage();

		assertEquals(page1, doc.getCurrentPage());

		documentService.selectPreviousPage();

		assertEquals(page1, doc.getCurrentPage());
	}
}